package com.example.ruleengine.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ruleengine.common.exception.BusinessException;
import com.example.ruleengine.entity.RuleCondition;
import com.example.ruleengine.mapper.RuleConditionMapper;
import com.example.ruleengine.service.RuleConditionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 规则条件 Service 实现类
 */
@Service
public class RuleConditionServiceImpl extends ServiceImpl<RuleConditionMapper, RuleCondition> implements RuleConditionService {

    @Override
    public List<RuleCondition> listByRuleId(Long ruleId) {
        return baseMapper.selectByRuleId(ruleId);
    }

    @Override
    public List<RuleCondition> listByGroupId(Long groupId) {
        return baseMapper.selectByGroupId(groupId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCondition(RuleCondition condition) {
        baseMapper.insert(condition);
        return condition.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchCreate(List<RuleCondition> conditions) {
        for (RuleCondition condition : conditions) {
            baseMapper.insert(condition);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCondition(RuleCondition condition) {
        RuleCondition existing = getById(condition.getId());
        if (existing == null) {
            throw new BusinessException("规则条件不存在");
        }

        baseMapper.updateById(condition);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCondition(Long id) {
        RuleCondition existing = getById(id);
        if (existing == null) {
            throw new BusinessException("规则条件不存在");
        }

        baseMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByRuleId(Long ruleId) {
        baseMapper.deleteByRuleId(ruleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByGroupId(Long groupId) {
        baseMapper.deleteByGroupId(groupId);
    }
}
