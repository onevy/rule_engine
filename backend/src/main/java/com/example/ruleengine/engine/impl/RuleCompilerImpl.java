package com.example.ruleengine.engine.impl;

import com.example.ruleengine.engine.RuleCompiler;
import com.example.ruleengine.entity.RuleAction;
import com.example.ruleengine.entity.RuleCondition;
import com.example.ruleengine.entity.RuleDefinition;
import com.example.ruleengine.service.RuleActionService;
import com.example.ruleengine.service.RuleConditionService;
import com.example.ruleengine.service.RuleDefinitionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 规则编译器实现
 * <p>
 * 负责将数据库中的规则定义转换为Drools DRL（Drools Rule Language）格式。
 * </p>
 *
 * <h3>核心功能：</h3>
 * <ul>
 *   <li>将条件配置转换为Drools eval表达式</li>
 *   <li>将动作配置转换为Drools then块代码</li>
 *   <li>支持条件组的AND/OR逻辑组合</li>
 *   <li>提供DRL语法验证</li>
 * </ul>
 *
 * <h3>支持的操作符：</h3>
 * <ul>
 *   <li>比较：EQ, NEQ, GT, GTE, LT, LTE</li>
 *   <li>集合：IN, NOT_IN</li>
 *   <li>字符串：LIKE, CONTAINS, STARTS_WITH, ENDS_WITH</li>
 *   <li>范围：BETWEEN</li>
 *   <li>空值：IS_NULL, IS_NOT_NULL</li>
 * </ul>
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RuleCompilerImpl implements RuleCompiler {

    private final RuleDefinitionService ruleDefinitionService;
    private final RuleConditionService ruleConditionService;
    private final RuleActionService ruleActionService;
    private final ObjectMapper objectMapper;

    private static final String DRL_TEMPLATE =
            "package rules.%s;\n\n" +
            "import java.util.Map;\n" +
            "import java.util.List;\n" +
            "import java.util.Arrays;\n" +
            "import com.example.ruleengine.engine.RuleContext;\n" +
            "import com.example.ruleengine.engine.RuleResult;\n\n" +
            "global RuleResult ruleResult;\n\n" +
            "%s\n";

    private static final String RULE_TEMPLATE =
            "rule \"%s\"\n" +
            "    salience %d\n" +
            "when\n" +
            "    $context : RuleContext()\n" +
            "    $data : Map() from $context.inputData\n" +
            "    eval(%s)\n" +
            "then\n" +
            "    ruleResult.addMatchedRuleCode(\"%s\");\n" +
            "    %s\n" +
            "end\n";

    @Override
    public String compile(RuleDefinition rule) {
        try {
            String ruleContent = compileRule(rule);
            return String.format(DRL_TEMPLATE, rule.getSceneCode().toLowerCase(), ruleContent);
        } catch (Exception e) {
            log.error("编译规则失败: ruleCode={}", rule.getRuleCode(), e);
            throw new RuntimeException("规则编译失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String compile(List<RuleDefinition> rules) {
        if (rules == null || rules.isEmpty()) {
            return "";
        }

        String sceneCode = rules.get(0).getSceneCode();
        StringBuilder rulesContent = new StringBuilder();

        for (RuleDefinition rule : rules) {
            try {
                rulesContent.append(compileRule(rule)).append("\n\n");
            } catch (Exception e) {
                log.error("编译规则失败: ruleCode={}", rule.getRuleCode(), e);
            }
        }

        return String.format(DRL_TEMPLATE, sceneCode.toLowerCase(), rulesContent.toString());
    }

    @Override
    public String compileByScene(String sceneCode) {
        List<RuleDefinition> rules = ruleDefinitionService.listActiveBySceneCode(sceneCode);
        if (rules.isEmpty()) {
            log.warn("场景 {} 没有启用的规则", sceneCode);
            return String.format(DRL_TEMPLATE, sceneCode.toLowerCase(), "// No active rules");
        }
        return compile(rules);
    }

    @Override
    public String validate(RuleDefinition rule) {
        try {
            String drl = compile(rule);
            return validateDrl(drl);
        } catch (Exception e) {
            return "规则编译错误: " + e.getMessage();
        }
    }

    /**
     * 验证DRL语法
     */
    private String validateDrl(String drl) {
        try {
            KieServices kieServices = KieServices.Factory.get();
            KieFileSystem kfs = kieServices.newKieFileSystem();
            kfs.write("src/main/resources/rules/validation.drl", drl);

            KieBuilder kieBuilder = kieServices.newKieBuilder(kfs);
            kieBuilder.buildAll();

            if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
                return kieBuilder.getResults().getMessages(Message.Level.ERROR)
                        .stream()
                        .map(Message::getText)
                        .collect(Collectors.joining("\n"));
            }
            return null;
        } catch (Exception e) {
            return "DRL验证异常: " + e.getMessage();
        }
    }

    /**
     * 编译单个规则
     */
    private String compileRule(RuleDefinition rule) {
        // 加载条件和动作
        List<RuleCondition> conditions = ruleConditionService.listByRuleId(rule.getId());
        List<RuleAction> actions = ruleActionService.listByRuleId(rule.getId());

        // 构建条件表达式
        String conditionExpr = buildConditionExpression(conditions);
        if (!StringUtils.hasText(conditionExpr)) {
            conditionExpr = "true";
        }

        // 构建动作表达式
        String actionExpr = buildActionExpression(actions);

        return String.format(RULE_TEMPLATE,
                rule.getRuleCode(),
                rule.getPriority() != null ? rule.getPriority() : 0,
                conditionExpr,
                rule.getRuleCode(),
                actionExpr);
    }

    /**
     * 构建条件表达式
     */
    private String buildConditionExpression(List<RuleCondition> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return "true";
        }

        // 按条件组ID分组
        Map<Long, List<RuleCondition>> groupedConditions = conditions.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getGroupId() != null ? c.getGroupId() : 0L,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<String> groupExpressions = new ArrayList<>();

        for (Map.Entry<Long, List<RuleCondition>> entry : groupedConditions.entrySet()) {
            List<String> conditionExprs = new ArrayList<>();
            for (RuleCondition condition : entry.getValue()) {
                String expr = buildSingleCondition(condition);
                if (StringUtils.hasText(expr)) {
                    conditionExprs.add(expr);
                }
            }
            if (!conditionExprs.isEmpty()) {
                // 同一组内的条件用 AND 连接
                groupExpressions.add("(" + String.join(" && ", conditionExprs) + ")");
            }
        }

        if (groupExpressions.isEmpty()) {
            return "true";
        }

        // 不同组之间用 OR 连接
        return String.join(" || ", groupExpressions);
    }

    /**
     * 构建单个条件表达式
     */
    private String buildSingleCondition(RuleCondition condition) {
        String fieldCode = condition.getFieldCode();
        String operator = condition.getOperator();
        String fieldValue = condition.getFieldValue();

        if (!StringUtils.hasText(fieldCode) || !StringUtils.hasText(operator)) {
            return "";
        }

        String fieldAccess = String.format("$data.get(\"%s\")", fieldCode);

        switch (operator.toUpperCase()) {
            case "EQ":
                return buildEqualCondition(fieldAccess, fieldValue, condition.getValueType());
            case "NEQ":
                return buildNotEqualCondition(fieldAccess, fieldValue, condition.getValueType());
            case "GT":
                return buildCompareCondition(fieldAccess, fieldValue, ">");
            case "GTE":
                return buildCompareCondition(fieldAccess, fieldValue, ">=");
            case "LT":
                return buildCompareCondition(fieldAccess, fieldValue, "<");
            case "LTE":
                return buildCompareCondition(fieldAccess, fieldValue, "<=");
            case "IN":
                return buildInCondition(fieldAccess, fieldValue);
            case "NOT_IN":
                return buildNotInCondition(fieldAccess, fieldValue);
            case "LIKE":
            case "CONTAINS":
                return buildContainsCondition(fieldAccess, fieldValue);
            case "NOT_LIKE":
            case "NOT_CONTAINS":
                return buildNotContainsCondition(fieldAccess, fieldValue);
            case "LEFT_MATCH":
            case "STARTS_WITH":
                return buildStartsWithCondition(fieldAccess, fieldValue);
            case "RIGHT_MATCH":
            case "ENDS_WITH":
                return buildEndsWithCondition(fieldAccess, fieldValue);
            case "BETWEEN":
                return buildBetweenCondition(fieldAccess, fieldValue);
            case "IS_NULL":
                return String.format("%s == null", fieldAccess);
            case "IS_NOT_NULL":
                return String.format("%s != null", fieldAccess);
            default:
                log.warn("不支持的操作符: {}", operator);
                return "true";
        }
    }

    private String buildEqualCondition(String fieldAccess, String value, String valueType) {
        if ("NUMBER".equalsIgnoreCase(valueType)) {
            return String.format("(%s != null && ((Number)%s).doubleValue() == %s)",
                    fieldAccess, fieldAccess, value);
        } else if ("BOOLEAN".equalsIgnoreCase(valueType)) {
            return String.format("(%s != null && Boolean.valueOf(%s.toString()) == %s)",
                    fieldAccess, fieldAccess, value);
        } else {
            return String.format("(%s != null && %s.toString().equals(\"%s\"))",
                    fieldAccess, fieldAccess, escapeString(value));
        }
    }

    private String buildNotEqualCondition(String fieldAccess, String value, String valueType) {
        if ("NUMBER".equalsIgnoreCase(valueType)) {
            return String.format("(%s == null || ((Number)%s).doubleValue() != %s)",
                    fieldAccess, fieldAccess, value);
        } else {
            return String.format("(%s == null || !%s.toString().equals(\"%s\"))",
                    fieldAccess, fieldAccess, escapeString(value));
        }
    }

    private String buildCompareCondition(String fieldAccess, String value, String operator) {
        return String.format("(%s != null && ((Number)%s).doubleValue() %s %s)",
                fieldAccess, fieldAccess, operator, value);
    }

    private String buildInCondition(String fieldAccess, String value) {
        List<String> values = parseArrayValue(value);
        if (values.isEmpty()) {
            return "false";
        }
        String valueList = values.stream()
                .map(v -> "\"" + escapeString(v) + "\"")
                .collect(Collectors.joining(", "));
        return String.format("(%s != null && Arrays.asList(%s).contains(%s.toString()))",
                fieldAccess, valueList, fieldAccess);
    }

    private String buildNotInCondition(String fieldAccess, String value) {
        List<String> values = parseArrayValue(value);
        if (values.isEmpty()) {
            return "true";
        }
        String valueList = values.stream()
                .map(v -> "\"" + escapeString(v) + "\"")
                .collect(Collectors.joining(", "));
        return String.format("(%s == null || !Arrays.asList(%s).contains(%s.toString()))",
                fieldAccess, valueList, fieldAccess);
    }

    private String buildContainsCondition(String fieldAccess, String value) {
        return String.format("(%s != null && %s.toString().contains(\"%s\"))",
                fieldAccess, fieldAccess, escapeString(value));
    }

    private String buildNotContainsCondition(String fieldAccess, String value) {
        return String.format("(%s == null || !%s.toString().contains(\"%s\"))",
                fieldAccess, fieldAccess, escapeString(value));
    }

    private String buildStartsWithCondition(String fieldAccess, String value) {
        return String.format("(%s != null && %s.toString().startsWith(\"%s\"))",
                fieldAccess, fieldAccess, escapeString(value));
    }

    private String buildEndsWithCondition(String fieldAccess, String value) {
        return String.format("(%s != null && %s.toString().endsWith(\"%s\"))",
                fieldAccess, fieldAccess, escapeString(value));
    }

    private String buildBetweenCondition(String fieldAccess, String value) {
        try {
            List<String> range = parseArrayValue(value);
            if (range.size() >= 2) {
                return String.format("(%s != null && ((Number)%s).doubleValue() >= %s && ((Number)%s).doubleValue() <= %s)",
                        fieldAccess, fieldAccess, range.get(0), fieldAccess, range.get(1));
            }
        } catch (Exception e) {
            log.error("解析BETWEEN值失败: {}", value, e);
        }
        return "true";
    }

    /**
     * 构建动作表达式
     */
    private String buildActionExpression(List<RuleAction> actions) {
        if (actions == null || actions.isEmpty()) {
            return "";
        }

        StringBuilder actionExpr = new StringBuilder();
        for (RuleAction action : actions) {
            String actionType = action.getActionType();
            String actionParams = action.getActionParams();

            if ("RETURN".equalsIgnoreCase(actionType)) {
                // 解析返回参数并设置到输出
                try {
                    Map<String, Object> params = objectMapper.readValue(actionParams,
                            new TypeReference<Map<String, Object>>() {});
                    for (Map.Entry<String, Object> entry : params.entrySet()) {
                        Object value = entry.getValue();
                        if (value instanceof String) {
                            actionExpr.append(String.format(
                                    "ruleResult.setOutput(\"%s\", \"%s\");\n    ",
                                    entry.getKey(), escapeString((String) value)));
                        } else if (value instanceof Number) {
                            actionExpr.append(String.format(
                                    "ruleResult.setOutput(\"%s\", %s);\n    ",
                                    entry.getKey(), value));
                        } else if (value instanceof Boolean) {
                            actionExpr.append(String.format(
                                    "ruleResult.setOutput(\"%s\", %s);\n    ",
                                    entry.getKey(), value));
                        } else {
                            actionExpr.append(String.format(
                                    "ruleResult.setOutput(\"%s\", \"%s\");\n    ",
                                    entry.getKey(), escapeString(value.toString())));
                        }
                    }
                } catch (Exception e) {
                    log.error("解析动作参数失败: {}", actionParams, e);
                }
            } else if ("LOG".equalsIgnoreCase(actionType)) {
                actionExpr.append(String.format(
                        "System.out.println(\"[规则日志] %s\");\n    ",
                        escapeString(actionParams)));
            }
        }

        return actionExpr.toString();
    }

    /**
     * 解析数组值
     */
    private List<String> parseArrayValue(String value) {
        if (!StringUtils.hasText(value)) {
            return Collections.emptyList();
        }

        try {
            // 尝试解析JSON数组
            if (value.trim().startsWith("[")) {
                return objectMapper.readValue(value, new TypeReference<List<String>>() {});
            }
            // 逗号分隔
            return Arrays.asList(value.split(","))
                    .stream()
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.singletonList(value);
        }
    }

    /**
     * 转义字符串
     */
    private String escapeString(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
