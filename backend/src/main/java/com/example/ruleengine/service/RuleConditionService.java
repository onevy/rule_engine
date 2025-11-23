package com.example.ruleengine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ruleengine.entity.RuleCondition;

import java.util.List;

/**
 * 规则条件 Service 接口
 */
public interface RuleConditionService extends IService<RuleCondition> {

    /**
     * 根据规则ID查询条件列表
     */
    List<RuleCondition> listByRuleId(Long ruleId);

    /**
     * 根据条件组ID查询条件列表
     */
    List<RuleCondition> listByGroupId(Long groupId);

    /**
     * 创建条件
     */
    Long createCondition(RuleCondition condition);

    /**
     * 批量创建条件
     */
    void batchCreate(List<RuleCondition> conditions);

    /**
     * 更新条件
     */
    void updateCondition(RuleCondition condition);

    /**
     * 删除条件
     */
    void deleteCondition(Long id);

    /**
     * 根据规则ID删除所有条件
     */
    void deleteByRuleId(Long ruleId);

    /**
     * 根据条件组ID删除所有条件
     */
    void deleteByGroupId(Long groupId);
}
