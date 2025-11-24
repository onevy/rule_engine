package com.example.ruleengine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ruleengine.entity.AssessmentGroupMapping;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 评估分组映射 Mapper 接口
 */
@Mapper
public interface AssessmentGroupMappingMapper extends BaseMapper<AssessmentGroupMapping> {

    /**
     * 根据场景编码查询分组映射配置
     *
     * @param sceneCode 场景编码
     * @return 分组映射列表
     */
    @Select("SELECT * FROM assessment_group_mapping WHERE scene_code = #{sceneCode} AND is_deleted = 0 ORDER BY sort_order ASC")
    List<AssessmentGroupMapping> selectBySceneCode(@Param("sceneCode") String sceneCode);
}
