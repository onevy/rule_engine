package com.example.ruleengine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ruleengine.common.exception.BusinessException;
import com.example.ruleengine.entity.RuleGroup;
import com.example.ruleengine.mapper.RuleGroupMapper;
import com.example.ruleengine.service.RuleGroupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 规则组 Service 实现类
 */
@Service
public class RuleGroupServiceImpl extends ServiceImpl<RuleGroupMapper, RuleGroup> implements RuleGroupService {

    @Override
    public List<RuleGroup> listBySceneCode(String sceneCode) {
        if (sceneCode == null || sceneCode.trim().isEmpty()) {
            // 如果场景编码为空，查询所有规则组
            LambdaQueryWrapper<RuleGroup> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByAsc(RuleGroup::getSortOrder);
            return baseMapper.selectList(wrapper);
        }
        return baseMapper.selectBySceneCode(sceneCode);
    }

    @Override
    public RuleGroup getByGroupCode(String groupCode) {
        return baseMapper.selectByGroupCode(groupCode);
    }

    @Override
    public List<RuleGroup> listByParentId(Long parentId) {
        return baseMapper.selectByParentId(parentId);
    }

    @Override
    public Page<RuleGroup> pageList(Integer page, Integer pageSize, String sceneCode, String groupName) {
        Page<RuleGroup> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<RuleGroup> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(sceneCode)) {
            wrapper.eq(RuleGroup::getSceneCode, sceneCode);
        }
        if (StringUtils.hasText(groupName)) {
            wrapper.like(RuleGroup::getGroupName, groupName);
        }
        wrapper.orderByAsc(RuleGroup::getSortOrder);

        return baseMapper.selectPage(pageParam, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createGroup(RuleGroup group) {
        // 检查规则组编码是否已存在
        RuleGroup existing = getByGroupCode(group.getGroupCode());
        if (existing != null) {
            throw new BusinessException("规则组编码已存在: " + group.getGroupCode());
        }

        baseMapper.insert(group);
        return group.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGroup(RuleGroup group) {
        RuleGroup existing = getById(group.getId());
        if (existing == null) {
            throw new BusinessException("规则组不存在");
        }

        // 检查规则组编码是否被其他记录使用
        if (!existing.getGroupCode().equals(group.getGroupCode())) {
            RuleGroup byCode = getByGroupCode(group.getGroupCode());
            if (byCode != null) {
                throw new BusinessException("规则组编码已存在: " + group.getGroupCode());
            }
        }

        baseMapper.updateById(group);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroup(Long id) {
        RuleGroup existing = getById(id);
        if (existing == null) {
            throw new BusinessException("规则组不存在");
        }

        // 检查是否有子规则组
        List<RuleGroup> children = listByParentId(id);
        if (!children.isEmpty()) {
            throw new BusinessException("该规则组下有子规则组，不能删除");
        }

        baseMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        RuleGroup group = baseMapper.selectById(id);
        if (group == null) {
            throw new BusinessException("规则组不存在");
        }

        // 验证状态值
        if (status != 0 && status != 1) {
            throw new BusinessException("状态值无效，必须为 0（禁用）或 1（启用）");
        }

        group.setStatus(status);
        baseMapper.updateById(group);
    }

    @Override
    public List<RuleGroup> getGroupTree(String sceneCode) {
        List<RuleGroup> allGroups = listBySceneCode(sceneCode);

        // 构建树形结构
        Map<Long, List<RuleGroup>> childrenMap = allGroups.stream()
            .filter(g -> g.getParentId() != null && g.getParentId() > 0)
            .collect(Collectors.groupingBy(RuleGroup::getParentId));

        List<RuleGroup> rootGroups = allGroups.stream()
            .filter(g -> g.getParentId() == null || g.getParentId() == 0)
            .collect(Collectors.toList());

        for (RuleGroup root : rootGroups) {
            buildTree(root, childrenMap);
        }

        return rootGroups;
    }

    private void buildTree(RuleGroup parent, Map<Long, List<RuleGroup>> childrenMap) {
        List<RuleGroup> children = childrenMap.get(parent.getId());
        if (children != null) {
            for (RuleGroup child : children) {
                buildTree(child, childrenMap);
            }
            // 这里可以设置children到parent，但需要在RuleGroup实体中添加children字段
        }
    }
}
