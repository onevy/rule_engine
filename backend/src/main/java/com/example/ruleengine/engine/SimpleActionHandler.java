package com.example.ruleengine.engine;

import java.util.Map;

/**
 * 简单动作处理器函数式接口
 *
 * 用于业务适配器快速定义动作处理器
 */
@FunctionalInterface
public interface SimpleActionHandler {

    /**
     * 执行动作
     *
     * @param actionType 动作类型
     * @param params     动作参数
     * @param context    规则执行上下文
     * @param result     规则执行结果（可修改）
     * @return 是否执行成功
     */
    boolean execute(String actionType, Map<String, Object> params, RuleContext context, RuleResult result);
}
