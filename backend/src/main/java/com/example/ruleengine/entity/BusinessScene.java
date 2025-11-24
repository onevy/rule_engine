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
     * 评估题目字段命名模式，如 q{n} 表示 q1,q2,...qN
     */
    private String itemPattern;

    /**
     * 评估题目数量，0表示动态数量（自动识别）
     */
    private Integer itemCount;

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;

    /**
     * 排序号
     */
    private Integer sortOrder;
}
