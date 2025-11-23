package com.example.ruleengine.engine.handler;

import com.example.ruleengine.engine.ActionHandler;
import com.example.ruleengine.engine.RuleContext;
import com.example.ruleengine.engine.RuleResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 返回数据动作处理器
 */
@Component
public class ReturnActionHandler implements ActionHandler {

    public static final String ACTION_TYPE = "RETURN";

    @Override
    public String getActionType() {
        return ACTION_TYPE;
    }

    @Override
    public RuleResult.ActionResult execute(RuleContext context, RuleResult result, Map<String, Object> params) {
        try {
            // 将参数中的数据添加到输出结果
            if (params != null) {
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    result.setOutput(entry.getKey(), entry.getValue());
                }
            }

            return new RuleResult.ActionResult(ACTION_TYPE, true, params);
        } catch (Exception e) {
            return new RuleResult.ActionResult(ACTION_TYPE, false, e.getMessage());
        }
    }
}
