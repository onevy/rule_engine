package com.example.ruleengine.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 规则执行日志实体
 */
@Data
@TableName("rule_execution_log")
public class RuleExecutionLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 链路追踪ID
     */
    private String traceId;

    /**
     * 规则ID
     */
    private Long ruleId;

    /**
     * 规则编码
     */
    private String ruleCode;

    /**
     * 场景编码
     */
    private String sceneCode;

    /**
     * 业务ID
     */
    private String businessId;

    /**
     * 输入数据(JSON格式)
     */
    private String inputData;

    /**
     * 输出数据(JSON格式)
     */
    private String outputData;

    /**
     * 执行结果: HIT, MISS, ERROR
     */
    private String executionResult;

    /**
     * 命中的规则列表(JSON格式)
     */
    private String hitRules;

    /**
     * 执行耗时(毫秒)
     */
    private Integer executionTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 执行人
     */
    private String executeBy;

    /**
     * 执行时间
     */
    private LocalDateTime executeTime;

    /**
     * 服务器IP
     */
    private String serverIp;
}
