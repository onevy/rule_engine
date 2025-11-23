package com.example.ruleengine.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ruleengine.common.result.PageResult;
import com.example.ruleengine.common.result.Result;
import com.example.ruleengine.entity.BusinessScene;
import com.example.ruleengine.service.BusinessSceneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 业务场景管理 Controller
 */
@Tag(name = "业务场景管理")
@RestController
@RequestMapping("/rule-engine/scene")
@RequiredArgsConstructor
public class BusinessSceneController {

    private final BusinessSceneService businessSceneService;

    @Operation(summary = "获取场景列表")
    @GetMapping("/list")
    public Result<List<BusinessScene>> list() {
        List<BusinessScene> scenes = businessSceneService.listEnabled();
        return Result.success(scenes);
    }

    @Operation(summary = "分页查询场景")
    @GetMapping("/page")
    public Result<PageResult<BusinessScene>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "场景名称") @RequestParam(required = false) String sceneName,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        Page<BusinessScene> pageResult = businessSceneService.pageList(page, pageSize, sceneName, status);
        return Result.success(PageResult.of(pageResult));
    }

    @Operation(summary = "获取场景详情")
    @GetMapping("/{id}")
    public Result<BusinessScene> getById(@PathVariable Long id) {
        BusinessScene scene = businessSceneService.getById(id);
        return Result.success(scene);
    }

    @Operation(summary = "根据场景编码获取")
    @GetMapping("/code/{sceneCode}")
    public Result<BusinessScene> getByCode(@PathVariable String sceneCode) {
        BusinessScene scene = businessSceneService.getBySceneCode(sceneCode);
        return Result.success(scene);
    }

    @Operation(summary = "创建场景")
    @PostMapping("/create")
    public Result<Long> create(@RequestBody BusinessScene scene) {
        Long id = businessSceneService.createScene(scene);
        return Result.success(id);
    }

    @Operation(summary = "更新场景")
    @PutMapping("/update/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody BusinessScene scene) {
        scene.setId(id);
        businessSceneService.updateScene(scene);
        return Result.success();
    }

    @Operation(summary = "删除场景")
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        businessSceneService.deleteScene(id);
        return Result.success();
    }

    @Operation(summary = "更新场景状态")
    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        businessSceneService.updateStatus(id, status);
        return Result.success();
    }
}
