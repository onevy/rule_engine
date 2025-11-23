package com.example.ruleengine.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ruleengine.common.exception.BusinessException;
import com.example.ruleengine.entity.RuleAction;
import com.example.ruleengine.mapper.RuleActionMapper;
import com.example.ruleengine.service.RuleActionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 规则动作 Service 实现类
 */
@Service
public class RuleActionServiceImpl extends ServiceImpl<RuleActionMapper, RuleAction> implements RuleActionService {

    @Override
    public List<RuleAction> listByRuleId(Long ruleId) {
        return baseMapper.selectByRuleId(ruleId);
    }

    @Override
    public List<RuleAction> listByRuleCode(String ruleCode) {
        return baseMapper.selectByRuleCode(ruleCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAction(RuleAction action) {
        baseMapper.insert(action);
        return action.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchCreate(List<RuleAction> actions) {
        for (RuleAction action : actions) {
            baseMapper.insert(action);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAction(RuleAction action) {
        RuleAction existing = getById(action.getId());
        if (existing == null) {
            throw new BusinessException("规则动作不存在");
        }

        baseMapper.updateById(action);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAction(Long id) {
        RuleAction existing = getById(id);
        if (existing == null) {
            throw new BusinessException("规则动作不存在");
        }

        baseMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByRuleId(Long ruleId) {
        baseMapper.deleteByRuleId(ruleId);
    }
}
