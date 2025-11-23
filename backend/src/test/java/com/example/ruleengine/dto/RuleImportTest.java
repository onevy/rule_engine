package com.example.ruleengine.dto;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 规则导入相关DTO单元测试
 */
public class RuleImportTest {

    @Test
    public void testRuleImportRequestDefaults() {
        RuleImportRequest request = new RuleImportRequest();

        assertEquals("SKIP", request.getConflictStrategy());
        assertFalse(request.getEnableAfterImport());
    }

    @Test
    public void testRuleImportRequestWithRules() {
        RuleImportRequest request = new RuleImportRequest();

        List<RuleExportDTO> rules = new ArrayList<>();
        RuleExportDTO rule1 = new RuleExportDTO();
        rule1.setRuleCode("RULE_001");
        rule1.setRuleName("规则1");
        rules.add(rule1);

        RuleExportDTO rule2 = new RuleExportDTO();
        rule2.setRuleCode("RULE_002");
        rule2.setRuleName("规则2");
        rules.add(rule2);

        request.setRules(rules);
        request.setConflictStrategy("RENAME");
        request.setEnableAfterImport(true);

        assertEquals(2, request.getRules().size());
        assertEquals("RENAME", request.getConflictStrategy());
        assertTrue(request.getEnableAfterImport());
    }

    @Test
    public void testRuleImportResultSuccess() {
        RuleImportResult result = new RuleImportResult();
        result.setTotal(3);
        result.setSuccessCount(2);
        result.setSkipCount(1);
        result.setFailCount(0);

        assertEquals(3, result.getTotal());
        assertEquals(2, result.getSuccessCount());
        assertEquals(1, result.getSkipCount());
        assertEquals(0, result.getFailCount());
    }

    @Test
    public void testImportDetailSuccess() {
        RuleImportResult.ImportDetail detail = RuleImportResult.ImportDetail.success(
                "RULE_001", "规则1", 100L);

        assertEquals("RULE_001", detail.getRuleCode());
        assertEquals("规则1", detail.getRuleName());
        assertEquals("SUCCESS", detail.getStatus());
        assertEquals(100L, detail.getNewRuleId());
        assertEquals("导入成功", detail.getMessage());
    }

    @Test
    public void testImportDetailSkip() {
        RuleImportResult.ImportDetail detail = RuleImportResult.ImportDetail.skip(
                "RULE_002", "规则2", "规则编码已存在");

        assertEquals("RULE_002", detail.getRuleCode());
        assertEquals("规则2", detail.getRuleName());
        assertEquals("SKIP", detail.getStatus());
        assertEquals("规则编码已存在", detail.getMessage());
        assertNull(detail.getNewRuleId());
    }

    @Test
    public void testImportDetailFail() {
        RuleImportResult.ImportDetail detail = RuleImportResult.ImportDetail.fail(
                "RULE_003", "规则3", "导入失败：数据格式错误");

        assertEquals("RULE_003", detail.getRuleCode());
        assertEquals("规则3", detail.getRuleName());
        assertEquals("FAIL", detail.getStatus());
        assertEquals("导入失败：数据格式错误", detail.getMessage());
        assertNull(detail.getNewRuleId());
    }

    @Test
    public void testRuleImportResultWithDetails() {
        RuleImportResult result = new RuleImportResult();

        List<RuleImportResult.ImportDetail> details = new ArrayList<>();
        details.add(RuleImportResult.ImportDetail.success("R1", "规则1", 1L));
        details.add(RuleImportResult.ImportDetail.skip("R2", "规则2", "已存在"));
        details.add(RuleImportResult.ImportDetail.fail("R3", "规则3", "格式错误"));

        result.setDetails(details);
        result.setTotal(3);
        result.setSuccessCount(1);
        result.setSkipCount(1);
        result.setFailCount(1);

        assertEquals(3, result.getDetails().size());
        assertEquals("SUCCESS", result.getDetails().get(0).getStatus());
        assertEquals("SKIP", result.getDetails().get(1).getStatus());
        assertEquals("FAIL", result.getDetails().get(2).getStatus());
    }

    @Test
    public void testConflictStrategies() {
        RuleImportRequest request = new RuleImportRequest();

        // Test SKIP strategy
        request.setConflictStrategy("SKIP");
        assertEquals("SKIP", request.getConflictStrategy());

        // Test OVERWRITE strategy
        request.setConflictStrategy("OVERWRITE");
        assertEquals("OVERWRITE", request.getConflictStrategy());

        // Test RENAME strategy
        request.setConflictStrategy("RENAME");
        assertEquals("RENAME", request.getConflictStrategy());
    }
}
