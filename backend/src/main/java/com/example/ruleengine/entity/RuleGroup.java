package com.example.ruleengine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 规则组实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("rule_group")
public class RuleGroup extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 所属场景编码
     */
    private String sceneCode;

    /**
     * 规则组编码
     */
    private String groupCode;

    /**
     * 规则组名称
     */
    private String groupName;

    /**
     * 规则组描述
     */
    private String groupDesc;

    /**
     * 父级ID, 0表示根节点
     */
    private Long parentId;

    /**
     * 执行模式: ALL-全部执行, FIRST-首个匹配
     */
    private String executionMode;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;
}
