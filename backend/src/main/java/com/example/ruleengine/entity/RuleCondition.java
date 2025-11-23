package com.example.ruleengine.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 规则条件实体
 */
@Data
@TableName("rule_condition")
public class RuleCondition implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属规则ID
     */
    private Long ruleId;

    /**
     * 所属条件组ID
     */
    private Long groupId;

    /**
     * 字段编码
     */
    @JsonAlias("field")
    private String fieldCode;

    /**
     * 操作符: EQ, IN, GT, LT等
     */
    private String operator;

    /**
     * 字段值(JSON格式)
     */
    @JsonAlias("value")
    private String fieldValue;

    /**
     * 值类型: CONSTANT, FIELD, ARRAY
     */
    private String valueType;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 条件组逻辑（非数据库字段，仅用于接收前端数据）
     */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String groupLogic;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
