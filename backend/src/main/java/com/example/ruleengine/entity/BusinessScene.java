package com.example.ruleengine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业务场景实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("business_scene")
public class BusinessScene extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 场景编码
     */
    private String sceneCode;

    /**
     * 场景名称
     */
    private String sceneName;

    /**
     * 场景描述
     */
    private String sceneDesc;

    /**
     * 适配器类全限定名
     */
    private String adapterClass;

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;

    /**
     * 排序号
     */
    private Integer sortOrder;
}
