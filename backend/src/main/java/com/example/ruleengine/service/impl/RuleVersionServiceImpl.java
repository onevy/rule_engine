package com.example.ruleengine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ruleengine.common.exception.BusinessException;
import com.example.ruleengine.entity.RuleDefinition;
import com.example.ruleengine.entity.RuleVersion;
import com.example.ruleengine.mapper.RuleDefinitionMapper;
import com.example.ruleengine.mapper.RuleVersionMapper;
import com.example.ruleengine.service.RuleVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 规则版本 Service 实现类
 */
@Service
@RequiredArgsConstructor
public class RuleVersionServiceImpl extends ServiceImpl<RuleVersionMapper, RuleVersion> implements RuleVersionService {

    private final RuleDefinitionMapper ruleDefinitionMapper;

    @Override
    public List<RuleVersion> listByRuleId(Long ruleId) {
        return baseMapper.selectByRuleId(ruleId);
    }

    @Override
    public RuleVersion getCurrentVersion(Long ruleId) {
        return baseMapper.selectCurrentVersion(ruleId);
    }

    @Override
    public RuleVersion getByRuleIdAndVersionNo(Long ruleId, Integer versionNo) {
        return baseMapper.selectByRuleIdAndVersionNo(ruleId, versionNo);
    }

    @Override
    public Page<RuleVersion> pageList(Integer page, Integer pageSize, Long ruleId) {
        Page<RuleVersion> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<RuleVersion> wrapper = new LambdaQueryWrapper<>();

        if (ruleId != null) {
            wrapper.eq(RuleVersion::getRuleId, ruleId);
        }
        wrapper.orderByDesc(RuleVersion::getVersionNo);

        return baseMapper.selectPage(pageParam, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createVersion(RuleVersion version) {
        baseMapper.insert(version);
        return version.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollback(Long ruleId, Integer versionNo) {
        // 获取目标版本
        RuleVersion targetVersion = getByRuleIdAndVersionNo(ruleId, versionNo);
        if (targetVersion == null) {
            throw new BusinessException("目标版本不存在");
        }

        // 获取当前规则
        RuleDefinition rule = ruleDefinitionMapper.selectById(ruleId);
        if (rule == null) {
            throw new BusinessException("规则不存在");
        }

        // 更新规则内容为目标版本
        rule.setJsonConfig(targetVersion.getRuleContent());
        rule.setVersion(rule.getVersion() + 1);
        ruleDefinitionMapper.updateById(rule);

        // 重置当前版本标记
        baseMapper.resetCurrentVersion(ruleId);

        // 创建新版本记录
        RuleVersion newVersion = new RuleVersion();
        newVersion.setRuleId(ruleId);
        newVersion.setVersionNo(rule.getVersion());
        newVersion.setRuleContent(targetVersion.getRuleContent());
        newVersion.setChangeLog("回滚到版本 " + versionNo);
        newVersion.setIsCurrent(1);
        baseMapper.insert(newVersion);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setCurrentVersion(Long ruleId, Long versionId) {
        // 重置所有版本的当前标记
        baseMapper.resetCurrentVersion(ruleId);

        // 设置指定版本为当前版本
        if (versionId != null) {
            RuleVersion version = getById(versionId);
            if (version != null) {
                version.setIsCurrent(1);
                baseMapper.updateById(version);
            }
        }
    }

    @Override
    public Object compareVersions(Long ruleId, Integer versionNo1, Integer versionNo2) {
        RuleVersion version1 = getByRuleIdAndVersionNo(ruleId, versionNo1);
        RuleVersion version2 = getByRuleIdAndVersionNo(ruleId, versionNo2);

        if (version1 == null || version2 == null) {
            throw new BusinessException("版本不存在");
        }

        Map<String, Object> comparison = new HashMap<>();
        comparison.put("version1", Map.of(
            "versionNo", version1.getVersionNo(),
            "ruleContent", version1.getRuleContent(),
            "changeLog", version1.getChangeLog() != null ? version1.getChangeLog() : "",
            "createTime", version1.getCreateTime()
        ));
        comparison.put("version2", Map.of(
            "versionNo", version2.getVersionNo(),
            "ruleContent", version2.getRuleContent(),
            "changeLog", version2.getChangeLog() != null ? version2.getChangeLog() : "",
            "createTime", version2.getCreateTime()
        ));

        return comparison;
    }
}
