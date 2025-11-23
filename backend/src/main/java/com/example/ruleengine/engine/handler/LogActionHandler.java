package com.example.ruleengine.engine.handler;

import com.example.ruleengine.engine.ActionHandler;
import com.example.ruleengine.engine.RuleContext;
import com.example.ruleengine.engine.RuleResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 日志记录动作处理器
 */
@Slf4j
@Component
public class LogActionHandler implements ActionHandler {

    public static final String ACTION_TYPE = "LOG";

    @Override
    public String getActionType() {
        return ACTION_TYPE;
    }

    @Override
    public RuleResult.ActionResult execute(RuleContext context, RuleResult result, Map<String, Object> params) {
        try {
            String message = params != null ? (String) params.get("message") : "";
            String level = params != null ? (String) params.getOrDefault("level", "INFO") : "INFO";

            String logMessage = String.format("[规则日志] 场景: %s, 业务ID: %s, 消息: %s",
                context.getSceneCode(),
                context.getBusinessId(),
                message);

            switch (level.toUpperCase()) {
                case "DEBUG":
                    log.debug(logMessage);
                    break;
                case "WARN":
                    log.warn(logMessage);
                    break;
                case "ERROR":
                    log.error(logMessage);
                    break;
                default:
                    log.info(logMessage);
            }

            return new RuleResult.ActionResult(ACTION_TYPE, true, message);
        } catch (Exception e) {
            return new RuleResult.ActionResult(ACTION_TYPE, false, e.getMessage());
        }
    }
}
