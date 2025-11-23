package com.example.ruleengine.dto;

import lombok.Data;

import java.util.List;

/**
 * 规则导入请求
 */
@Data
public class RuleImportRequest {

    /**
     * 要导入的规则列表
     */
    private List<RuleExportDTO> rules;

    /**
     * 冲突处理策略: SKIP-跳过, OVERWRITE-覆盖, RENAME-重命名
     */
    private String conflictStrategy = "SKIP";

    /**
     * 是否导入后自动启用
     */
    private Boolean enableAfterImport = false;
}
