package com.example.ruleengine.engine.impl;

import com.example.ruleengine.engine.RuleValidator;
import com.example.ruleengine.entity.RuleDefinition;
import com.example.ruleengine.service.BusinessSceneService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 规则验证器实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RuleValidatorImpl implements RuleValidator {

    private final BusinessSceneService businessSceneService;
    private final ObjectMapper objectMapper;

    @Override
    public List<String> validate(RuleDefinition rule) {
        List<String> errors = new ArrayList<>();

        // 验证基本字段
        if (!StringUtils.hasText(rule.getRuleCode())) {
            errors.add("规则编码不能为空");
        } else if (rule.getRuleCode().length() > 64) {
            errors.add("规则编码长度不能超过64个字符");
        } else if (!rule.getRuleCode().matches("^[A-Za-z0-9_]+$")) {
            errors.add("规则编码只能包含字母、数字和下划线");
        }

        if (!StringUtils.hasText(rule.getRuleName())) {
            errors.add("规则名称不能为空");
        } else if (rule.getRuleName().length() > 128) {
            errors.add("规则名称长度不能超过128个字符");
        }

        if (!StringUtils.hasText(rule.getSceneCode())) {
            errors.add("场景编码不能为空");
        } else {
            // 验证场景是否存在
            if (businessSceneService.getBySceneCode(rule.getSceneCode()) == null) {
                errors.add("场景不存在: " + rule.getSceneCode());
            }
        }

        // 验证规则类型
        if (!StringUtils.hasText(rule.getRuleType())) {
            errors.add("规则类型不能为空");
        } else if (!isValidRuleType(rule.getRuleType())) {
            errors.add("无效的规则类型: " + rule.getRuleType());
        }

        // 验证优先级
        if (rule.getPriority() != null && (rule.getPriority() < 0 || rule.getPriority() > 10000)) {
            errors.add("优先级必须在0-10000之间");
        }

        // 验证状态
        if (rule.getStatus() != null && (rule.getStatus() < 0 || rule.getStatus() > 2)) {
            errors.add("无效的状态值: " + rule.getStatus());
        }

        // 验证生效时间
        if (rule.getEffectiveStartTime() != null && rule.getEffectiveEndTime() != null) {
            if (rule.getEffectiveStartTime().isAfter(rule.getEffectiveEndTime())) {
                errors.add("生效开始时间不能晚于结束时间");
            }
        }

        // 验证JSON配置
        if (StringUtils.hasText(rule.getJsonConfig())) {
            errors.addAll(validateConditions(rule.getJsonConfig()));
        }

        return errors;
    }

    @Override
    public String validateDrl(String drl) {
        if (!StringUtils.hasText(drl)) {
            return "DRL内容不能为空";
        }

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
            log.error("DRL验证异常", e);
            return "DRL验证异常: " + e.getMessage();
        }
    }

    @Override
    public List<String> validateConditions(String jsonConfig) {
        List<String> errors = new ArrayList<>();

        if (!StringUtils.hasText(jsonConfig)) {
            return errors;
        }

        try {
            JsonNode root = objectMapper.readTree(jsonConfig);

            // 验证条件结构
            if (root.has("conditions")) {
                validateConditionNode(root.get("conditions"), errors, "conditions");
            }

            // 验证动作结构
            if (root.has("actions")) {
                validateActionsNode(root.get("actions"), errors);
            }

        } catch (Exception e) {
            errors.add("JSON配置解析失败: " + e.getMessage());
        }

        return errors;
    }

    /**
     * 验证条件节点
     */
    private void validateConditionNode(JsonNode node, List<String> errors, String path) {
        if (node == null) {
            return;
        }

        if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                validateConditionNode(node.get(i), errors, path + "[" + i + "]");
            }
        } else if (node.isObject()) {
            // 验证逻辑操作符
            if (node.has("logic")) {
                String logic = node.get("logic").asText();
                if (!"AND".equalsIgnoreCase(logic) && !"OR".equalsIgnoreCase(logic)) {
                    errors.add(path + ": 无效的逻辑操作符 '" + logic + "'");
                }
            }

            // 验证条件组
            if (node.has("groups")) {
                validateConditionNode(node.get("groups"), errors, path + ".groups");
            }

            // 验证单个条件
            if (node.has("field")) {
                validateSingleCondition(node, errors, path);
            }
        }
    }

    /**
     * 验证单个条件
     */
    private void validateSingleCondition(JsonNode node, List<String> errors, String path) {
        // 验证字段
        if (!node.has("field") || !StringUtils.hasText(node.get("field").asText())) {
            errors.add(path + ": 条件字段不能为空");
        }

        // 验证操作符
        if (!node.has("operator") || !StringUtils.hasText(node.get("operator").asText())) {
            errors.add(path + ": 操作符不能为空");
        } else {
            String operator = node.get("operator").asText();
            if (!isValidOperator(operator)) {
                errors.add(path + ": 无效的操作符 '" + operator + "'");
            }
        }

        // 验证值（部分操作符允许空值）
        String operator = node.has("operator") ? node.get("operator").asText() : "";
        if (!isNullableOperator(operator)) {
            if (!node.has("value") || node.get("value").isNull()) {
                errors.add(path + ": 条件值不能为空");
            }
        }
    }

    /**
     * 验证动作节点
     */
    private void validateActionsNode(JsonNode node, List<String> errors) {
        if (node == null || !node.isArray()) {
            return;
        }

        for (int i = 0; i < node.size(); i++) {
            JsonNode action = node.get(i);
            String path = "actions[" + i + "]";

            if (!action.has("actionType") || !StringUtils.hasText(action.get("actionType").asText())) {
                errors.add(path + ": 动作类型不能为空");
            } else {
                String actionType = action.get("actionType").asText();
                if (!isValidActionType(actionType)) {
                    errors.add(path + ": 无效的动作类型 '" + actionType + "'");
                }
            }
        }
    }

    /**
     * 是否有效的规则类型
     */
    private boolean isValidRuleType(String ruleType) {
        return "CONDITION".equalsIgnoreCase(ruleType) ||
                "DECISION_TABLE".equalsIgnoreCase(ruleType) ||
                "SCRIPT".equalsIgnoreCase(ruleType);
    }

    /**
     * 是否有效的操作符
     */
    private boolean isValidOperator(String operator) {
        String[] validOperators = {
                "EQ", "NEQ", "GT", "GTE", "LT", "LTE",
                "IN", "NOT_IN", "LIKE", "NOT_LIKE",
                "CONTAINS", "NOT_CONTAINS",
                "LEFT_MATCH", "RIGHT_MATCH", "STARTS_WITH", "ENDS_WITH",
                "BETWEEN", "IS_NULL", "IS_NOT_NULL"
        };

        for (String valid : validOperators) {
            if (valid.equalsIgnoreCase(operator)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否允许空值的操作符
     */
    private boolean isNullableOperator(String operator) {
        return "IS_NULL".equalsIgnoreCase(operator) ||
                "IS_NOT_NULL".equalsIgnoreCase(operator);
    }

    /**
     * 是否有效的动作类型
     */
    private boolean isValidActionType(String actionType) {
        return "RETURN".equalsIgnoreCase(actionType) ||
                "MODIFY".equalsIgnoreCase(actionType) ||
                "LOG".equalsIgnoreCase(actionType) ||
                "CALL_SERVICE".equalsIgnoreCase(actionType) ||
                "SEND_MESSAGE".equalsIgnoreCase(actionType);
    }
}
