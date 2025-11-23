package com.example.ruleengine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ruleengine.entity.RuleCondition;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 规则条件 Mapper 接口
 */
@Mapper
public interface RuleConditionMapper extends BaseMapper<RuleCondition> {

    /**
     * 根据规则ID查询条件列表
     */
    @Select("SELECT * FROM rule_condition WHERE rule_id = #{ruleId} ORDER BY sort_order")
    List<RuleCondition> selectByRuleId(@Param("ruleId") Long ruleId);

    /**
     * 根据条件组ID查询条件列表
     */
    @Select("SELECT * FROM rule_condition WHERE group_id = #{groupId} ORDER BY sort_order")
    List<RuleCondition> selectByGroupId(@Param("groupId") Long groupId);

    /**
     * 根据规则ID删除条件
     */
    @Delete("DELETE FROM rule_condition WHERE rule_id = #{ruleId}")
    int deleteByRuleId(@Param("ruleId") Long ruleId);

    /**
     * 根据条件组ID删除条件
     */
    @Delete("DELETE FROM rule_condition WHERE group_id = #{groupId}")
    int deleteByGroupId(@Param("groupId") Long groupId);
}
