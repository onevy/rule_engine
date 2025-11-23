package com.example.ruleengine.engine;

import java.util.Map;

/**
 * 动作处理器接口
 *
 * 用于处理规则触发后的动作，如返回数据、调用服务、发送消息等
 */
public interface ActionHandler {

    /**
     * 获取动作类型
     *
     * @return 动作类型标识
     */
    String getActionType();

    /**
     * 执行动作
     *
     * @param context 规则执行上下文
     * @param result  规则执行结果（可修改）
     * @param params  动作参数
     * @return 动作执行结果
     */
    RuleResult.ActionResult execute(RuleContext context, RuleResult result, Map<String, Object> params);
}
