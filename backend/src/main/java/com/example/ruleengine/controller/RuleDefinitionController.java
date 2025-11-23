package com.example.ruleengine.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ruleengine.common.result.PageResult;
import com.example.ruleengine.common.result.Result;
import com.example.ruleengine.dto.RuleExportDTO;
import com.example.ruleengine.dto.RuleImportRequest;
import com.example.ruleengine.dto.RuleImportResult;
import com.example.ruleengine.entity.RuleDefinition;
import com.example.ruleengine.service.RuleDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 规则定义管理 Controller
 */
@Tag(name = "规则定义管理")
@RestController
@RequestMapping("/rule-engine/rule")
@RequiredArgsConstructor
public class RuleDefinitionController {

    private final RuleDefinitionService ruleDefinitionService;

    @Operation(summary = "查询规则列表")
    @GetMapping("/list")
    public Result<PageResult<RuleDefinition>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "场景编码") @RequestParam(required = false) String sceneCode,
            @Parameter(description = "规则名称") @RequestParam(required = false) String ruleName,
            @Parameter(description = "规则编码") @RequestParam(required = false) String ruleCode,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "规则组ID") @RequestParam(required = false) Long groupId) {
        Page<RuleDefinition> pageResult = ruleDefinitionService.pageList(page, pageSize, sceneCode, ruleName, ruleCode, status, groupId);
        return Result.success(PageResult.of(pageResult));
    }

    @Operation(summary = "查询规则详情")
    @GetMapping("/{ruleId}")
    public Result<RuleDefinition> getById(@PathVariable Long ruleId) {
        RuleDefinition rule = ruleDefinitionService.getRuleDetail(ruleId);
        return Result.success(rule);
    }

    @Operation(summary = "根据规则编码查询")
    @GetMapping("/code/{ruleCode}")
    public Result<RuleDefinition> getByCode(@PathVariable String ruleCode) {
        RuleDefinition rule = ruleDefinitionService.getByRuleCode(ruleCode);
        return Result.success(rule);
    }

    @Operation(summary = "创建规则")
    @PostMapping("/create")
    public Result<Long> create(@RequestBody RuleDefinition rule) {
        Long id = ruleDefinitionService.createRule(rule);
        return Result.success(id);
    }

    @Operation(summary = "更新规则")
    @PutMapping("/update/{ruleId}")
    public Result<Void> update(@PathVariable Long ruleId, @RequestBody RuleDefinition rule) {
        rule.setId(ruleId);
        ruleDefinitionService.updateRule(rule);
        return Result.success();
    }

    @Operation(summary = "删除规则")
    @DeleteMapping("/delete/{ruleId}")
    public Result<Void> delete(@PathVariable Long ruleId) {
        ruleDefinitionService.deleteRule(ruleId);
        return Result.success();
    }

    @Operation(summary = "复制规则")
    @PostMapping("/copy/{ruleId}")
    public Result<Long> copy(@PathVariable Long ruleId) {
        Long newRuleId = ruleDefinitionService.copyRule(ruleId);
        return Result.success(newRuleId);
    }

    @Operation(summary = "更新规则状态（启用/禁用）")
    @PutMapping("/status/{ruleId}")
    public Result<Void> updateStatus(@PathVariable Long ruleId, @RequestParam Integer status) {
        ruleDefinitionService.updateStatus(ruleId, status);
        return Result.success();
    }

    @Operation(summary = "根据场景编码查询规则列表")
    @GetMapping("/scene/{sceneCode}")
    public Result<List<RuleDefinition>> listByScene(@PathVariable String sceneCode) {
        List<RuleDefinition> rules = ruleDefinitionService.listBySceneCode(sceneCode);
        return Result.success(rules);
    }

    @Operation(summary = "根据规则组ID查询规则列表")
    @GetMapping("/group/{groupId}")
    public Result<List<RuleDefinition>> listByGroup(@PathVariable Long groupId) {
        List<RuleDefinition> rules = ruleDefinitionService.listByGroupId(groupId);
        return Result.success(rules);
    }

    @Operation(summary = "导出规则")
    @PostMapping("/export")
    public Result<List<RuleExportDTO>> exportRules(
            @Parameter(description = "规则ID列表") @RequestBody(required = false) List<Long> ruleIds,
            @Parameter(description = "场景编码") @RequestParam(required = false) String sceneCode) {
        List<RuleExportDTO> exportData = ruleDefinitionService.exportRules(ruleIds, sceneCode);
        return Result.success(exportData);
    }

    @Operation(summary = "导入规则")
    @PostMapping("/import")
    public Result<RuleImportResult> importRules(@RequestBody RuleImportRequest request) {
        RuleImportResult result = ruleDefinitionService.importRules(request);
        return Result.success(result);
    }

    @Operation(summary = "批量删除规则")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ruleIds) {
        ruleDefinitionService.batchDelete(ruleIds);
        return Result.success();
    }

    @Operation(summary = "批量更新规则状态")
    @PutMapping("/batch/status")
    public Result<Void> batchUpdateStatus(
            @RequestBody List<Long> ruleIds,
            @RequestParam Integer status) {
        ruleDefinitionService.batchUpdateStatus(ruleIds, status);
        return Result.success();
    }
}
