package com.example.ruleengine.engine;

import java.util.List;

/**
 * 规则引擎接口
 */
public interface RuleEngine {

    /**
     * 执行规则
     *
     * @param context 规则执行上下文
     * @return 规则执行结果
     */
    RuleResult execute(RuleContext context);

    /**
     * 批量执行规则
     *
     * @param contexts 规则执行上下文列表
     * @return 规则执行结果列表
     */
    List<RuleResult> batchExecute(List<RuleContext> contexts);

    /**
     * 重新加载指定场景的规则
     *
     * @param sceneCode 场景编码
     */
    void reload(String sceneCode);

    /**
     * 重新加载所有规则
     */
    void reloadAll();

    /**
     * 验证规则语法
     *
     * @param ruleContent 规则内容（DRL格式）
     * @return 验证结果，null表示验证通过，否则返回错误信息
     */
    String validate(String ruleContent);

    /**
     * 清除指定场景的规则缓存
     *
     * @param sceneCode 场景编码
     */
    void clearCache(String sceneCode);

    /**
     * 清除所有规则缓存
     */
    void clearAllCache();
}
