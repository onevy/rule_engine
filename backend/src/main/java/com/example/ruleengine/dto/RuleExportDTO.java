package com.example.ruleengine.dto;

import com.example.ruleengine.entity.RuleAction;
import com.example.ruleengine.entity.RuleCondition;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 规则导出DTO
 */
@Data
public class RuleExportDTO {

    /**
     * 规则编码
     */
    private String ruleCode;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 规则描述
     */
    private String ruleDesc;

    /**
     * 所属场景编码
     */
    private String sceneCode;

    /**
     * 规则类型
     */
    private String ruleType;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 生效开始时间
     */
    private LocalDateTime effectiveStartTime;

    /**
     * 生效结束时间
     */
    private LocalDateTime effectiveEndTime;

    /**
     * 规则条件列表
     */
    private List<ConditionDTO> conditions;

    /**
     * 规则动作列表
     */
    private List<ActionDTO> actions;

    /**
     * 条件DTO
     */
    @Data
    public static class ConditionDTO {
        private String fieldCode;
        private String operator;
        private String fieldValue;
        private String valueType;
        private Integer groupId;
        private String groupLogic;
        private Integer sortOrder;
    }

    /**
     * 动作DTO
     */
    @Data
    public static class ActionDTO {
        private String actionCode;
        private String actionType;
        private String actionParams;
        private Integer sortOrder;
    }
}
