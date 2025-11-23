package com.example.ruleengine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ruleengine.entity.FieldMetadata;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 字段元数据 Mapper 接口
 */
@Mapper
public interface FieldMetadataMapper extends BaseMapper<FieldMetadata> {

    /**
     * 根据场景编码查询字段列表
     */
    @Select("SELECT * FROM field_metadata WHERE scene_code = #{sceneCode} AND is_deleted = 0 ORDER BY sort_order")
    List<FieldMetadata> selectBySceneCode(@Param("sceneCode") String sceneCode);

    /**
     * 根据场景编码和字段编码查询
     */
    @Select("SELECT * FROM field_metadata WHERE scene_code = #{sceneCode} AND field_code = #{fieldCode} AND is_deleted = 0")
    FieldMetadata selectBySceneCodeAndFieldCode(@Param("sceneCode") String sceneCode, @Param("fieldCode") String fieldCode);

    /**
     * 根据场景编码和分类查询
     */
    @Select("SELECT * FROM field_metadata WHERE scene_code = #{sceneCode} AND category = #{category} AND is_deleted = 0 ORDER BY sort_order")
    List<FieldMetadata> selectBySceneCodeAndCategory(@Param("sceneCode") String sceneCode, @Param("category") String category);
}
