package com.example.ruleengine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ruleengine.entity.RuleDefinition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 规则定义 Mapper 接口
 */
@Mapper
public interface RuleDefinitionMapper extends BaseMapper<RuleDefinition> {

    /**
     * 根据场景编码查询启用的规则列表（包含生效时间窗口检查）
     */
    @Select("SELECT * FROM rule_definition WHERE scene_code = #{sceneCode} AND status = 1 AND is_deleted = 0 " +
            "AND (effective_start_time IS NULL OR effective_start_time <= NOW()) " +
            "AND (effective_end_time IS NULL OR effective_end_time >= NOW()) " +
            "ORDER BY priority DESC")
    List<RuleDefinition> selectActiveBySceneCode(@Param("sceneCode") String sceneCode);

    /**
     * 根据规则编码查询
     */
    @Select("SELECT * FROM rule_definition WHERE rule_code = #{ruleCode} AND is_deleted = 0")
    RuleDefinition selectByRuleCode(@Param("ruleCode") String ruleCode);

    /**
     * 根据规则组ID查询规则列表
     */
    @Select("SELECT * FROM rule_definition WHERE group_id = #{groupId} AND is_deleted = 0 ORDER BY priority DESC")
    List<RuleDefinition> selectByGroupId(@Param("groupId") Long groupId);

    /**
     * 根据场景编码查询所有规则
     */
    @Select("SELECT * FROM rule_definition WHERE scene_code = #{sceneCode} AND is_deleted = 0 ORDER BY priority DESC")
    List<RuleDefinition> selectBySceneCode(@Param("sceneCode") String sceneCode);
}
