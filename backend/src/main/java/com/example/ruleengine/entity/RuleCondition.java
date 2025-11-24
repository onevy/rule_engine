package com.example.ruleengine.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.ruleengine.config.ArrayToJsonStringDeserializer;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
    @JsonDeserialize(using = ArrayToJsonStringDeserializer.class)
    private String fieldValue;

    /**
     * 值类型: CONSTANT, FIELD, ARRAY
     */
    private String valueType;

    /**
     * 聚合函数: SUM, AVG, MAX, MIN, COUNT
     */
    private String aggregateFunction;

    /**
     * 聚合字段列表，JSON数组格式，如 ["q1","q2","q3"]
     */
    private String aggregateFields;

    /**
     * 计算表达式，用于 EXPRESSION 操作符，支持 SpEL 语法
     */
    private String expression;

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
