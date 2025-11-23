package com.example.ruleengine.engine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

/**
 * 规则测试请求
 */
@Data
@Schema(description = "规则测试请求")
public class RuleTestRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "规则ID", required = true)
    @NotNull(message = "规则ID不能为空")
    private Long ruleId;

    @Schema(description = "输入数据", required = true)
    private Map<String, Object> inputData;
}
