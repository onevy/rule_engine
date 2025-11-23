package com.example.ruleengine.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ruleengine.common.result.PageResult;
import com.example.ruleengine.common.result.Result;
import com.example.ruleengine.entity.RuleVersion;
import com.example.ruleengine.service.RuleVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 规则版本管理 Controller
 */
@Tag(name = "规则版本管理")
@RestController
@RequestMapping("/rule-engine/version")
@RequiredArgsConstructor
public class RuleVersionController {

    private final RuleVersionService ruleVersionService;

    @Operation(summary = "查询版本列表")
    @GetMapping("/list/{ruleId}")
    public Result<List<RuleVersion>> list(@PathVariable Long ruleId) {
        List<RuleVersion> versions = ruleVersionService.listByRuleId(ruleId);
        return Result.success(versions);
    }

    @Operation(summary = "分页查询版本列表")
    @GetMapping("/page")
    public Result<PageResult<RuleVersion>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "规则ID") @RequestParam Long ruleId) {
        Page<RuleVersion> pageResult = ruleVersionService.pageList(page, pageSize, ruleId);
        return Result.success(PageResult.of(pageResult));
    }

    @Operation(summary = "获取当前版本")
    @GetMapping("/current/{ruleId}")
    public Result<RuleVersion> getCurrentVersion(@PathVariable Long ruleId) {
        RuleVersion version = ruleVersionService.getCurrentVersion(ruleId);
        return Result.success(version);
    }

    @Operation(summary = "获取指定版本")
    @GetMapping("/{ruleId}/{versionNo}")
    public Result<RuleVersion> getVersion(
            @PathVariable Long ruleId,
            @PathVariable Integer versionNo) {
        RuleVersion version = ruleVersionService.getByRuleIdAndVersionNo(ruleId, versionNo);
        return Result.success(version);
    }

    @Operation(summary = "版本回滚")
    @PostMapping("/rollback")
    public Result<Void> rollback(
            @Parameter(description = "规则ID") @RequestParam Long ruleId,
            @Parameter(description = "目标版本号") @RequestParam Integer versionNo) {
        ruleVersionService.rollback(ruleId, versionNo);
        return Result.success();
    }

    @Operation(summary = "版本对比")
    @GetMapping("/compare")
    public Result<Object> compare(
            @Parameter(description = "规则ID") @RequestParam Long ruleId,
            @Parameter(description = "版本号1") @RequestParam Integer versionNo1,
            @Parameter(description = "版本号2") @RequestParam Integer versionNo2) {
        Object comparison = ruleVersionService.compareVersions(ruleId, versionNo1, versionNo2);
        return Result.success(comparison);
    }
}
