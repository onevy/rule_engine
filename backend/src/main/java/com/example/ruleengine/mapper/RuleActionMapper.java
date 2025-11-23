package com.example.ruleengine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ruleengine.entity.RuleAction;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 规则动作 Mapper 接口
 */
@Mapper
public interface RuleActionMapper extends BaseMapper<RuleAction> {

    /**
     * 根据规则ID查询动作列表
     */
    @Select("SELECT * FROM rule_action WHERE rule_id = #{ruleId} ORDER BY sort_order")
    List<RuleAction> selectByRuleId(@Param("ruleId") Long ruleId);

    /**
     * 根据规则ID删除动作
     */
    @Delete("DELETE FROM rule_action WHERE rule_id = #{ruleId}")
    int deleteByRuleId(@Param("ruleId") Long ruleId);

    /**
     * 根据规则编码查询动作列表
     */
    @Select("SELECT a.* FROM rule_action a " +
            "INNER JOIN rule_definition d ON a.rule_id = d.id " +
            "WHERE d.rule_code = #{ruleCode} ORDER BY a.sort_order")
    List<RuleAction> selectByRuleCode(@Param("ruleCode") String ruleCode);
}
