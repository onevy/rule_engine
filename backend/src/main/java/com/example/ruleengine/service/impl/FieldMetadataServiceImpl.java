package com.example.ruleengine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ruleengine.common.exception.BusinessException;
import com.example.ruleengine.entity.FieldMetadata;
import com.example.ruleengine.mapper.FieldMetadataMapper;
import com.example.ruleengine.service.FieldMetadataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 字段元数据 Service 实现类
 */
@Service
public class FieldMetadataServiceImpl extends ServiceImpl<FieldMetadataMapper, FieldMetadata> implements FieldMetadataService {

    @Override
    public List<FieldMetadata> listBySceneCode(String sceneCode) {
        return baseMapper.selectBySceneCode(sceneCode);
    }

    @Override
    public FieldMetadata getBySceneCodeAndFieldCode(String sceneCode, String fieldCode) {
        return baseMapper.selectBySceneCodeAndFieldCode(sceneCode, fieldCode);
    }

    @Override
    public List<FieldMetadata> listBySceneCodeAndCategory(String sceneCode, String category) {
        return baseMapper.selectBySceneCodeAndCategory(sceneCode, category);
    }

    @Override
    public Page<FieldMetadata> pageList(Integer page, Integer pageSize, String sceneCode, String fieldName, String category) {
        Page<FieldMetadata> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<FieldMetadata> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(sceneCode)) {
            wrapper.eq(FieldMetadata::getSceneCode, sceneCode);
        }
        if (StringUtils.hasText(fieldName)) {
            wrapper.like(FieldMetadata::getFieldName, fieldName);
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(FieldMetadata::getCategory, category);
        }
        wrapper.orderByAsc(FieldMetadata::getSortOrder);

        return baseMapper.selectPage(pageParam, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createMetadata(FieldMetadata metadata) {
        // 检查字段编码是否已存在
        FieldMetadata existing = getBySceneCodeAndFieldCode(metadata.getSceneCode(), metadata.getFieldCode());
        if (existing != null) {
            throw new BusinessException("字段编码在该场景下已存在: " + metadata.getFieldCode());
        }

        baseMapper.insert(metadata);
        return metadata.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchCreate(List<FieldMetadata> metadataList) {
        for (FieldMetadata metadata : metadataList) {
            createMetadata(metadata);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMetadata(FieldMetadata metadata) {
        FieldMetadata existing = getById(metadata.getId());
        if (existing == null) {
            throw new BusinessException("字段元数据不存在");
        }

        // 检查字段编码是否被其他记录使用
        if (!existing.getFieldCode().equals(metadata.getFieldCode()) ||
            !existing.getSceneCode().equals(metadata.getSceneCode())) {
            FieldMetadata byCode = getBySceneCodeAndFieldCode(metadata.getSceneCode(), metadata.getFieldCode());
            if (byCode != null && !byCode.getId().equals(metadata.getId())) {
                throw new BusinessException("字段编码在该场景下已存在: " + metadata.getFieldCode());
            }
        }

        baseMapper.updateById(metadata);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMetadata(Long id) {
        FieldMetadata existing = getById(id);
        if (existing == null) {
            throw new BusinessException("字段元数据不存在");
        }

        baseMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBySceneCode(String sceneCode) {
        LambdaQueryWrapper<FieldMetadata> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FieldMetadata::getSceneCode, sceneCode);
        baseMapper.delete(wrapper);
    }
}
