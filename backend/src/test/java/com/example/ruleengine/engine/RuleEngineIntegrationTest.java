package com.example.ruleengine.engine;

import com.example.ruleengine.entity.RuleAction;
import com.example.ruleengine.entity.RuleCondition;
import com.example.ruleengine.entity.RuleDefinition;
import com.example.ruleengine.service.RuleDefinitionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 规则引擎集成测试
 * <p>
 * 测试规则的完整执行流程，包括：
 * <ul>
 *   <li>规则编译和加载</li>
 *   <li>各种条件操作符的执行</li>
 *   <li>条件组的AND/OR逻辑</li>
 *   <li>规则动作的执行</li>
 * </ul>
 * </p>
 *
 * @author 开发团队
 * @since 1.0.0
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class RuleEngineIntegrationTest {

    @Autowired
    private RuleEngine ruleEngine;

    @Autowired
    private RuleDefinitionService ruleDefinitionService;

    @Autowired
    private RuleCompiler ruleCompiler;

    private static final String TEST_SCENE = "INFECTIOUS_DISEASE";

    @BeforeEach
    void setUp() {
        // 每个测试前重新加载规则
        ruleEngine.reload(TEST_SCENE);
    }

    // ==================== 基本功能测试 ====================

    @Test
    @DisplayName("测试规则引擎基本执行 - 匹配单条规则")
    void testBasicRuleExecution() {
        // 创建测试规则
        RuleDefinition rule = createRule("INT_TEST_001", "集成测试规则-基本执行");
        addCondition(rule, "lab_hiv_antibody", "EQ", "阳性", "STRING");
        addAction(rule, "RETURN", "{\"need_report\":true,\"disease_code\":\"HIV\"}");
        ruleDefinitionService.createRule(rule);

        // 重新加载规则
        ruleEngine.reload(TEST_SCENE);

        // 执行规则 - 应该匹配
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("lab_hiv_antibody", "阳性");

        RuleContext context = new RuleContext();
        context.setSceneCode(TEST_SCENE);
        context.setInputData(inputData);

        RuleResult result = ruleEngine.execute(context);

        assertNotNull(result);
        assertTrue(result.isMatched());
        assertTrue(result.getMatchedRuleCodes().contains("INT_TEST_001"));
    }

    @Test
    @DisplayName("测试规则引擎 - 不匹配规则")
    void testRuleNotMatched() {
        // 创建测试规则
        RuleDefinition rule = createRule("INT_TEST_002", "集成测试规则-不匹配");
        addCondition(rule, "lab_hiv_antibody", "EQ", "阳性", "STRING");
        ruleDefinitionService.createRule(rule);

        ruleEngine.reload(TEST_SCENE);

        // 执行规则 - 不应该匹配
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("lab_hiv_antibody", "阴性");

        RuleContext context = new RuleContext();
        context.setSceneCode(TEST_SCENE);
        context.setInputData(inputData);

        RuleResult result = ruleEngine.execute(context);

        assertNotNull(result);
        assertFalse(result.getMatchedRuleCodes().contains("INT_TEST_002"));
    }

    // ==================== 操作符测试 ====================

    @Test
    @DisplayName("测试EQ操作符 - 等于")
    void testEqualOperator() {
        RuleDefinition rule = createRule("INT_TEST_EQ", "等于操作符测试");
        addCondition(rule, "status", "EQ", "confirmed", "STRING");
        ruleDefinitionService.createRule(rule);
        ruleEngine.reload(TEST_SCENE);

        // 测试匹配
        RuleResult result1 = executeRule(Map.of("status", "confirmed"));
        assertTrue(result1.getMatchedRuleCodes().contains("INT_TEST_EQ"));

        // 测试不匹配
        RuleResult result2 = executeRule(Map.of("status", "pending"));
        assertFalse(result2.getMatchedRuleCodes().contains("INT_TEST_EQ"));
    }

    @Test
    @DisplayName("测试NEQ操作符 - 不等于")
    void testNotEqualOperator() {
        RuleDefinition rule = createRule("INT_TEST_NEQ", "不等于操作符测试");
        addCondition(rule, "status", "NEQ", "deleted", "STRING");
        ruleDefinitionService.createRule(rule);
        ruleEngine.reload(TEST_SCENE);

        // 测试匹配
        RuleResult result1 = executeRule(Map.of("status", "active"));
        assertTrue(result1.getMatchedRuleCodes().contains("INT_TEST_NEQ"));

        // 测试不匹配
        RuleResult result2 = executeRule(Map.of("status", "deleted"));
        assertFalse(result2.getMatchedRuleCodes().contains("INT_TEST_NEQ"));
    }

    @Test
    @DisplayName("测试GT/GTE/LT/LTE操作符 - 数值比较")
    void testNumericComparisonOperators() {
        // 创建大于规则
        RuleDefinition ruleGT = createRule("INT_TEST_GT", "大于操作符测试");
        addCondition(ruleGT, "age", "GT", "18", "NUMBER");
        ruleDefinitionService.createRule(ruleGT);

        // 创建大于等于规则
        RuleDefinition ruleGTE = createRule("INT_TEST_GTE", "大于等于操作符测试");
        addCondition(ruleGTE, "age", "GTE", "18", "NUMBER");
        ruleDefinitionService.createRule(ruleGTE);

        // 创建小于规则
        RuleDefinition ruleLT = createRule("INT_TEST_LT", "小于操作符测试");
        addCondition(ruleLT, "age", "LT", "60", "NUMBER");
        ruleDefinitionService.createRule(ruleLT);

        ruleEngine.reload(TEST_SCENE);

        // 测试 age = 20
        RuleResult result1 = executeRule(Map.of("age", 20));
        assertTrue(result1.getMatchedRuleCodes().contains("INT_TEST_GT"));
        assertTrue(result1.getMatchedRuleCodes().contains("INT_TEST_GTE"));
        assertTrue(result1.getMatchedRuleCodes().contains("INT_TEST_LT"));

        // 测试 age = 18
        RuleResult result2 = executeRule(Map.of("age", 18));
        assertFalse(result2.getMatchedRuleCodes().contains("INT_TEST_GT"));
        assertTrue(result2.getMatchedRuleCodes().contains("INT_TEST_GTE"));

        // 测试 age = 17
        RuleResult result3 = executeRule(Map.of("age", 17));
        assertFalse(result3.getMatchedRuleCodes().contains("INT_TEST_GT"));
        assertFalse(result3.getMatchedRuleCodes().contains("INT_TEST_GTE"));
    }

    @Test
    @DisplayName("测试IN操作符 - 包含于列表")
    void testInOperator() {
        RuleDefinition rule = createRule("INT_TEST_IN", "IN操作符测试");
        addCondition(rule, "diagnosis_code", "IN", "[\"B20\",\"B21\",\"B22\"]", "ARRAY");
        ruleDefinitionService.createRule(rule);
        ruleEngine.reload(TEST_SCENE);

        // 测试匹配
        RuleResult result1 = executeRule(Map.of("diagnosis_code", "B20"));
        assertTrue(result1.getMatchedRuleCodes().contains("INT_TEST_IN"));

        // 测试不匹配
        RuleResult result2 = executeRule(Map.of("diagnosis_code", "A00"));
        assertFalse(result2.getMatchedRuleCodes().contains("INT_TEST_IN"));
    }

    @Test
    @DisplayName("测试LIKE操作符 - 模糊匹配")
    void testLikeOperator() {
        RuleDefinition rule = createRule("INT_TEST_LIKE", "LIKE操作符测试");
        addCondition(rule, "diagnosis_name", "LIKE", "肺炎", "STRING");
        ruleDefinitionService.createRule(rule);
        ruleEngine.reload(TEST_SCENE);

        // 测试匹配
        RuleResult result1 = executeRule(Map.of("diagnosis_name", "新冠肺炎"));
        assertTrue(result1.getMatchedRuleCodes().contains("INT_TEST_LIKE"));

        RuleResult result2 = executeRule(Map.of("diagnosis_name", "肺炎球菌感染"));
        assertTrue(result2.getMatchedRuleCodes().contains("INT_TEST_LIKE"));

        // 测试不匹配
        RuleResult result3 = executeRule(Map.of("diagnosis_name", "感冒"));
        assertFalse(result3.getMatchedRuleCodes().contains("INT_TEST_LIKE"));
    }

    @Test
    @DisplayName("测试LEFT_MATCH操作符 - 左匹配/前缀匹配")
    void testLeftMatchOperator() {
        RuleDefinition rule = createRule("INT_TEST_LEFT", "左匹配操作符测试");
        addCondition(rule, "icd_code", "LEFT_MATCH", "B20", "STRING");
        ruleDefinitionService.createRule(rule);
        ruleEngine.reload(TEST_SCENE);

        // 测试匹配
        RuleResult result1 = executeRule(Map.of("icd_code", "B20.100"));
        assertTrue(result1.getMatchedRuleCodes().contains("INT_TEST_LEFT"));

        RuleResult result2 = executeRule(Map.of("icd_code", "B20"));
        assertTrue(result2.getMatchedRuleCodes().contains("INT_TEST_LEFT"));

        // 测试不匹配
        RuleResult result3 = executeRule(Map.of("icd_code", "A20.100"));
        assertFalse(result3.getMatchedRuleCodes().contains("INT_TEST_LEFT"));
    }

    @Test
    @DisplayName("测试BETWEEN操作符 - 范围查询")
    void testBetweenOperator() {
        RuleDefinition rule = createRule("INT_TEST_BETWEEN", "BETWEEN操作符测试");
        addCondition(rule, "wbc_count", "BETWEEN", "[4.0, 10.0]", "NUMBER");
        ruleDefinitionService.createRule(rule);
        ruleEngine.reload(TEST_SCENE);

        // 测试匹配 - 在范围内
        RuleResult result1 = executeRule(Map.of("wbc_count", 7.5));
        assertTrue(result1.getMatchedRuleCodes().contains("INT_TEST_BETWEEN"));

        // 测试边界值
        RuleResult result2 = executeRule(Map.of("wbc_count", 4.0));
        assertTrue(result2.getMatchedRuleCodes().contains("INT_TEST_BETWEEN"));

        // 测试不匹配 - 超出范围
        RuleResult result3 = executeRule(Map.of("wbc_count", 15.0));
        assertFalse(result3.getMatchedRuleCodes().contains("INT_TEST_BETWEEN"));
    }

    @Test
    @DisplayName("测试IS_NULL/IS_NOT_NULL操作符 - 空值判断")
    void testNullOperators() {
        // 创建IS_NULL规则
        RuleDefinition ruleNull = createRule("INT_TEST_NULL", "IS_NULL操作符测试");
        addCondition(ruleNull, "optional_field", "IS_NULL", "", "STRING");
        ruleDefinitionService.createRule(ruleNull);

        // 创建IS_NOT_NULL规则
        RuleDefinition ruleNotNull = createRule("INT_TEST_NOT_NULL", "IS_NOT_NULL操作符测试");
        addCondition(ruleNotNull, "required_field", "IS_NOT_NULL", "", "STRING");
        ruleDefinitionService.createRule(ruleNotNull);

        ruleEngine.reload(TEST_SCENE);

        // 测试IS_NULL
        Map<String, Object> data1 = new HashMap<>();
        data1.put("optional_field", null);
        data1.put("required_field", "value");
        RuleResult result1 = executeRule(data1);
        assertTrue(result1.getMatchedRuleCodes().contains("INT_TEST_NULL"));
        assertTrue(result1.getMatchedRuleCodes().contains("INT_TEST_NOT_NULL"));

        // 测试字段有值
        Map<String, Object> data2 = new HashMap<>();
        data2.put("optional_field", "has_value");
        data2.put("required_field", "value");
        RuleResult result2 = executeRule(data2);
        assertFalse(result2.getMatchedRuleCodes().contains("INT_TEST_NULL"));
    }

    // ==================== 条件组逻辑测试 ====================

    @Test
    @DisplayName("测试AND逻辑 - 所有条件都满足")
    void testAndLogic() {
        RuleDefinition rule = createRule("INT_TEST_AND", "AND逻辑测试");
        // 同一个组内的条件默认是AND关系
        addConditionWithGroup(rule, "status", "EQ", "confirmed", "STRING", 1L, "AND");
        addConditionWithGroup(rule, "age", "GTE", "18", "NUMBER", 1L, "AND");
        ruleDefinitionService.createRule(rule);
        ruleEngine.reload(TEST_SCENE);

        // 测试全部满足
        RuleResult result1 = executeRule(Map.of("status", "confirmed", "age", 25));
        assertTrue(result1.getMatchedRuleCodes().contains("INT_TEST_AND"));

        // 测试部分满足
        RuleResult result2 = executeRule(Map.of("status", "confirmed", "age", 15));
        assertFalse(result2.getMatchedRuleCodes().contains("INT_TEST_AND"));

        RuleResult result3 = executeRule(Map.of("status", "pending", "age", 25));
        assertFalse(result3.getMatchedRuleCodes().contains("INT_TEST_AND"));
    }

    @Test
    @DisplayName("测试OR逻辑 - 任一条件满足")
    void testOrLogic() {
        RuleDefinition rule = createRule("INT_TEST_OR", "OR逻辑测试");
        // 不同组之间是OR关系
        addConditionWithGroup(rule, "diagnosis_code", "EQ", "B20", "STRING", 1L, "AND");
        addConditionWithGroup(rule, "lab_hiv_antibody", "EQ", "阳性", "STRING", 2L, "AND");
        ruleDefinitionService.createRule(rule);
        ruleEngine.reload(TEST_SCENE);

        // 测试第一个条件满足
        RuleResult result1 = executeRule(Map.of("diagnosis_code", "B20", "lab_hiv_antibody", "阴性"));
        assertTrue(result1.getMatchedRuleCodes().contains("INT_TEST_OR"));

        // 测试第二个条件满足
        RuleResult result2 = executeRule(Map.of("diagnosis_code", "A00", "lab_hiv_antibody", "阳性"));
        assertTrue(result1.getMatchedRuleCodes().contains("INT_TEST_OR"));

        // 测试都不满足
        RuleResult result3 = executeRule(Map.of("diagnosis_code", "A00", "lab_hiv_antibody", "阴性"));
        assertFalse(result3.getMatchedRuleCodes().contains("INT_TEST_OR"));
    }

    // ==================== 规则优先级测试 ====================

    @Test
    @DisplayName("测试规则优先级 - 高优先级规则先执行")
    void testRulePriority() {
        // 创建低优先级规则
        RuleDefinition ruleLow = createRule("INT_TEST_PRIO_LOW", "低优先级规则");
        ruleLow.setPriority(10);
        addCondition(ruleLow, "type", "EQ", "test", "STRING");
        addAction(ruleLow, "RETURN", "{\"priority\":\"low\"}");
        ruleDefinitionService.createRule(ruleLow);

        // 创建高优先级规则
        RuleDefinition ruleHigh = createRule("INT_TEST_PRIO_HIGH", "高优先级规则");
        ruleHigh.setPriority(100);
        addCondition(ruleHigh, "type", "EQ", "test", "STRING");
        addAction(ruleHigh, "RETURN", "{\"priority\":\"high\"}");
        ruleDefinitionService.createRule(ruleHigh);

        ruleEngine.reload(TEST_SCENE);

        RuleResult result = executeRule(Map.of("type", "test"));

        // 两个规则都应该匹配
        assertTrue(result.getMatchedRuleCodes().contains("INT_TEST_PRIO_LOW"));
        assertTrue(result.getMatchedRuleCodes().contains("INT_TEST_PRIO_HIGH"));

        // 高优先级规则应该先执行（出现在列表前面）
        List<String> matchedCodes = new ArrayList<>(result.getMatchedRuleCodes());
        int highIndex = matchedCodes.indexOf("INT_TEST_PRIO_HIGH");
        int lowIndex = matchedCodes.indexOf("INT_TEST_PRIO_LOW");
        assertTrue(highIndex < lowIndex, "高优先级规则应该先执行");
    }

    // ==================== 规则编译器测试 ====================

    @Test
    @DisplayName("测试规则编译器 - 生成有效DRL")
    void testRuleCompiler() {
        RuleDefinition rule = createRule("INT_TEST_COMPILER", "编译器测试规则");
        addCondition(rule, "field1", "EQ", "value1", "STRING");
        addCondition(rule, "field2", "GT", "10", "NUMBER");
        addAction(rule, "RETURN", "{\"result\":true}");
        ruleDefinitionService.createRule(rule);

        // 获取完整规则（包含条件和动作）
        RuleDefinition fullRule = ruleDefinitionService.getRuleDetail(rule.getId());

        // 编译规则
        String drl = ruleCompiler.compile(fullRule);

        assertNotNull(drl);
        assertTrue(drl.contains("INT_TEST_COMPILER"));
        assertTrue(drl.contains("package rules"));
        assertTrue(drl.contains("when"));
        assertTrue(drl.contains("then"));
    }

    @Test
    @DisplayName("测试规则语法验证")
    void testRuleValidation() {
        RuleDefinition validRule = createRule("INT_TEST_VALID", "有效规则");
        addCondition(validRule, "status", "EQ", "active", "STRING");
        ruleDefinitionService.createRule(validRule);

        RuleDefinition fullRule = ruleDefinitionService.getRuleDetail(validRule.getId());
        String validationResult = ruleCompiler.validate(fullRule);

        // 有效规则验证应该返回null
        assertNull(validationResult, "有效规则验证应该通过");
    }

    // ==================== 批量执行测试 ====================

    @Test
    @DisplayName("测试批量执行规则")
    void testBatchExecution() {
        // 创建规则
        RuleDefinition rule = createRule("INT_TEST_BATCH", "批量执行测试规则");
        addCondition(rule, "value", "GT", "50", "NUMBER");
        ruleDefinitionService.createRule(rule);
        ruleEngine.reload(TEST_SCENE);

        // 准备批量数据
        List<RuleContext> contexts = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            RuleContext ctx = new RuleContext();
            ctx.setSceneCode(TEST_SCENE);
            ctx.setInputData(Map.of("value", i * 10)); // 0, 10, 20, ..., 90
            contexts.add(ctx);
        }

        // 批量执行
        List<RuleResult> results = ruleEngine.batchExecute(contexts);

        assertNotNull(results);
        assertEquals(10, results.size());

        // 验证结果 - value > 50 的应该匹配 (60, 70, 80, 90)
        long matchedCount = results.stream()
                .filter(r -> r.getMatchedRuleCodes().contains("INT_TEST_BATCH"))
                .count();
        assertEquals(4, matchedCount);
    }

    // ==================== 辅助方法 ====================

    private RuleDefinition createRule(String ruleCode, String ruleName) {
        RuleDefinition rule = new RuleDefinition();
        rule.setRuleCode(ruleCode);
        rule.setRuleName(ruleName);
        rule.setRuleDesc("集成测试规则");
        rule.setSceneCode(TEST_SCENE);
        rule.setRuleType("CONDITION");
        rule.setPriority(50);
        rule.setStatus(1);
        rule.setEffectiveStartTime(LocalDateTime.now().minusDays(1));
        rule.setEffectiveEndTime(LocalDateTime.now().plusYears(10));
        rule.setVersion(1);
        rule.setConditions(new ArrayList<>());
        rule.setActions(new ArrayList<>());
        return rule;
    }

    private void addCondition(RuleDefinition rule, String fieldCode, String operator,
                              String value, String valueType) {
        addConditionWithGroup(rule, fieldCode, operator, value, valueType, 1L, "AND");
    }

    private void addConditionWithGroup(RuleDefinition rule, String fieldCode, String operator,
                                        String value, String valueType, Long groupId, String groupLogic) {
        RuleCondition condition = new RuleCondition();
        condition.setFieldCode(fieldCode);
        condition.setOperator(operator);
        condition.setFieldValue(value);
        condition.setValueType(valueType);
        condition.setGroupId(groupId);
        condition.setGroupLogic(groupLogic);
        condition.setSortOrder(rule.getConditions().size());
        rule.getConditions().add(condition);
    }

    private void addAction(RuleDefinition rule, String actionType, String actionParams) {
        RuleAction action = new RuleAction();
        action.setActionCode(rule.getRuleCode() + "_ACTION_" + rule.getActions().size());
        action.setActionType(actionType);
        action.setActionParams(actionParams);
        action.setSortOrder(rule.getActions().size());
        rule.getActions().add(action);
    }

    private RuleResult executeRule(Map<String, Object> inputData) {
        RuleContext context = new RuleContext();
        context.setSceneCode(TEST_SCENE);
        context.setInputData(inputData);
        return ruleEngine.execute(context);
    }
}
