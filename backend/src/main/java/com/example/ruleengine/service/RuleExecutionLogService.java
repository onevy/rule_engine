package com.example.ruleengine.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ruleengine.entity.RuleExecutionLog;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 规则执行日志 Service 接口
 */
public interface RuleExecutionLogService extends IService<RuleExecutionLog> {

    /**
     * 根据追踪ID查询日志
     */
    List<RuleExecutionLog> listByTraceId(String traceId);

    /**
     * 根据场景编码查询日志
     */
    List<RuleExecutionLog> listBySceneCode(String sceneCode);

    /**
     * 根据业务ID查询日志
     */
    List<RuleExecutionLog> listByBusinessId(String businessId);

    /**
     * 分页查询
     */
    Page<RuleExecutionLog> pageList(Integer page, Integer pageSize, String sceneCode,
                                     String businessId, String traceId, String executionResult,
                                     LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 记录执行日志
     */
    Long logExecution(RuleExecutionLog log);

    /**
     * 批量记录执行日志
     */
    void batchLogExecution(List<RuleExecutionLog> logs);

    /**
     * 删除过期日志
     */
    int deleteExpiredLogs(LocalDateTime expireTime);

    /**
     * 统计执行情况
     */
    Object getExecutionStatistics(String sceneCode, LocalDateTime startTime, LocalDateTime endTime);
}
