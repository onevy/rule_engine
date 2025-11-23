package com.example.ruleengine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ruleengine.entity.RuleExecutionLog;
import com.example.ruleengine.mapper.RuleExecutionLogMapper;
import com.example.ruleengine.service.RuleExecutionLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 规则执行日志 Service 实现类
 */
@Service
public class RuleExecutionLogServiceImpl extends ServiceImpl<RuleExecutionLogMapper, RuleExecutionLog> implements RuleExecutionLogService {

    @Override
    public List<RuleExecutionLog> listByTraceId(String traceId) {
        return baseMapper.selectByTraceId(traceId);
    }

    @Override
    public List<RuleExecutionLog> listBySceneCode(String sceneCode) {
        return baseMapper.selectBySceneCode(sceneCode);
    }

    @Override
    public List<RuleExecutionLog> listByBusinessId(String businessId) {
        return baseMapper.selectByBusinessId(businessId);
    }

    @Override
    public Page<RuleExecutionLog> pageList(Integer page, Integer pageSize, String sceneCode,
                                            String businessId, String traceId, String executionResult,
                                            LocalDateTime startTime, LocalDateTime endTime) {
        Page<RuleExecutionLog> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<RuleExecutionLog> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(sceneCode)) {
            wrapper.eq(RuleExecutionLog::getSceneCode, sceneCode);
        }
        if (StringUtils.hasText(businessId)) {
            wrapper.eq(RuleExecutionLog::getBusinessId, businessId);
        }
        if (StringUtils.hasText(traceId)) {
            wrapper.eq(RuleExecutionLog::getTraceId, traceId);
        }
        if (StringUtils.hasText(executionResult)) {
            wrapper.eq(RuleExecutionLog::getExecutionResult, executionResult);
        }
        if (startTime != null) {
            wrapper.ge(RuleExecutionLog::getExecuteTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(RuleExecutionLog::getExecuteTime, endTime);
        }
        wrapper.orderByDesc(RuleExecutionLog::getExecuteTime);

        return baseMapper.selectPage(pageParam, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long logExecution(RuleExecutionLog log) {
        baseMapper.insert(log);
        return log.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchLogExecution(List<RuleExecutionLog> logs) {
        for (RuleExecutionLog log : logs) {
            baseMapper.insert(log);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteExpiredLogs(LocalDateTime expireTime) {
        LambdaQueryWrapper<RuleExecutionLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(RuleExecutionLog::getExecuteTime, expireTime);
        return baseMapper.delete(wrapper);
    }

    @Override
    public Object getExecutionStatistics(String sceneCode, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<RuleExecutionLog> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(sceneCode)) {
            wrapper.eq(RuleExecutionLog::getSceneCode, sceneCode);
        }
        if (startTime != null) {
            wrapper.ge(RuleExecutionLog::getExecuteTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(RuleExecutionLog::getExecuteTime, endTime);
        }

        List<RuleExecutionLog> logs = baseMapper.selectList(wrapper);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("total", logs.size());
        statistics.put("hit", logs.stream().filter(l -> "HIT".equals(l.getExecutionResult())).count());
        statistics.put("miss", logs.stream().filter(l -> "MISS".equals(l.getExecutionResult())).count());
        statistics.put("error", logs.stream().filter(l -> "ERROR".equals(l.getExecutionResult())).count());

        if (!logs.isEmpty()) {
            double avgExecutionTime = logs.stream()
                .filter(l -> l.getExecutionTime() != null)
                .mapToInt(RuleExecutionLog::getExecutionTime)
                .average()
                .orElse(0);
            statistics.put("avgExecutionTime", avgExecutionTime);

            int maxExecutionTime = logs.stream()
                .filter(l -> l.getExecutionTime() != null)
                .mapToInt(RuleExecutionLog::getExecutionTime)
                .max()
                .orElse(0);
            statistics.put("maxExecutionTime", maxExecutionTime);

            int minExecutionTime = logs.stream()
                .filter(l -> l.getExecutionTime() != null)
                .mapToInt(RuleExecutionLog::getExecutionTime)
                .min()
                .orElse(0);
            statistics.put("minExecutionTime", minExecutionTime);
        }

        return statistics;
    }
}
