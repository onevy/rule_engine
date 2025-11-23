package com.example.ruleengine.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ruleengine.common.exception.BusinessException;
import com.example.ruleengine.entity.ConditionGroup;
import com.example.ruleengine.mapper.ConditionGroupMapper;
import com.example.ruleengine.service.ConditionGroupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 条件组 Service 实现类
 */
@Service
public class ConditionGroupServiceImpl extends ServiceImpl<ConditionGroupMapper, ConditionGroup> implements ConditionGroupService {

    @Override
    public List<ConditionGroup> listByRuleId(Long ruleId) {
        return baseMapper.selectByRuleId(ruleId);
    }

    @Override
    public List<ConditionGroup> listByParentGroupId(Long parentGroupId) {
        return baseMapper.selectByParentGroupId(parentGroupId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createGroup(ConditionGroup group) {
        baseMapper.insert(group);
        return group.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchCreate(List<ConditionGroup> groups) {
        for (ConditionGroup group : groups) {
            baseMapper.insert(group);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGroup(ConditionGroup group) {
        ConditionGroup existing = getById(group.getId());
        if (existing == null) {
            throw new BusinessException("条件组不存在");
        }

        baseMapper.updateById(group);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroup(Long id) {
        ConditionGroup existing = getById(id);
        if (existing == null) {
            throw new BusinessException("条件组不存在");
        }

        // 检查是否有子条件组
        List<ConditionGroup> children = listByParentGroupId(id);
        if (!children.isEmpty()) {
            throw new BusinessException("该条件组下有子条件组，不能删除");
        }

        baseMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByRuleId(Long ruleId) {
        baseMapper.deleteByRuleId(ruleId);
    }

    @Override
    public List<ConditionGroup> getGroupTree(Long ruleId) {
        List<ConditionGroup> allGroups = listByRuleId(ruleId);

        // 构建树形结构
        Map<Long, List<ConditionGroup>> childrenMap = allGroups.stream()
            .filter(g -> g.getParentGroupId() != null && g.getParentGroupId() > 0)
            .collect(Collectors.groupingBy(ConditionGroup::getParentGroupId));

        List<ConditionGroup> rootGroups = allGroups.stream()
            .filter(g -> g.getParentGroupId() == null || g.getParentGroupId() == 0)
            .collect(Collectors.toList());

        for (ConditionGroup root : rootGroups) {
            buildTree(root, childrenMap);
        }

        return rootGroups;
    }

    private void buildTree(ConditionGroup parent, Map<Long, List<ConditionGroup>> childrenMap) {
        List<ConditionGroup> children = childrenMap.get(parent.getId());
        if (children != null) {
            for (ConditionGroup child : children) {
                buildTree(child, childrenMap);
            }
            // 这里可以设置children到parent，但需要在ConditionGroup实体中添加children字段
        }
    }
}
