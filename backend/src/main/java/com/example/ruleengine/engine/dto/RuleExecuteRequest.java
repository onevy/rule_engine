package com.example.ruleengine.engine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Map;

/**
 * 规则执行请求
 */
@Data
@Schema(description = "规则执行请求")
public class RuleExecuteRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "场景编码", required = true)
    @NotBlank(message = "场景编码不能为空")
    private String sceneCode;

    @Schema(description = "业务ID")
    private String businessId;

    @Schema(description = "输入数据", required = true)
    private Map<String, Object> inputData;
}
