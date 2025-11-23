package com.example.ruleengine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字段元数据实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("field_metadata")
public class FieldMetadata extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 所属场景编码
     */
    private String sceneCode;

    /**
     * 字段编码
     */
    private String fieldCode;

    /**
     * 字段中文名称
     */
    private String fieldName;

    /**
     * 字段类型: String, Number, Date, Boolean
     */
    private String fieldType;

    /**
     * 数据来源说明
     */
    private String dataSource;

    /**
     * 支持的操作符,逗号分隔
     */
    private String supportedOperators;

    /**
     * 值域说明
     */
    private String valueRange;

    /**
     * 字段分类
     */
    private String category;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 是否必填: 0-否, 1-是
     */
    private Integer isRequired;
}
