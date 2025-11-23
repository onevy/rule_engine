package com.example.ruleengine.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ruleengine.common.result.PageResult;
import com.example.ruleengine.common.result.Result;
import com.example.ruleengine.entity.RuleGroup;
import com.example.ruleengine.service.RuleGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 规则组管理 Controller
 */
@Tag(name = "规则组管理")
@RestController
@RequestMapping("/rule-engine/group")
@RequiredArgsConstructor
public class RuleGroupController {

    private final RuleGroupService ruleGroupService;

    @Operation(summary = "获取规则组列表")
    @GetMapping("/list")
    public Result<List<RuleGroup>> list(
            @Parameter(description = "场景编码") @RequestParam(required = false) String sceneCode) {
        List<RuleGroup> groups = ruleGroupService.listBySceneCode(sceneCode);
        return Result.success(groups);
    }

    @Operation(summary = "获取规则组树形结构")
    @GetMapping("/tree")
    public Result<List<RuleGroup>> tree(
            @Parameter(description = "场景编码") @RequestParam String sceneCode) {
        List<RuleGroup> tree = ruleGroupService.getGroupTree(sceneCode);
        return Result.success(tree);
    }

    @Operation(summary = "分页查询规则组")
    @GetMapping("/page")
    public Result<PageResult<RuleGroup>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "场景编码") @RequestParam(required = false) String sceneCode,
            @Parameter(description = "规则组名称") @RequestParam(required = false) String groupName) {
        Page<RuleGroup> pageResult = ruleGroupService.pageList(page, pageSize, sceneCode, groupName);
        return Result.success(PageResult.of(pageResult));
    }

    @Operation(summary = "获取规则组详情")
    @GetMapping("/{id}")
    public Result<RuleGroup> getById(@PathVariable Long id) {
        RuleGroup group = ruleGroupService.getById(id);
        return Result.success(group);
    }

    @Operation(summary = "根据编码获取规则组")
    @GetMapping("/code/{groupCode}")
    public Result<RuleGroup> getByCode(@PathVariable String groupCode) {
        RuleGroup group = ruleGroupService.getByGroupCode(groupCode);
        return Result.success(group);
    }

    @Operation(summary = "创建规则组")
    @PostMapping("/create")
    public Result<Long> create(@RequestBody RuleGroup group) {
        Long id = ruleGroupService.createGroup(group);
        return Result.success(id);
    }

    @Operation(summary = "更新规则组")
    @PutMapping("/update/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody RuleGroup group) {
        group.setId(id);
        ruleGroupService.updateGroup(group);
        return Result.success();
    }

    @Operation(summary = "删除规则组")
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        ruleGroupService.deleteGroup(id);
        return Result.success();
    }

    @Operation(summary = "更新规则组状态")
    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        ruleGroupService.updateStatus(id, status);
        return Result.success();
    }
}
