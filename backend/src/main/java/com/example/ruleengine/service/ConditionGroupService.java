package com.example.ruleengine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ruleengine.entity.ConditionGroup;

import java.util.List;

/**
 * 条件组 Service 接口
 */
public interface ConditionGroupService extends IService<ConditionGroup> {

    /**
     * 根据规则ID查询条件组列表
     */
    List<ConditionGroup> listByRuleId(Long ruleId);

    /**
     * 根据父条件组ID查询子条件组
     */
    List<ConditionGroup> listByParentGroupId(Long parentGroupId);

    /**
     * 创建条件组
     */
    Long createGroup(ConditionGroup group);

    /**
     * 批量创建条件组
     */
    void batchCreate(List<ConditionGroup> groups);

    /**
     * 更新条件组
     */
    void updateGroup(ConditionGroup group);

    /**
     * 删除条件组
     */
    void deleteGroup(Long id);

    /**
     * 根据规则ID删除所有条件组
     */
    void deleteByRuleId(Long ruleId);

    /**
     * 获取条件组树形结构
     */
    List<ConditionGroup> getGroupTree(Long ruleId);
}
