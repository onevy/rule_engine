package com.example.ruleengine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ruleengine.entity.RuleExecutionLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 规则执行日志 Mapper 接口
 */
@Mapper
public interface RuleExecutionLogMapper extends BaseMapper<RuleExecutionLog> {

    /**
     * 根据追踪ID查询日志
     */
    @Select("SELECT * FROM rule_execution_log WHERE trace_id = #{traceId}")
    List<RuleExecutionLog> selectByTraceId(@Param("traceId") String traceId);

    /**
     * 根据场景编码查询日志
     */
    @Select("SELECT * FROM rule_execution_log WHERE scene_code = #{sceneCode} ORDER BY execute_time DESC")
    List<RuleExecutionLog> selectBySceneCode(@Param("sceneCode") String sceneCode);

    /**
     * 根据业务ID查询日志
     */
    @Select("SELECT * FROM rule_execution_log WHERE business_id = #{businessId} ORDER BY execute_time DESC")
    List<RuleExecutionLog> selectByBusinessId(@Param("businessId") String businessId);
}
