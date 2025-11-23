package com.example.ruleengine.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RuleExportDTO单元测试
 */
public class RuleExportDTOTest {

    @Test
    public void testRuleExportDTOCreation() {
        RuleExportDTO dto = new RuleExportDTO();
        dto.setRuleCode("TEST_RULE_001");
        dto.setRuleName("测试规则");
        dto.setRuleDesc("测试规则描述");
        dto.setSceneCode("INFECTIOUS_DISEASE");
        dto.setRuleType("CONDITION");
        dto.setPriority(100);
        dto.setStatus(1);
        dto.setEffectiveStartTime(LocalDateTime.now());
        dto.setEffectiveEndTime(LocalDateTime.now().plusYears(1));

        assertEquals("TEST_RULE_001", dto.getRuleCode());
        assertEquals("测试规则", dto.getRuleName());
        assertEquals("INFECTIOUS_DISEASE", dto.getSceneCode());
        assertEquals(100, dto.getPriority());
    }

    @Test
    public void testConditionDTO() {
        RuleExportDTO.ConditionDTO condition = new RuleExportDTO.ConditionDTO();
        condition.setFieldCode("lab_hiv_antibody");
        condition.setOperator("EQ");
        condition.setFieldValue("阳性");
        condition.setValueType("CONSTANT");
        condition.setGroupId(1);
        condition.setGroupLogic("AND");
        condition.setSortOrder(1);

        assertEquals("lab_hiv_antibody", condition.getFieldCode());
        assertEquals("EQ", condition.getOperator());
        assertEquals("阳性", condition.getFieldValue());
    }

    @Test
    public void testActionDTO() {
        RuleExportDTO.ActionDTO action = new RuleExportDTO.ActionDTO();
        action.setActionCode("HIV_REPORT");
        action.setActionType("RETURN");
        action.setActionParams("{\"message\":\"需要上报\"}");
        action.setSortOrder(1);

        assertEquals("HIV_REPORT", action.getActionCode());
        assertEquals("RETURN", action.getActionType());
    }

    @Test
    public void testRuleExportDTOWithConditionsAndActions() {
        RuleExportDTO dto = new RuleExportDTO();
        dto.setRuleCode("TEST_FULL_RULE");
        dto.setRuleName("完整测试规则");

        // 添加条件
        List<RuleExportDTO.ConditionDTO> conditions = new ArrayList<>();
        RuleExportDTO.ConditionDTO condition1 = new RuleExportDTO.ConditionDTO();
        condition1.setFieldCode("field1");
        condition1.setOperator("EQ");
        condition1.setFieldValue("value1");
        conditions.add(condition1);

        RuleExportDTO.ConditionDTO condition2 = new RuleExportDTO.ConditionDTO();
        condition2.setFieldCode("field2");
        condition2.setOperator("GT");
        condition2.setFieldValue("10");
        conditions.add(condition2);

        dto.setConditions(conditions);

        // 添加动作
        List<RuleExportDTO.ActionDTO> actions = new ArrayList<>();
        RuleExportDTO.ActionDTO action = new RuleExportDTO.ActionDTO();
        action.setActionCode("ACTION1");
        action.setActionType("RETURN");
        actions.add(action);

        dto.setActions(actions);

        assertNotNull(dto.getConditions());
        assertEquals(2, dto.getConditions().size());
        assertNotNull(dto.getActions());
        assertEquals(1, dto.getActions().size());
    }
}
