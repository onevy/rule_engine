package com.example.ruleengine.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 规则动作实体
 */
@Data
@TableName("rule_action")
public class RuleAction implements Serializable {

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
     * 动作类型: RETURN, CALL_SERVICE, SEND_MESSAGE
     */
    private String actionType;

    /**
     * 动作编码
     */
    private String actionCode;

    /**
     * 动作参数(JSON格式)
     */
    private String actionParams;

    /**
     * 执行顺序
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
