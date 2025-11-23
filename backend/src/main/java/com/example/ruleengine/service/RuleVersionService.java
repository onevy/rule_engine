package com.example.ruleengine.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ruleengine.entity.RuleVersion;

import java.util.List;

/**
 * 规则版本 Service 接口
 */
public interface RuleVersionService extends IService<RuleVersion> {

    /**
     * 根据规则ID查询版本列表
     */
    List<RuleVersion> listByRuleId(Long ruleId);

    /**
     * 查询当前版本
     */
    RuleVersion getCurrentVersion(Long ruleId);

    /**
     * 根据规则ID和版本号查询
     */
    RuleVersion getByRuleIdAndVersionNo(Long ruleId, Integer versionNo);

    /**
     * 分页查询版本列表
     */
    Page<RuleVersion> pageList(Integer page, Integer pageSize, Long ruleId);

    /**
     * 创建新版本
     */
    Long createVersion(RuleVersion version);

    /**
     * 回滚到指定版本
     */
    void rollback(Long ruleId, Integer versionNo);

    /**
     * 设置当前版本
     */
    void setCurrentVersion(Long ruleId, Long versionId);

    /**
     * 版本对比
     */
    Object compareVersions(Long ruleId, Integer versionNo1, Integer versionNo2);
}
