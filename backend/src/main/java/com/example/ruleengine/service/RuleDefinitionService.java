package com.example.ruleengine.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ruleengine.dto.RuleExportDTO;
import com.example.ruleengine.dto.RuleImportRequest;
import com.example.ruleengine.dto.RuleImportResult;
import com.example.ruleengine.entity.RuleDefinition;

import java.util.List;

/**
 * 规则定义 Service 接口
 */
public interface RuleDefinitionService extends IService<RuleDefinition> {

    /**
     * 根据规则编码查询
     */
    RuleDefinition getByRuleCode(String ruleCode);

    /**
     * 根据场景编码查询规则列表
     */
    List<RuleDefinition> listBySceneCode(String sceneCode);

    /**
     * 根据规则组ID查询规则列表
     */
    List<RuleDefinition> listByGroupId(Long groupId);

    /**
     * 查询场景下所有生效的规则
     */
    List<RuleDefinition> listActiveBySceneCode(String sceneCode);

    /**
     * 分页查询
     */
    Page<RuleDefinition> pageList(Integer page, Integer pageSize, String sceneCode,
                                   String ruleName, String ruleCode, Integer status, Long groupId);

    /**
     * 创建规则
     */
    Long createRule(RuleDefinition rule);

    /**
     * 更新规则
     */
    void updateRule(RuleDefinition rule);

    /**
     * 删除规则
     */
    void deleteRule(Long id);

    /**
     * 复制规则
     */
    Long copyRule(Long id);

    /**
     * 更新规则状态
     */
    void updateStatus(Long id, Integer status);

    /**
     * 获取规则详情（包含条件和动作）
     */
    RuleDefinition getRuleDetail(Long id);

    /**
     * 导出规则
     * @param ruleIds 规则ID列表，为空则导出所有
     * @param sceneCode 场景编码，用于过滤
     * @return 导出的规则列表
     */
    List<RuleExportDTO> exportRules(List<Long> ruleIds, String sceneCode);

    /**
     * 导入规则
     * @param request 导入请求
     * @return 导入结果
     */
    RuleImportResult importRules(RuleImportRequest request);

    /**
     * 批量删除规则
     * @param ruleIds 规则ID列表
     */
    void batchDelete(List<Long> ruleIds);

    /**
     * 批量更新规则状态
     * @param ruleIds 规则ID列表
     * @param status 状态
     */
    void batchUpdateStatus(List<Long> ruleIds, Integer status);
}
