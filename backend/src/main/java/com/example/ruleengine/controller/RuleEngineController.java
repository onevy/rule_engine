package com.example.ruleengine.controller;

import com.example.ruleengine.common.result.Result;
import com.example.ruleengine.engine.RuleContext;
import com.example.ruleengine.engine.RuleEngine;
import com.example.ruleengine.engine.RuleResult;
import com.example.ruleengine.engine.RuleValidator;
import com.example.ruleengine.engine.dto.RuleExecuteRequest;
import com.example.ruleengine.engine.dto.RuleExecuteResponse;
import com.example.ruleengine.engine.dto.RuleTestRequest;
import com.example.ruleengine.entity.RuleDefinition;
import com.example.ruleengine.service.RuleDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 规则引擎执行 Controller
 */
@Tag(name = "规则引擎执行")
@RestController
@RequestMapping("/rule-engine")
@RequiredArgsConstructor
@Slf4j
public class RuleEngineController {

    private final RuleEngine ruleEngine;
    private final RuleDefinitionService ruleDefinitionService;
    private final RuleValidator ruleValidator;

    @Operation(summary = "执行规则")
    @PostMapping("/execute")
    public Result<RuleExecuteResponse> execute(@Valid @RequestBody RuleExecuteRequest request) {
        log.info("执行规则 - 场景: {}, 业务ID: {}", request.getSceneCode(), request.getBusinessId());

        // 构建执行上下文
        RuleContext context = new RuleContext();
        context.setSceneCode(request.getSceneCode());
        context.setBusinessId(request.getBusinessId());
        context.setTraceId(UUID.randomUUID().toString());
        context.setInputData(request.getInputData());

        // 执行规则
        RuleResult result = ruleEngine.execute(context);

        // 构建响应
        RuleExecuteResponse response = new RuleExecuteResponse();
        response.setMatched(result.isMatched());
        response.setMatchedRuleCodes(result.getMatchedRuleCodes());
        response.setOutputData(result.getOutputData());
        response.setExecutionTime(result.getExecutionTime());
        response.setTraceId(context.getTraceId());
        response.setErrorMessage(result.getErrorMessage());

        return Result.success(response);
    }

    @Operation(summary = "批量执行规则")
    @PostMapping("/batch-execute")
    public Result<List<RuleExecuteResponse>> batchExecute(
            @Parameter(description = "场景编码") @RequestParam String sceneCode,
            @RequestBody List<Map<String, Object>> inputDataList) {
        log.info("批量执行规则 - 场景: {}, 数据量: {}", sceneCode, inputDataList.size());

        // 构建执行上下文列表
        List<RuleContext> contexts = inputDataList.stream()
                .map(data -> {
                    RuleContext context = new RuleContext();
                    context.setSceneCode(sceneCode);
                    context.setTraceId(UUID.randomUUID().toString());
                    context.setInputData(data);
                    return context;
                })
                .collect(Collectors.toList());

        // 批量执行规则
        List<RuleResult> results = ruleEngine.batchExecute(contexts);

        // 构建响应列表
        List<RuleExecuteResponse> responses = results.stream()
                .map(result -> {
                    RuleExecuteResponse response = new RuleExecuteResponse();
                    response.setMatched(result.isMatched());
                    response.setMatchedRuleCodes(result.getMatchedRuleCodes());
                    response.setOutputData(result.getOutputData());
                    response.setExecutionTime(result.getExecutionTime());
                    response.setErrorMessage(result.getErrorMessage());
                    return response;
                })
                .collect(Collectors.toList());

        return Result.success(responses);
    }

    @Operation(summary = "测试规则")
    @PostMapping("/test")
    public Result<RuleExecuteResponse> test(@Valid @RequestBody RuleTestRequest request) {
        log.info("测试规则 - 规则ID: {}", request.getRuleId());

        // 获取规则定义
        RuleDefinition rule = ruleDefinitionService.getById(request.getRuleId());
        if (rule == null) {
            return Result.fail("规则不存在");
        }

        // 构建执行上下文
        RuleContext context = new RuleContext();
        context.setSceneCode(rule.getSceneCode());
        context.setTraceId(UUID.randomUUID().toString());
        context.setInputData(request.getInputData());

        // 执行规则
        RuleResult result = ruleEngine.execute(context);

        // 构建响应
        RuleExecuteResponse response = new RuleExecuteResponse();
        response.setMatched(result.isMatched());
        response.setMatchedRuleCodes(result.getMatchedRuleCodes());
        response.setOutputData(result.getOutputData());
        response.setExecutionTime(result.getExecutionTime());
        response.setTraceId(context.getTraceId());
        response.setErrorMessage(result.getErrorMessage());

        return Result.success(response);
    }

    @Operation(summary = "重载规则缓存")
    @PostMapping("/reload/{sceneCode}")
    public Result<Void> reload(@PathVariable String sceneCode) {
        log.info("重载规则缓存 - 场景: {}", sceneCode);
        ruleEngine.reload(sceneCode);
        return Result.success();
    }

    @Operation(summary = "重载所有规则缓存")
    @PostMapping("/reload-all")
    public Result<Void> reloadAll() {
        log.info("重载所有规则缓存");
        ruleEngine.reloadAll();
        return Result.success();
    }

    @Operation(summary = "清除规则缓存")
    @DeleteMapping("/cache/{sceneCode}")
    public Result<Void> clearCache(@PathVariable String sceneCode) {
        log.info("清除规则缓存 - 场景: {}", sceneCode);
        ruleEngine.clearCache(sceneCode);
        return Result.success();
    }

    @Operation(summary = "清除所有规则缓存")
    @DeleteMapping("/cache")
    public Result<Void> clearAllCache() {
        log.info("清除所有规则缓存");
        ruleEngine.clearAllCache();
        return Result.success();
    }

    @Operation(summary = "验证规则语法")
    @PostMapping("/validate")
    public Result<Map<String, Object>> validate(@RequestBody String ruleContent) {
        log.info("验证规则语法");

        String error = ruleEngine.validate(ruleContent);
        boolean valid = (error == null);

        return Result.success(Map.of(
                "valid", valid,
                "error", error != null ? error : ""
        ));
    }

    @Operation(summary = "验证规则定义")
    @PostMapping("/validate-rule")
    public Result<Map<String, Object>> validateRule(@RequestBody RuleDefinition rule) {
        log.info("验证规则定义: {}", rule.getRuleCode());

        List<String> errors = ruleValidator.validate(rule);
        boolean valid = errors.isEmpty();

        return Result.success(Map.of(
                "valid", valid,
                "errors", errors
        ));
    }
}
