package com.example.ruleengine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ruleengine.entity.RuleGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 规则组 Mapper 接口
 */
@Mapper
public interface RuleGroupMapper extends BaseMapper<RuleGroup> {

    /**
     * 根据场景编码查询规则组列表
     */
    @Select("SELECT * FROM rule_group WHERE scene_code = #{sceneCode} AND is_deleted = 0 ORDER BY sort_order")
    List<RuleGroup> selectBySceneCode(@Param("sceneCode") String sceneCode);

    /**
     * 根据规则组编码查询
     */
    @Select("SELECT * FROM rule_group WHERE group_code = #{groupCode} AND is_deleted = 0")
    RuleGroup selectByGroupCode(@Param("groupCode") String groupCode);

    /**
     * 根据父ID查询子规则组
     */
    @Select("SELECT * FROM rule_group WHERE parent_id = #{parentId} AND is_deleted = 0 ORDER BY sort_order")
    List<RuleGroup> selectByParentId(@Param("parentId") Long parentId);
}
