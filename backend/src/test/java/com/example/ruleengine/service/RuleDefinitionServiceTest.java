package com.example.ruleengine.service;

import com.example.ruleengine.dto.RuleExportDTO;
import com.example.ruleengine.dto.RuleImportRequest;
import com.example.ruleengine.dto.RuleImportResult;
import com.example.ruleengine.entity.RuleAction;
import com.example.ruleengine.entity.RuleCondition;
import com.example.ruleengine.entity.RuleDefinition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 规则定义服务测试类
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class RuleDefinitionServiceTest {

    @Autowired
    private RuleDefinitionService ruleDefinitionService;

    /**
     * 测试创建规则
     */
    @Test
    public void testCreateRule() {
        RuleDefinition rule = createTestRule("TEST_CREATE_001", "测试创建规则");

        Long ruleId = ruleDefinitionService.createRule(rule);

        assertNotNull(ruleId);
        assertTrue(ruleId > 0);

        // 验证规则已创建
        RuleDefinition savedRule = ruleDefinitionService.getById(ruleId);
        assertNotNull(savedRule);
        assertEquals("TEST_CREATE_001", savedRule.getRuleCode());
        assertEquals("测试创建规则", savedRule.getRuleName());
    }

    /**
     * 测试更新规则
     */
    @Test
    public void testUpdateRule() {
        // 先创建规则
        RuleDefinition rule = createTestRule("TEST_UPDATE_001", "测试更新规则");
        Long ruleId = ruleDefinitionService.createRule(rule);

        // 更新规则
        RuleDefinition updateRule = ruleDefinitionService.getById(ruleId);
        updateRule.setRuleName("更新后的规则名称");
        updateRule.setPriority(200);

        ruleDefinitionService.updateRule(updateRule);

        // 验证更新成功
        RuleDefinition updatedRule = ruleDefinitionService.getById(ruleId);
        assertEquals("更新后的规则名称", updatedRule.getRuleName());
        assertEquals(200, updatedRule.getPriority());
    }

    /**
     * 测试复制规则
     */
    @Test
    public void testCopyRule() {
        // 先创建规则
        RuleDefinition rule = createTestRule("TEST_COPY_001", "测试复制规则");
        Long originalRuleId = ruleDefinitionService.createRule(rule);

        // 复制规则
        Long copiedRuleId = ruleDefinitionService.copyRule(originalRuleId);

        assertNotNull(copiedRuleId);
        assertNotEquals(originalRuleId, copiedRuleId);

        // 验证复制的规则
        RuleDefinition copiedRule = ruleDefinitionService.getById(copiedRuleId);
        assertNotNull(copiedRule);
        assertTrue(copiedRule.getRuleCode().contains("_copy_"));
        assertTrue(copiedRule.getRuleName().contains("(副本)"));
    }

    /**
     * 测试导出规则
     */
    @Test
    public void testExportRules() {
        // 创建测试规则
        RuleDefinition rule = createTestRule("TEST_EXPORT_001", "测试导出规则");
        Long ruleId = ruleDefinitionService.createRule(rule);

        // 导出规则
        List<RuleExportDTO> exportedRules = ruleDefinitionService.exportRules(
                Arrays.asList(ruleId), null);

        assertNotNull(exportedRules);
        assertEquals(1, exportedRules.size());

        RuleExportDTO exportedRule = exportedRules.get(0);
        assertEquals("TEST_EXPORT_001", exportedRule.getRuleCode());
        assertEquals("测试导出规则", exportedRule.getRuleName());
    }

    /**
     * 测试导入规则 - 跳过策略
     */
    @Test
    public void testImportRulesWithSkipStrategy() {
        // 先创建一个规则
        RuleDefinition existingRule = createTestRule("TEST_IMPORT_SKIP", "已存在的规则");
        ruleDefinitionService.createRule(existingRule);

        // 准备导入数据（相同的规则编码）
        RuleExportDTO importDTO = new RuleExportDTO();
        importDTO.setRuleCode("TEST_IMPORT_SKIP");
        importDTO.setRuleName("导入的规则");
        importDTO.setSceneCode("INFECTIOUS_DISEASE");
        importDTO.setRuleType("CONDITION");
        importDTO.setPriority(100);
        importDTO.setStatus(1);
        importDTO.setConditions(new ArrayList<>());
        importDTO.setActions(new ArrayList<>());

        RuleImportRequest request = new RuleImportRequest();
        request.setRules(Arrays.asList(importDTO));
        request.setConflictStrategy("SKIP");
        request.setEnableAfterImport(false);

        // 执行导入
        RuleImportResult result = ruleDefinitionService.importRules(request);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getSkipCount());
    }

    /**
     * 测试导入规则 - 重命名策略
     */
    @Test
    public void testImportRulesWithRenameStrategy() {
        // 先创建一个规则
        RuleDefinition existingRule = createTestRule("TEST_IMPORT_RENAME", "已存在的规则");
        ruleDefinitionService.createRule(existingRule);

        // 准备导入数据
        RuleExportDTO importDTO = new RuleExportDTO();
        importDTO.setRuleCode("TEST_IMPORT_RENAME");
        importDTO.setRuleName("导入的规则");
        importDTO.setSceneCode("INFECTIOUS_DISEASE");
        importDTO.setRuleType("CONDITION");
        importDTO.setPriority(100);
        importDTO.setStatus(1);
        importDTO.setConditions(new ArrayList<>());
        importDTO.setActions(new ArrayList<>());

        RuleImportRequest request = new RuleImportRequest();
        request.setRules(Arrays.asList(importDTO));
        request.setConflictStrategy("RENAME");
        request.setEnableAfterImport(false);

        // 执行导入
        RuleImportResult result = ruleDefinitionService.importRules(request);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getSuccessCount());

        // 验证新规则被创建
        assertNotNull(result.getDetails().get(0).getNewRuleId());
    }

    /**
     * 测试批量删除
     */
    @Test
    public void testBatchDelete() {
        // 创建多个规则
        RuleDefinition rule1 = createTestRule("TEST_BATCH_DEL_001", "批量删除测试1");
        RuleDefinition rule2 = createTestRule("TEST_BATCH_DEL_002", "批量删除测试2");

        Long ruleId1 = ruleDefinitionService.createRule(rule1);
        Long ruleId2 = ruleDefinitionService.createRule(rule2);

        // 批量删除
        ruleDefinitionService.batchDelete(Arrays.asList(ruleId1, ruleId2));

        // 验证已删除（逻辑删除）
        RuleDefinition deletedRule1 = ruleDefinitionService.getById(ruleId1);
        RuleDefinition deletedRule2 = ruleDefinitionService.getById(ruleId2);

        // 使用逻辑删除，getById应该返回null
        assertNull(deletedRule1);
        assertNull(deletedRule2);
    }

    /**
     * 测试批量更新状态
     */
    @Test
    public void testBatchUpdateStatus() {
        // 创建多个规则（初始状态为1-启用）
        RuleDefinition rule1 = createTestRule("TEST_BATCH_STATUS_001", "批量状态测试1");
        RuleDefinition rule2 = createTestRule("TEST_BATCH_STATUS_002", "批量状态测试2");

        Long ruleId1 = ruleDefinitionService.createRule(rule1);
        Long ruleId2 = ruleDefinitionService.createRule(rule2);

        // 批量禁用
        ruleDefinitionService.batchUpdateStatus(Arrays.asList(ruleId1, ruleId2), 0);

        // 验证状态已更新
        RuleDefinition updatedRule1 = ruleDefinitionService.getById(ruleId1);
        RuleDefinition updatedRule2 = ruleDefinitionService.getById(ruleId2);

        assertEquals(0, updatedRule1.getStatus());
        assertEquals(0, updatedRule2.getStatus());
    }

    /**
     * 测试根据场景编码查询规则
     */
    @Test
    public void testListBySceneCode() {
        // 创建规则
        RuleDefinition rule = createTestRule("TEST_SCENE_001", "场景查询测试");
        ruleDefinitionService.createRule(rule);

        // 按场景查询
        List<RuleDefinition> rules = ruleDefinitionService.listBySceneCode("INFECTIOUS_DISEASE");

        assertNotNull(rules);
        assertTrue(rules.stream().anyMatch(r -> "TEST_SCENE_001".equals(r.getRuleCode())));
    }

    /**
     * 创建测试规则的辅助方法
     */
    private RuleDefinition createTestRule(String ruleCode, String ruleName) {
        RuleDefinition rule = new RuleDefinition();
        rule.setRuleCode(ruleCode);
        rule.setRuleName(ruleName);
        rule.setRuleDesc("测试规则描述");
        rule.setSceneCode("INFECTIOUS_DISEASE");
        rule.setRuleType("CONDITION");
        rule.setPriority(100);
        rule.setStatus(1);
        rule.setEffectiveStartTime(LocalDateTime.now());
        rule.setEffectiveEndTime(LocalDateTime.now().plusYears(10));
        rule.setVersion(1);

        // 添加测试条件
        List<RuleCondition> conditions = new ArrayList<>();
        RuleCondition condition = new RuleCondition();
        condition.setFieldCode("lab_hiv_antibody");
        condition.setOperator("EQ");
        condition.setFieldValue("阳性");
        condition.setValueType("CONSTANT");
        condition.setGroupId(0L);
        condition.setGroupLogic("AND");
        condition.setSortOrder(1);
        conditions.add(condition);
        rule.setConditions(conditions);

        // 添加测试动作
        List<RuleAction> actions = new ArrayList<>();
        RuleAction action = new RuleAction();
        action.setActionCode("TEST_ACTION");
        action.setActionType("RETURN");
        action.setActionParams("{\"message\":\"测试动作\"}");
        action.setSortOrder(1);
        actions.add(action);
        rule.setActions(actions);

        return rule;
    }
}
