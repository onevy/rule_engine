package com.example.ruleengine.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 规则定义实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("rule_definition")
public class RuleDefinition extends BaseEntity {

    private static final long serialVersionUID = 1L;

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
     * 所属规则组ID
     */
    private Long groupId;

    /**
     * 规则类型: CONDITION, DECISION_TABLE, SCRIPT
     */
    private String ruleType;

    /**
     * 优先级: 数值越大优先级越高
     */
    private Integer priority;

    /**
     * 状态: 0-禁用, 1-启用, 2-草稿
     */
    private Integer status;

    /**
     * 生效开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime effectiveStartTime;

    /**
     * 生效结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime effectiveEndTime;

    /**
     * 编译后的DRL规则内容
     */
    private String drlContent;

    /**
     * JSON格式的规则配置
     */
    private String jsonConfig;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 规则条件列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<RuleCondition> conditions;

    /**
     * 规则动作列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<RuleAction> actions;
}
