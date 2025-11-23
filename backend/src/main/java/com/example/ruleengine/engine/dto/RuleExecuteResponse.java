package com.example.ruleengine.engine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 规则执行响应
 */
@Data
@Schema(description = "规则执行响应")
public class RuleExecuteResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "是否匹配到规则")
    private boolean matched;

    @Schema(description = "匹配的规则编码列表")
    private List<String> matchedRuleCodes;

    @Schema(description = "输出数据")
    private Map<String, Object> outputData;

    @Schema(description = "执行时间（毫秒）")
    private long executionTime;

    @Schema(description = "追踪ID")
    private String traceId;

    @Schema(description = "错误信息")
    private String errorMessage;
}
