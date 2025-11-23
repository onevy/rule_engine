package com.example.ruleengine.engine.handler;

import com.example.ruleengine.engine.ActionHandler;
import com.example.ruleengine.engine.RuleContext;
import com.example.ruleengine.engine.RuleResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 修改数据动作处理器
 */
@Component
public class ModifyActionHandler implements ActionHandler {

    public static final String ACTION_TYPE = "MODIFY";

    @Override
    public String getActionType() {
        return ACTION_TYPE;
    }

    @Override
    public RuleResult.ActionResult execute(RuleContext context, RuleResult result, Map<String, Object> params) {
        try {
            // 修改上下文中的数据
            if (params != null) {
                String field = (String) params.get("field");
                Object value = params.get("value");

                if (field != null) {
                    context.setInput(field, value);
                    result.setOutput(field, value);
                }
            }

            return new RuleResult.ActionResult(ACTION_TYPE, true, params);
        } catch (Exception e) {
            return new RuleResult.ActionResult(ACTION_TYPE, false, e.getMessage());
        }
    }
}
