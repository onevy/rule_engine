package com.example.ruleengine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ruleengine.entity.BusinessScene;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 业务场景 Mapper 接口
 */
@Mapper
public interface BusinessSceneMapper extends BaseMapper<BusinessScene> {

    /**
     * 根据场景编码查询
     */
    @Select("SELECT * FROM business_scene WHERE scene_code = #{sceneCode} AND is_deleted = 0")
    BusinessScene selectBySceneCode(@Param("sceneCode") String sceneCode);
}
