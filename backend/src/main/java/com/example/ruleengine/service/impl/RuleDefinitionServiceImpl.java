package com.example.ruleengine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ruleengine.common.exception.BusinessException;
import com.example.ruleengine.dto.RuleExportDTO;
import com.example.ruleengine.dto.RuleImportRequest;
import com.example.ruleengine.dto.RuleImportResult;
import com.example.ruleengine.engine.RuleCompiler;
import com.example.ruleengine.entity.ConditionGroup;
import com.example.ruleengine.entity.RuleAction;
import com.example.ruleengine.entity.RuleCondition;
import com.example.ruleengine.entity.RuleDefinition;
import com.example.ruleengine.entity.RuleVersion;
import com.example.ruleengine.mapper.RuleDefinitionMapper;
import com.example.ruleengine.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import static com.example.ruleengine.config.CacheConfig.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 规则定义 Service 实现类
 * <p>
 * 提供规则的完整生命周期管理，包括：
 * <ul>
 *   <li>规则的CRUD操作（创建、读取、更新、删除）</li>
 *   <li>规则条件和动作的关联管理</li>
 *   <li>规则版本控制和历史记录</li>
 *   <li>规则的导入导出功能</li>
 *   <li>规则状态管理（启用/禁用）</li>
 *   <li>规则缓存管理</li>
 * </ul>
 * </p>
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RuleDefinitionServiceImpl extends ServiceImpl<RuleDefinitionMapper, RuleDefinition> implements RuleDefinitionService {

    private final RuleConditionService ruleConditionService;
    private final RuleActionService ruleActionService;
    private final RuleVersionService ruleVersionService;
    private final ConditionGroupService conditionGroupService;
    private final ObjectMapper objectMapper;

    @Lazy
    @org.springframework.beans.factory.annotation.Autowired
    private RuleCompiler ruleCompiler;

    /**
     * 根据规则编码查询规则
     *
     * @param ruleCode 规则唯一编码
     * @return 规则定义，不存在返回null
     */
    @Override
    public RuleDefinition getByRuleCode(String ruleCode) {
        return baseMapper.selectByRuleCode(ruleCode);
    }

    /**
     * 查询指定场景下的所有规则
     *
     * @param sceneCode 业务场景编码
     * @return 规则列表
     */
    @Override
    public List<RuleDefinition> listBySceneCode(String sceneCode) {
        return baseMapper.selectBySceneCode(sceneCode);
    }

    /**
     * 查询指定规则组下的所有规则
     *
     * @param groupId 规则组ID
     * @return 规则列表
     */
    @Override
    public List<RuleDefinition> listByGroupId(Long groupId) {
        return baseMapper.selectByGroupId(groupId);
    }

    /**
     * 查询指定场景下已启用且在有效期内的规则
     * <p>用于规则引擎执行时加载活跃规则</p>
     *
     * @param sceneCode 业务场景编码
     * @return 活跃规则列表
     */
    @Override
    public List<RuleDefinition> listActiveBySceneCode(String sceneCode) {
        return baseMapper.selectActiveBySceneCode(sceneCode);
    }

    /**
     * 分页查询规则列表
     * <p>支持多条件组合筛选，结果按优先级和创建时间倒序排列</p>
     *
     * @param page      页码（从1开始）
     * @param pageSize  每页大小
     * @param sceneCode 场景编码（可选）
     * @param ruleName  规则名称（模糊匹配，可选）
     * @param ruleCode  规则编码（模糊匹配，可选）
     * @param status    状态：0-禁用，1-启用（可选）
     * @param groupId   规则组ID（可选）
     * @return 分页结果
     */
    @Override
    public Page<RuleDefinition> pageList(Integer page, Integer pageSize, String sceneCode,
                                          String ruleName, String ruleCode, Integer status, Long groupId) {
        Page<RuleDefinition> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<RuleDefinition> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(sceneCode)) {
            wrapper.eq(RuleDefinition::getSceneCode, sceneCode);
        }
        if (StringUtils.hasText(ruleName)) {
            wrapper.like(RuleDefinition::getRuleName, ruleName);
        }
        if (StringUtils.hasText(ruleCode)) {
            wrapper.like(RuleDefinition::getRuleCode, ruleCode);
        }
        if (status != null) {
            wrapper.eq(RuleDefinition::getStatus, status);
        }
        if (groupId != null) {
            wrapper.eq(RuleDefinition::getGroupId, groupId);
        }
        wrapper.orderByDesc(RuleDefinition::getPriority);
        wrapper.orderByDesc(RuleDefinition::getCreateTime);

        return baseMapper.selectPage(pageParam, wrapper);
    }

    /**
     * 创建规则
     * <p>
     * 完整的规则创建流程：
     * <ol>
     *   <li>校验规则编码唯一性</li>
     *   <li>保存规则基本信息</li>
     *   <li>创建条件组并保存条件</li>
     *   <li>保存规则动作</li>
     *   <li>生成JSON配置和DRL内容</li>
     *   <li>创建初始版本记录</li>
     * </ol>
     * </p>
     *
     * @param rule 规则定义（包含条件和动作）
     * @return 新创建的规则ID
     * @throws BusinessException 规则编码已存在时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRule(RuleDefinition rule) {
        // 检查规则编码是否已存在
        RuleDefinition existing = getByRuleCode(rule.getRuleCode());
        if (existing != null) {
            throw new BusinessException("规则编码已存在: " + rule.getRuleCode());
        }

        // 设置初始版本
        rule.setVersion(1);
        baseMapper.insert(rule);

        // 保存规则条件和条件组
        if (rule.getConditions() != null && !rule.getConditions().isEmpty()) {
            // 创建条件组映射：前端传入的groupId -> 数据库生成的condition_group.id
            Map<Long, Long> groupIdMapping = new HashMap<>();

            // 按 groupId 分组，获取每组的 groupLogic
            Map<Long, String> groupLogicMap = rule.getConditions().stream()
                    .collect(Collectors.toMap(
                            c -> c.getGroupId() != null ? c.getGroupId() : 1L,
                            c -> c.getGroupLogic() != null ? c.getGroupLogic() : "AND",
                            (first, second) -> first // 同组取第一个的 logic
                    ));

            // 创建条件组
            groupLogicMap.keySet().stream()
                    .sorted()
                    .forEach(frontendGroupId -> {
                        ConditionGroup group = new ConditionGroup();
                        group.setRuleId(rule.getId());
                        group.setParentGroupId(0L); // 根节点
                        group.setGroupLogic(groupLogicMap.get(frontendGroupId)); // 使用前端传入的逻辑
                        group.setGroupLevel(frontendGroupId.intValue());
                        group.setSortOrder(frontendGroupId.intValue());
                        Long newGroupId = conditionGroupService.createGroup(group);
                        groupIdMapping.put(frontendGroupId, newGroupId);
                    });

            // 保存条件，使用映射后的真实 groupId
            int sortOrder = 0;
            for (RuleCondition condition : rule.getConditions()) {
                condition.setRuleId(rule.getId());
                // 映射到真实的 condition_group.id
                Long frontendGroupId = condition.getGroupId() != null ? condition.getGroupId() : 1L;
                condition.setGroupId(groupIdMapping.get(frontendGroupId));
                // 设置排序号
                condition.setSortOrder(sortOrder++);
                ruleConditionService.save(condition);
            }
        }

        // 保存规则动作
        if (rule.getActions() != null && !rule.getActions().isEmpty()) {
            int actionIndex = 0;
            for (RuleAction action : rule.getActions()) {
                action.setRuleId(rule.getId());
                // 自动生成 actionCode: 规则编码_动作类型_序号
                if (action.getActionCode() == null || action.getActionCode().isEmpty()) {
                    action.setActionCode(rule.getRuleCode() + "_" + action.getActionType() + "_" + actionIndex);
                }
                // 设置排序号
                if (action.getSortOrder() == null) {
                    action.setSortOrder(actionIndex);
                }
                actionIndex++;
                ruleActionService.save(action);
            }
        }

        // 生成并保存 JSON 配置和 DRL 内容
        String jsonConfig = generateJsonConfig(rule);
        String drlContent = generateDrlContent(rule);
        if (jsonConfig != null || drlContent != null) {
            rule.setJsonConfig(jsonConfig);
            rule.setDrlContent(drlContent);
            baseMapper.updateById(rule);
        }

        // 创建初始版本记录
        createVersionRecord(rule, "创建规则");

        return rule.getId();
    }

    /**
     * 更新规则
     * <p>
     * 更新流程会删除旧的条件和动作，重新创建新的关联数据。
     * 同时自动递增版本号并创建版本记录。
     * </p>
     *
     * @param rule 规则定义（包含条件和动作）
     * @throws BusinessException 规则不存在或编码冲突时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = CACHE_RULE_DETAIL, key = "#rule.id")
    public void updateRule(RuleDefinition rule) {
        RuleDefinition existing = getById(rule.getId());
        if (existing == null) {
            throw new BusinessException("规则不存在");
        }

        // 检查规则编码是否被其他记录使用
        if (!existing.getRuleCode().equals(rule.getRuleCode())) {
            RuleDefinition byCode = getByRuleCode(rule.getRuleCode());
            if (byCode != null) {
                throw new BusinessException("规则编码已存在: " + rule.getRuleCode());
            }
        }

        // 版本号自增
        rule.setVersion(existing.getVersion() + 1);
        baseMapper.updateById(rule);

        // 删除旧的条件、条件组和动作
        ruleConditionService.deleteByRuleId(rule.getId());
        conditionGroupService.deleteByRuleId(rule.getId());
        ruleActionService.deleteByRuleId(rule.getId());

        // 保存新的规则条件和条件组
        if (rule.getConditions() != null && !rule.getConditions().isEmpty()) {
            // 创建条件组映射：前端传入的groupId -> 数据库生成的condition_group.id
            Map<Long, Long> groupIdMapping = new HashMap<>();

            // 按 groupId 分组，获取每组的 groupLogic
            Map<Long, String> groupLogicMap = rule.getConditions().stream()
                    .collect(Collectors.toMap(
                            c -> c.getGroupId() != null ? c.getGroupId() : 1L,
                            c -> c.getGroupLogic() != null ? c.getGroupLogic() : "AND",
                            (first, second) -> first // 同组取第一个的 logic
                    ));

            // 创建条件组
            groupLogicMap.keySet().stream()
                    .sorted()
                    .forEach(frontendGroupId -> {
                        ConditionGroup group = new ConditionGroup();
                        group.setRuleId(rule.getId());
                        group.setParentGroupId(0L); // 根节点
                        group.setGroupLogic(groupLogicMap.get(frontendGroupId)); // 使用前端传入的逻辑
                        group.setGroupLevel(frontendGroupId.intValue());
                        group.setSortOrder(frontendGroupId.intValue());
                        Long newGroupId = conditionGroupService.createGroup(group);
                        groupIdMapping.put(frontendGroupId, newGroupId);
                    });

            // 保存条件，使用映射后的真实 groupId
            int sortOrder = 0;
            for (RuleCondition condition : rule.getConditions()) {
                condition.setRuleId(rule.getId());
                // 映射到真实的 condition_group.id
                Long frontendGroupId = condition.getGroupId() != null ? condition.getGroupId() : 1L;
                condition.setGroupId(groupIdMapping.get(frontendGroupId));
                // 设置排序号
                condition.setSortOrder(sortOrder++);
                ruleConditionService.save(condition);
            }
        }

        // 保存新的规则动作
        if (rule.getActions() != null && !rule.getActions().isEmpty()) {
            int actionIndex = 0;
            for (RuleAction action : rule.getActions()) {
                action.setRuleId(rule.getId());
                // 自动生成 actionCode: 规则编码_动作类型_序号
                if (action.getActionCode() == null || action.getActionCode().isEmpty()) {
                    action.setActionCode(rule.getRuleCode() + "_" + action.getActionType() + "_" + actionIndex);
                }
                // 设置排序号
                if (action.getSortOrder() == null) {
                    action.setSortOrder(actionIndex);
                }
                actionIndex++;
                ruleActionService.save(action);
            }
        }

        // 生成并保存 JSON 配置和 DRL 内容
        String jsonConfig = generateJsonConfig(rule);
        String drlContent = generateDrlContent(rule);
        if (jsonConfig != null || drlContent != null) {
            rule.setJsonConfig(jsonConfig);
            rule.setDrlContent(drlContent);
            baseMapper.updateById(rule);
        }

        // 创建版本记录
        createVersionRecord(rule, "更新规则");
    }

    /**
     * 删除规则
     * <p>级联删除规则的条件、条件组和动作</p>
     *
     * @param id 规则ID
     * @throws BusinessException 规则不存在时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = CACHE_RULE_DETAIL, key = "#id")
    public void deleteRule(Long id) {
        RuleDefinition existing = getById(id);
        if (existing == null) {
            throw new BusinessException("规则不存在");
        }

        // 删除相关条件
        ruleConditionService.deleteByRuleId(id);

        // 删除相关条件组
        conditionGroupService.deleteByRuleId(id);

        // 删除相关动作
        ruleActionService.deleteByRuleId(id);

        // 删除规则
        baseMapper.deleteById(id);
    }

    /**
     * 复制规则
     * <p>
     * 创建一个规则的副本，包含：
     * <ul>
     *   <li>新的规则编码（原编码_COPY_时间戳）</li>
     *   <li>新的规则名称（原名称_副本）</li>
     *   <li>复制所有条件和动作</li>
     *   <li>副本默认为禁用状态</li>
     * </ul>
     * </p>
     *
     * @param id 源规则ID
     * @return 新规则的ID
     * @throws BusinessException 源规则不存在时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long copyRule(Long id) {
        RuleDefinition existing = getRuleDetail(id);
        if (existing == null) {
            throw new BusinessException("规则不存在");
        }

        // 复制规则定义
        RuleDefinition newRule = new RuleDefinition();
        newRule.setRuleCode(existing.getRuleCode() + "_COPY_" + System.currentTimeMillis());
        newRule.setRuleName(existing.getRuleName() + "_副本");
        newRule.setRuleDesc(existing.getRuleDesc());
        newRule.setSceneCode(existing.getSceneCode());
        newRule.setGroupId(existing.getGroupId());
        newRule.setRuleType(existing.getRuleType());
        newRule.setPriority(existing.getPriority());
        newRule.setStatus(0); // 默认禁用
        newRule.setEffectiveStartTime(existing.getEffectiveStartTime());
        newRule.setEffectiveEndTime(existing.getEffectiveEndTime());
        newRule.setDrlContent(existing.getDrlContent());
        newRule.setJsonConfig(existing.getJsonConfig());
        newRule.setVersion(1);

        baseMapper.insert(newRule);

        // 复制条件
        List<RuleCondition> conditions = ruleConditionService.listByRuleId(id);
        for (RuleCondition condition : conditions) {
            RuleCondition newCondition = new RuleCondition();
            newCondition.setRuleId(newRule.getId());
            newCondition.setGroupId(condition.getGroupId());
            newCondition.setFieldCode(condition.getFieldCode());
            newCondition.setOperator(condition.getOperator());
            newCondition.setFieldValue(condition.getFieldValue());
            newCondition.setValueType(condition.getValueType());
            newCondition.setSortOrder(condition.getSortOrder());
            ruleConditionService.createCondition(newCondition);
        }

        // 复制动作
        List<RuleAction> actions = ruleActionService.listByRuleId(id);
        for (RuleAction action : actions) {
            RuleAction newAction = new RuleAction();
            newAction.setRuleId(newRule.getId());
            newAction.setActionType(action.getActionType());
            newAction.setActionParams(action.getActionParams());
            newAction.setSortOrder(action.getSortOrder());
            ruleActionService.createAction(newAction);
        }

        // 创建版本记录
        createVersionRecord(newRule, "复制规则");

        return newRule.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        RuleDefinition existing = getById(id);
        if (existing == null) {
            throw new BusinessException("规则不存在");
        }

        existing.setStatus(status);
        baseMapper.updateById(existing);
    }

    /**
     * 获取规则完整详情
     * <p>
     * 包含规则的条件列表和动作列表，支持缓存。
     * 每个条件会关联其所属条件组的逻辑关系(AND/OR)。
     * </p>
     *
     * @param id 规则ID
     * @return 规则详情（包含条件和动作），不存在返回null
     */
    @Override
    @Cacheable(value = CACHE_RULE_DETAIL, key = "#id", unless = "#result == null")
    public RuleDefinition getRuleDetail(Long id) {
        RuleDefinition rule = getById(id);
        if (rule == null) {
            return null;
        }

        // 加载条件
        List<RuleCondition> conditions = ruleConditionService.listByRuleId(id);

        // 加载条件组，构建 groupId -> groupLogic 映射
        List<ConditionGroup> groups = conditionGroupService.listByRuleId(id);
        Map<Long, String> groupLogicMap = groups.stream()
                .collect(Collectors.toMap(ConditionGroup::getId, ConditionGroup::getGroupLogic));

        // 为每个条件设置 groupLogic
        for (RuleCondition condition : conditions) {
            if (condition.getGroupId() != null && groupLogicMap.containsKey(condition.getGroupId())) {
                condition.setGroupLogic(groupLogicMap.get(condition.getGroupId()));
            }
        }

        rule.setConditions(conditions);

        // 加载动作
        List<RuleAction> actions = ruleActionService.listByRuleId(id);
        rule.setActions(actions);

        return rule;
    }

    /**
     * 生成规则的 JSON 配置
     */
    private String generateJsonConfig(RuleDefinition rule) {
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("ruleCode", rule.getRuleCode());
            config.put("ruleName", rule.getRuleName());
            config.put("ruleType", rule.getRuleType());
            config.put("priority", rule.getPriority());

            // 添加条件配置
            if (rule.getConditions() != null && !rule.getConditions().isEmpty()) {
                List<Map<String, Object>> conditionList = rule.getConditions().stream()
                        .map(cond -> {
                            Map<String, Object> condMap = new HashMap<>();
                            condMap.put("fieldCode", cond.getFieldCode());
                            condMap.put("operator", cond.getOperator());
                            condMap.put("fieldValue", cond.getFieldValue());
                            condMap.put("valueType", cond.getValueType());
                            condMap.put("groupId", cond.getGroupId());
                            return condMap;
                        })
                        .collect(Collectors.toList());
                config.put("conditions", conditionList);
            }

            // 添加动作配置
            if (rule.getActions() != null && !rule.getActions().isEmpty()) {
                List<Map<String, Object>> actionList = rule.getActions().stream()
                        .map(action -> {
                            Map<String, Object> actionMap = new HashMap<>();
                            actionMap.put("actionType", action.getActionType());
                            actionMap.put("actionParams", action.getActionParams());
                            actionMap.put("sortOrder", action.getSortOrder());
                            return actionMap;
                        })
                        .collect(Collectors.toList());
                config.put("actions", actionList);
            }

            return objectMapper.writeValueAsString(config);
        } catch (Exception e) {
            log.error("生成规则JSON配置失败: ruleCode={}", rule.getRuleCode(), e);
            return null;
        }
    }

    /**
     * 生成规则的 DRL 内容
     */
    private String generateDrlContent(RuleDefinition rule) {
        try {
            return ruleCompiler.compile(rule);
        } catch (Exception e) {
            log.error("编译规则DRL内容失败: ruleCode={}", rule.getRuleCode(), e);
            return null;
        }
    }

    private void createVersionRecord(RuleDefinition rule, String changeLog) {
        RuleVersion version = new RuleVersion();
        version.setRuleId(rule.getId());
        version.setVersionNo(rule.getVersion());
        version.setRuleContent(rule.getJsonConfig());
        version.setChangeLog(changeLog);
        version.setIsCurrent(1);

        // 重置之前的当前版本
        ruleVersionService.setCurrentVersion(rule.getId(), null);

        ruleVersionService.createVersion(version);
    }

    /**
     * 导出规则
     * <p>
     * 支持三种导出方式：
     * <ul>
     *   <li>按规则ID列表导出（优先）</li>
     *   <li>按场景编码导出该场景下所有规则</li>
     *   <li>导出所有规则（两个参数都为空时）</li>
     * </ul>
     * </p>
     *
     * @param ruleIds   规则ID列表（可选）
     * @param sceneCode 场景编码（可选）
     * @return 规则导出DTO列表
     */
    @Override
    public List<RuleExportDTO> exportRules(List<Long> ruleIds, String sceneCode) {
        List<RuleDefinition> rules;

        if (ruleIds != null && !ruleIds.isEmpty()) {
            // 按ID列表导出
            rules = ruleIds.stream()
                    .map(this::getRuleDetail)
                    .filter(r -> r != null)
                    .collect(Collectors.toList());
        } else if (StringUtils.hasText(sceneCode)) {
            // 按场景导出
            List<RuleDefinition> basicRules = listBySceneCode(sceneCode);
            rules = basicRules.stream()
                    .map(r -> getRuleDetail(r.getId()))
                    .collect(Collectors.toList());
        } else {
            // 导出所有
            List<RuleDefinition> allRules = list();
            rules = allRules.stream()
                    .map(r -> getRuleDetail(r.getId()))
                    .collect(Collectors.toList());
        }

        return rules.stream()
                .map(this::convertToExportDTO)
                .collect(Collectors.toList());
    }

    private RuleExportDTO convertToExportDTO(RuleDefinition rule) {
        RuleExportDTO dto = new RuleExportDTO();
        dto.setRuleCode(rule.getRuleCode());
        dto.setRuleName(rule.getRuleName());
        dto.setRuleDesc(rule.getRuleDesc());
        dto.setSceneCode(rule.getSceneCode());
        dto.setRuleType(rule.getRuleType());
        dto.setPriority(rule.getPriority());
        dto.setStatus(rule.getStatus());
        dto.setEffectiveStartTime(rule.getEffectiveStartTime());
        dto.setEffectiveEndTime(rule.getEffectiveEndTime());

        // 转换条件
        if (rule.getConditions() != null) {
            List<RuleExportDTO.ConditionDTO> conditions = rule.getConditions().stream()
                    .map(c -> {
                        RuleExportDTO.ConditionDTO condDTO = new RuleExportDTO.ConditionDTO();
                        condDTO.setFieldCode(c.getFieldCode());
                        condDTO.setOperator(c.getOperator());
                        condDTO.setFieldValue(c.getFieldValue());
                        condDTO.setValueType(c.getValueType());
                        condDTO.setGroupId(c.getGroupId() != null ? c.getGroupId().intValue() : 1);
                        condDTO.setGroupLogic(c.getGroupLogic());
                        condDTO.setSortOrder(c.getSortOrder());
                        return condDTO;
                    })
                    .collect(Collectors.toList());
            dto.setConditions(conditions);
        }

        // 转换动作
        if (rule.getActions() != null) {
            List<RuleExportDTO.ActionDTO> actions = rule.getActions().stream()
                    .map(a -> {
                        RuleExportDTO.ActionDTO actionDTO = new RuleExportDTO.ActionDTO();
                        actionDTO.setActionCode(a.getActionCode());
                        actionDTO.setActionType(a.getActionType());
                        actionDTO.setActionParams(a.getActionParams());
                        actionDTO.setSortOrder(a.getSortOrder());
                        return actionDTO;
                    })
                    .collect(Collectors.toList());
            dto.setActions(actions);
        }

        return dto;
    }

    /**
     * 导入规则
     * <p>
     * 支持三种冲突处理策略：
     * <ul>
     *   <li>SKIP：跳过已存在的规则</li>
     *   <li>OVERWRITE：覆盖已存在的规则</li>
     *   <li>RENAME：自动重命名后导入</li>
     * </ul>
     * </p>
     *
     * @param request 导入请求（包含规则列表和冲突策略）
     * @return 导入结果（成功/失败/跳过数量及详情）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RuleImportResult importRules(RuleImportRequest request) {
        RuleImportResult result = new RuleImportResult();
        List<RuleExportDTO> rules = request.getRules();

        if (rules == null || rules.isEmpty()) {
            result.setTotal(0);
            return result;
        }

        result.setTotal(rules.size());
        String conflictStrategy = request.getConflictStrategy();

        for (RuleExportDTO dto : rules) {
            try {
                RuleDefinition existing = getByRuleCode(dto.getRuleCode());

                if (existing != null) {
                    // 处理冲突
                    switch (conflictStrategy) {
                        case "SKIP":
                            result.setSkipCount(result.getSkipCount() + 1);
                            result.getDetails().add(RuleImportResult.ImportDetail.skip(
                                    dto.getRuleCode(), dto.getRuleName(), "规则编码已存在，已跳过"));
                            continue;

                        case "OVERWRITE":
                            // 删除旧规则，重新创建
                            deleteRule(existing.getId());
                            break;

                        case "RENAME":
                            // 重命名规则编码
                            dto.setRuleCode(dto.getRuleCode() + "_IMPORT_" + System.currentTimeMillis());
                            break;

                        default:
                            result.setSkipCount(result.getSkipCount() + 1);
                            result.getDetails().add(RuleImportResult.ImportDetail.skip(
                                    dto.getRuleCode(), dto.getRuleName(), "规则编码已存在，已跳过"));
                            continue;
                    }
                }

                // 创建新规则
                RuleDefinition newRule = convertFromExportDTO(dto);
                if (request.getEnableAfterImport() != null && request.getEnableAfterImport()) {
                    newRule.setStatus(1);
                } else {
                    newRule.setStatus(0); // 默认禁用
                }

                Long newRuleId = createRule(newRule);
                result.setSuccessCount(result.getSuccessCount() + 1);
                result.getDetails().add(RuleImportResult.ImportDetail.success(
                        dto.getRuleCode(), dto.getRuleName(), newRuleId));

            } catch (Exception e) {
                log.error("导入规则失败: ruleCode={}", dto.getRuleCode(), e);
                result.setFailCount(result.getFailCount() + 1);
                result.getDetails().add(RuleImportResult.ImportDetail.fail(
                        dto.getRuleCode(), dto.getRuleName(), e.getMessage()));
            }
        }

        return result;
    }

    private RuleDefinition convertFromExportDTO(RuleExportDTO dto) {
        RuleDefinition rule = new RuleDefinition();
        rule.setRuleCode(dto.getRuleCode());
        rule.setRuleName(dto.getRuleName());
        rule.setRuleDesc(dto.getRuleDesc());
        rule.setSceneCode(dto.getSceneCode());
        rule.setRuleType(dto.getRuleType());
        rule.setPriority(dto.getPriority());
        rule.setStatus(dto.getStatus());
        rule.setEffectiveStartTime(dto.getEffectiveStartTime());
        rule.setEffectiveEndTime(dto.getEffectiveEndTime());

        // 转换条件
        if (dto.getConditions() != null) {
            List<RuleCondition> conditions = dto.getConditions().stream()
                    .map(c -> {
                        RuleCondition cond = new RuleCondition();
                        cond.setFieldCode(c.getFieldCode());
                        cond.setOperator(c.getOperator());
                        cond.setFieldValue(c.getFieldValue());
                        cond.setValueType(c.getValueType());
                        cond.setGroupId(c.getGroupId() != null ? c.getGroupId().longValue() : 1L);
                        cond.setGroupLogic(c.getGroupLogic());
                        cond.setSortOrder(c.getSortOrder());
                        return cond;
                    })
                    .collect(Collectors.toList());
            rule.setConditions(conditions);
        }

        // 转换动作
        if (dto.getActions() != null) {
            List<RuleAction> actions = dto.getActions().stream()
                    .map(a -> {
                        RuleAction action = new RuleAction();
                        action.setActionCode(a.getActionCode());
                        action.setActionType(a.getActionType());
                        action.setActionParams(a.getActionParams());
                        action.setSortOrder(a.getSortOrder());
                        return action;
                    })
                    .collect(Collectors.toList());
            rule.setActions(actions);
        }

        return rule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ruleIds) {
        if (ruleIds == null || ruleIds.isEmpty()) {
            return;
        }
        for (Long ruleId : ruleIds) {
            deleteRule(ruleId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateStatus(List<Long> ruleIds, Integer status) {
        if (ruleIds == null || ruleIds.isEmpty()) {
            return;
        }
        for (Long ruleId : ruleIds) {
            updateStatus(ruleId, status);
        }
    }
}
