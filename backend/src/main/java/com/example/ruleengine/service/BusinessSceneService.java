package com.example.ruleengine.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ruleengine.entity.BusinessScene;

import java.util.List;

/**
 * 业务场景 Service 接口
 */
public interface BusinessSceneService extends IService<BusinessScene> {

    /**
     * 根据场景编码查询
     */
    BusinessScene getBySceneCode(String sceneCode);

    /**
     * 分页查询场景列表
     */
    Page<BusinessScene> pageList(Integer page, Integer pageSize, String sceneName, Integer status);

    /**
     * 查询所有启用的场景
     */
    List<BusinessScene> listEnabled();

    /**
     * 创建场景
     */
    Long createScene(BusinessScene scene);

    /**
     * 更新场景
     */
    void updateScene(BusinessScene scene);

    /**
     * 删除场景
     */
    void deleteScene(Long id);

    /**
     * 更新场景状态
     */
    void updateStatus(Long id, Integer status);
}
