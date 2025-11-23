package com.example.ruleengine.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ruleengine.entity.RuleGroup;

import java.util.List;

/**
 * 规则组 Service 接口
 */
public interface RuleGroupService extends IService<RuleGroup> {

    /**
     * 根据场景编码查询规则组列表
     */
    List<RuleGroup> listBySceneCode(String sceneCode);

    /**
     * 根据规则组编码查询
     */
    RuleGroup getByGroupCode(String groupCode);

    /**
     * 根据父级ID查询子规则组
     */
    List<RuleGroup> listByParentId(Long parentId);

    /**
     * 分页查询
     */
    Page<RuleGroup> pageList(Integer page, Integer pageSize, String sceneCode, String groupName);

    /**
     * 创建规则组
     */
    Long createGroup(RuleGroup group);

    /**
     * 更新规则组
     */
    void updateGroup(RuleGroup group);

    /**
     * 删除规则组
     */
    void deleteGroup(Long id);

    /**
     * 更新规则组状态
     */
    void updateStatus(Long id, Integer status);

    /**
     * 获取规则组树形结构
     */
    List<RuleGroup> getGroupTree(String sceneCode);
}
