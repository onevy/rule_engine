package com.example.ruleengine.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ruleengine.entity.FieldMetadata;

import java.util.List;

/**
 * 字段元数据 Service 接口
 */
public interface FieldMetadataService extends IService<FieldMetadata> {

    /**
     * 根据场景编码查询字段列表
     */
    List<FieldMetadata> listBySceneCode(String sceneCode);

    /**
     * 根据场景编码和字段编码查询
     */
    FieldMetadata getBySceneCodeAndFieldCode(String sceneCode, String fieldCode);

    /**
     * 根据场景编码和分类查询
     */
    List<FieldMetadata> listBySceneCodeAndCategory(String sceneCode, String category);

    /**
     * 分页查询
     */
    Page<FieldMetadata> pageList(Integer page, Integer pageSize, String sceneCode, String fieldName, String category);

    /**
     * 创建字段元数据
     */
    Long createMetadata(FieldMetadata metadata);

    /**
     * 批量创建字段元数据
     */
    void batchCreate(List<FieldMetadata> metadataList);

    /**
     * 更新字段元数据
     */
    void updateMetadata(FieldMetadata metadata);

    /**
     * 删除字段元数据
     */
    void deleteMetadata(Long id);

    /**
     * 根据场景编码删除所有字段
     */
    void deleteBySceneCode(String sceneCode);
}
