package com.example.ruleengine.engine;

import java.util.Map;

/**
 * 业务适配器接口
 *
 * 每个业务场景需要实现此接口，负责将业务数据转换为规则引擎可识别的格式，
 * 以及将规则执行结果转换回业务格式。
 */
public interface BusinessAdapter {

    /**
     * 获取场景编码
     *
     * @return 场景编码
     */
    String getSceneCode();

    /**
     * 前置处理：将业务数据转换为规则引擎输入格式
     *
     * @param originalData 原始业务数据
     * @return 转换后的Map格式数据
     */
    Map<String, Object> preProcess(Object originalData);

    /**
     * 后置处理：将规则执行结果转换为业务格式
     *
     * @param ruleResult 规则执行结果
     * @return 业务格式的结果
     */
    Object postProcess(RuleResult ruleResult);

    /**
     * 获取自定义动作处理器
     *
     * @return 动作处理器映射表（动作类型 -> 处理器）
     */
    default Map<String, SimpleActionHandler> getActionHandlers() {
        return null;
    }

    /**
     * 数据验证
     *
     * @param data 待验证数据
     * @return 验证结果，null表示验证通过，否则返回错误信息
     */
    default String validate(Map<String, Object> data) {
        return null;
    }
}
