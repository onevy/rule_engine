package com.example.ruleengine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评估分组映射实体
 * 用于多维度体质评估等分组计算场景
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("assessment_group_mapping")
public class AssessmentGroupMapping extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 所属场景编码
     */
    private String sceneCode;

    /**
     * 分组编码，如 qixu, yangxu
     */
    private String groupCode;

    /**
     * 分组名称，如 气虚质
     */
    private String groupName;

    /**
     * 题目编号列表，JSON数组格式，如 [2,3,4,14]
     */
    private String itemList;

    /**
     * 需要反向计分的题目编号，JSON数组格式
     */
    private String reverseItems;

    /**
     * 排序号
     */
    private Integer sortOrder;
}
