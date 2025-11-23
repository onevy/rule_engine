package com.example.ruleengine.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 规则版本实体
 */
@Data
@TableName("rule_version")
public class RuleVersion implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 规则ID
     */
    private Long ruleId;

    /**
     * 版本号
     */
    private Integer versionNo;

    /**
     * 规则内容快照(JSON格式)
     */
    private String ruleContent;

    /**
     * 变更说明
     */
    private String changeLog;

    /**
     * 是否当前版本: 0-否, 1-是
     */
    private Integer isCurrent;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
