package com.example.ruleengine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ruleengine.entity.ConditionGroup;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 条件组 Mapper 接口
 */
@Mapper
public interface ConditionGroupMapper extends BaseMapper<ConditionGroup> {

    /**
     * 根据规则ID查询条件组列表
     */
    @Select("SELECT * FROM condition_group WHERE rule_id = #{ruleId} ORDER BY group_level, sort_order")
    List<ConditionGroup> selectByRuleId(@Param("ruleId") Long ruleId);

    /**
     * 根据父条件组ID查询子条件组
     */
    @Select("SELECT * FROM condition_group WHERE parent_group_id = #{parentGroupId} ORDER BY sort_order")
    List<ConditionGroup> selectByParentGroupId(@Param("parentGroupId") Long parentGroupId);

    /**
     * 根据规则ID删除条件组
     */
    @Delete("DELETE FROM condition_group WHERE rule_id = #{ruleId}")
    int deleteByRuleId(@Param("ruleId") Long ruleId);
}
