package com.example.ruleengine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ruleengine.common.exception.BusinessException;
import com.example.ruleengine.entity.BusinessScene;
import com.example.ruleengine.mapper.BusinessSceneMapper;
import com.example.ruleengine.service.BusinessSceneService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 业务场景 Service 实现类
 */
@Service
public class BusinessSceneServiceImpl extends ServiceImpl<BusinessSceneMapper, BusinessScene> implements BusinessSceneService {

    @Override
    public BusinessScene getBySceneCode(String sceneCode) {
        return baseMapper.selectBySceneCode(sceneCode);
    }

    @Override
    public Page<BusinessScene> pageList(Integer page, Integer pageSize, String sceneName, Integer status) {
        Page<BusinessScene> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<BusinessScene> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(sceneName)) {
            wrapper.like(BusinessScene::getSceneName, sceneName);
        }
        if (status != null) {
            wrapper.eq(BusinessScene::getStatus, status);
        }
        wrapper.orderByDesc(BusinessScene::getCreateTime);

        return baseMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public List<BusinessScene> listEnabled() {
        LambdaQueryWrapper<BusinessScene> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BusinessScene::getStatus, 1);
        wrapper.orderByAsc(BusinessScene::getSortOrder);
        return baseMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createScene(BusinessScene scene) {
        // 检查场景编码是否已存在
        BusinessScene existing = getBySceneCode(scene.getSceneCode());
        if (existing != null) {
            throw new BusinessException("场景编码已存在: " + scene.getSceneCode());
        }

        baseMapper.insert(scene);
        return scene.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateScene(BusinessScene scene) {
        BusinessScene existing = getById(scene.getId());
        if (existing == null) {
            throw new BusinessException("场景不存在");
        }

        // 检查场景编码是否被其他记录使用
        if (!existing.getSceneCode().equals(scene.getSceneCode())) {
            BusinessScene byCode = getBySceneCode(scene.getSceneCode());
            if (byCode != null) {
                throw new BusinessException("场景编码已存在: " + scene.getSceneCode());
            }
        }

        baseMapper.updateById(scene);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteScene(Long id) {
        BusinessScene existing = getById(id);
        if (existing == null) {
            throw new BusinessException("场景不存在");
        }

        baseMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        BusinessScene existing = getById(id);
        if (existing == null) {
            throw new BusinessException("场景不存在");
        }

        existing.setStatus(status);
        baseMapper.updateById(existing);
    }
}
