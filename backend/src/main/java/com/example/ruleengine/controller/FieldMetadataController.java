package com.example.ruleengine.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ruleengine.common.result.PageResult;
import com.example.ruleengine.common.result.Result;
import com.example.ruleengine.entity.FieldMetadata;
import com.example.ruleengine.service.FieldMetadataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字段元数据管理 Controller
 */
@Tag(name = "字段元数据管理")
@RestController
@RequestMapping("/rule-engine/metadata")
@RequiredArgsConstructor
public class FieldMetadataController {

    private final FieldMetadataService fieldMetadataService;

    @Operation(summary = "根据场景编码获取元数据")
    @GetMapping("/{sceneCode}")
    public Result<List<FieldMetadata>> listBySceneCode(@PathVariable String sceneCode) {
        List<FieldMetadata> metadataList = fieldMetadataService.listBySceneCode(sceneCode);
        return Result.success(metadataList);
    }

    @Operation(summary = "根据场景和分类获取元数据")
    @GetMapping("/{sceneCode}/category/{category}")
    public Result<List<FieldMetadata>> listByCategory(
            @PathVariable String sceneCode,
            @PathVariable String category) {
        List<FieldMetadata> metadataList = fieldMetadataService.listBySceneCodeAndCategory(sceneCode, category);
        return Result.success(metadataList);
    }

    @Operation(summary = "分页查询元数据")
    @GetMapping("/page")
    public Result<PageResult<FieldMetadata>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "场景编码") @RequestParam(required = false) String sceneCode,
            @Parameter(description = "字段名称") @RequestParam(required = false) String fieldName,
            @Parameter(description = "分类") @RequestParam(required = false) String category) {
        Page<FieldMetadata> pageResult = fieldMetadataService.pageList(page, pageSize, sceneCode, fieldName, category);
        return Result.success(PageResult.of(pageResult));
    }

    @Operation(summary = "获取元数据详情")
    @GetMapping("/detail/{id}")
    public Result<FieldMetadata> getById(@PathVariable Long id) {
        FieldMetadata metadata = fieldMetadataService.getById(id);
        return Result.success(metadata);
    }

    @Operation(summary = "创建元数据")
    @PostMapping("/create")
    public Result<Long> create(@RequestBody FieldMetadata metadata) {
        Long id = fieldMetadataService.createMetadata(metadata);
        return Result.success(id);
    }

    @Operation(summary = "批量创建元数据")
    @PostMapping("/batch-create")
    public Result<Void> batchCreate(@RequestBody List<FieldMetadata> metadataList) {
        fieldMetadataService.batchCreate(metadataList);
        return Result.success();
    }

    @Operation(summary = "更新元数据")
    @PutMapping("/update/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody FieldMetadata metadata) {
        metadata.setId(id);
        fieldMetadataService.updateMetadata(metadata);
        return Result.success();
    }

    @Operation(summary = "删除元数据")
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        fieldMetadataService.deleteMetadata(id);
        return Result.success();
    }

    @Operation(summary = "根据场景编码删除所有元数据")
    @DeleteMapping("/delete-by-scene/{sceneCode}")
    public Result<Void> deleteBySceneCode(@PathVariable String sceneCode) {
        fieldMetadataService.deleteBySceneCode(sceneCode);
        return Result.success();
    }
}
