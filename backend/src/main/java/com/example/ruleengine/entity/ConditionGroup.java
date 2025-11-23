package com.example.ruleengine.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 条件组实体
 */
@Data
@TableName("condition_group")
public class ConditionGroup implements Serializable {

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
     * 父条件组ID, 0表示根节点
     */
    private Long parentGroupId;

    /**
     * 组内逻辑: AND, OR
     */
    private String groupLogic;

    /**
     * 嵌套层级
     */
    private Integer groupLevel;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
