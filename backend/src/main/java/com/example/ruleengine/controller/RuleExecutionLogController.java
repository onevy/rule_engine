package com.example.ruleengine.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ruleengine.common.result.PageResult;
import com.example.ruleengine.common.result.Result;
import com.example.ruleengine.entity.RuleExecutionLog;
import com.example.ruleengine.service.RuleExecutionLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 规则执行日志 Controller
 */
@Tag(name = "规则执行日志")
@RestController
@RequestMapping("/rule-engine/log")
@RequiredArgsConstructor
public class RuleExecutionLogController {

    private final RuleExecutionLogService ruleExecutionLogService;

    @Operation(summary = "查询执行日志列表")
    @GetMapping("/list")
    public Result<PageResult<RuleExecutionLog>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "场景编码") @RequestParam(required = false) String sceneCode,
            @Parameter(description = "业务ID") @RequestParam(required = false) String businessId,
            @Parameter(description = "追踪ID") @RequestParam(required = false) String traceId,
            @Parameter(description = "执行结果(HIT/MISS/ERROR)") @RequestParam(required = false) String executionResult,
            @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        Page<RuleExecutionLog> pageResult = ruleExecutionLogService.pageList(page, pageSize, sceneCode, businessId, traceId, executionResult, startTime, endTime);
        return Result.success(PageResult.of(pageResult));
    }

    @Operation(summary = "查询日志详情")
    @GetMapping("/{traceId}")
    public Result<List<RuleExecutionLog>> getByTraceId(@PathVariable String traceId) {
        List<RuleExecutionLog> logs = ruleExecutionLogService.listByTraceId(traceId);
        return Result.success(logs);
    }

    @Operation(summary = "根据业务ID查询日志")
    @GetMapping("/business/{businessId}")
    public Result<List<RuleExecutionLog>> getByBusinessId(@PathVariable String businessId) {
        List<RuleExecutionLog> logs = ruleExecutionLogService.listByBusinessId(businessId);
        return Result.success(logs);
    }

    @Operation(summary = "根据场景编码查询日志")
    @GetMapping("/scene/{sceneCode}")
    public Result<List<RuleExecutionLog>> getBySceneCode(@PathVariable String sceneCode) {
        List<RuleExecutionLog> logs = ruleExecutionLogService.listBySceneCode(sceneCode);
        return Result.success(logs);
    }

    @Operation(summary = "获取执行统计信息")
    @GetMapping("/statistics")
    public Result<Object> getStatistics(
            @Parameter(description = "场景编码") @RequestParam(required = false) String sceneCode,
            @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        Object statistics = ruleExecutionLogService.getExecutionStatistics(sceneCode, startTime, endTime);
        return Result.success(statistics);
    }

    @Operation(summary = "删除过期日志")
    @DeleteMapping("/expired")
    public Result<Integer> deleteExpired(
            @Parameter(description = "过期时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime expireTime) {
        int count = ruleExecutionLogService.deleteExpiredLogs(expireTime);
        return Result.success(count);
    }
}
