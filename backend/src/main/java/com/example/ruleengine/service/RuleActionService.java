package com.example.ruleengine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ruleengine.entity.RuleAction;

import java.util.List;

/**
 * 规则动作 Service 接口
 */
public interface RuleActionService extends IService<RuleAction> {

    /**
     * 根据规则ID查询动作列表
     */
    List<RuleAction> listByRuleId(Long ruleId);

    /**
     * 根据规则编码查询动作列表
     */
    List<RuleAction> listByRuleCode(String ruleCode);

    /**
     * 创建动作
     */
    Long createAction(RuleAction action);

    /**
     * 批量创建动作
     */
    void batchCreate(List<RuleAction> actions);

    /**
     * 更新动作
     */
    void updateAction(RuleAction action);

    /**
     * 删除动作
     */
    void deleteAction(Long id);

    /**
     * 根据规则ID删除所有动作
     */
    void deleteByRuleId(Long ruleId);
}
