package com.example.ruleengine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ruleengine.entity.RuleVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 规则版本 Mapper 接口
 */
@Mapper
public interface RuleVersionMapper extends BaseMapper<RuleVersion> {

    /**
     * 根据规则ID查询版本列表
     */
    @Select("SELECT * FROM rule_version WHERE rule_id = #{ruleId} ORDER BY version_no DESC")
    List<RuleVersion> selectByRuleId(@Param("ruleId") Long ruleId);

    /**
     * 查询当前版本
     */
    @Select("SELECT * FROM rule_version WHERE rule_id = #{ruleId} AND is_current = 1")
    RuleVersion selectCurrentVersion(@Param("ruleId") Long ruleId);

    /**
     * 根据规则ID和版本号查询
     */
    @Select("SELECT * FROM rule_version WHERE rule_id = #{ruleId} AND version_no = #{versionNo}")
    RuleVersion selectByRuleIdAndVersionNo(@Param("ruleId") Long ruleId, @Param("versionNo") Integer versionNo);

    /**
     * 重置当前版本标记
     */
    @Update("UPDATE rule_version SET is_current = 0 WHERE rule_id = #{ruleId}")
    int resetCurrentVersion(@Param("ruleId") Long ruleId);
}
