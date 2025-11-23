package com.example.ruleengine.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 规则导入结果
 */
@Data
public class RuleImportResult {

    /**
     * 总数
     */
    private int total;

    /**
     * 成功数
     */
    private int successCount;

    /**
     * 跳过数
     */
    private int skipCount;

    /**
     * 失败数
     */
    private int failCount;

    /**
     * 导入详情列表
     */
    private List<ImportDetail> details = new ArrayList<>();

    /**
     * 导入详情
     */
    @Data
    public static class ImportDetail {
        /**
         * 规则编码
         */
        private String ruleCode;

        /**
         * 规则名称
         */
        private String ruleName;

        /**
         * 状态: SUCCESS-成功, SKIP-跳过, FAIL-失败
         */
        private String status;

        /**
         * 消息
         */
        private String message;

        /**
         * 新规则ID（导入成功时）
         */
        private Long newRuleId;

        public static ImportDetail success(String ruleCode, String ruleName, Long newRuleId) {
            ImportDetail detail = new ImportDetail();
            detail.setRuleCode(ruleCode);
            detail.setRuleName(ruleName);
            detail.setStatus("SUCCESS");
            detail.setMessage("导入成功");
            detail.setNewRuleId(newRuleId);
            return detail;
        }

        public static ImportDetail skip(String ruleCode, String ruleName, String reason) {
            ImportDetail detail = new ImportDetail();
            detail.setRuleCode(ruleCode);
            detail.setRuleName(ruleName);
            detail.setStatus("SKIP");
            detail.setMessage(reason);
            return detail;
        }

        public static ImportDetail fail(String ruleCode, String ruleName, String error) {
            ImportDetail detail = new ImportDetail();
            detail.setRuleCode(ruleCode);
            detail.setRuleName(ruleName);
            detail.setStatus("FAIL");
            detail.setMessage(error);
            return detail;
        }
    }
}
