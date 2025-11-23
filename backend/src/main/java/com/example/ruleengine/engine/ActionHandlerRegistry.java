package com.example.ruleengine.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 动作处理器注册表
 */
@Slf4j
@Component
public class ActionHandlerRegistry {

    private final Map<String, ActionHandler> handlers = new HashMap<>();

    @Autowired
    private List<ActionHandler> actionHandlers;

    @PostConstruct
    public void init() {
        // 自动注册所有ActionHandler实现
        for (ActionHandler handler : actionHandlers) {
            register(handler);
        }
        log.info("动作处理器注册完成，共注册 {} 个处理器", handlers.size());
    }

    /**
     * 注册动作处理器
     */
    public void register(ActionHandler handler) {
        String actionType = handler.getActionType();
        if (handlers.containsKey(actionType)) {
            log.warn("动作处理器已存在，将被覆盖: {}", actionType);
        }
        handlers.put(actionType, handler);
        log.debug("注册动作处理器: {}", actionType);
    }

    /**
     * 获取动作处理器
     */
    public ActionHandler getHandler(String actionType) {
        return handlers.get(actionType);
    }

    /**
     * 检查处理器是否存在
     */
    public boolean hasHandler(String actionType) {
        return handlers.containsKey(actionType);
    }

    /**
     * 获取所有已注册的动作类型
     */
    public java.util.Set<String> getRegisteredTypes() {
        return handlers.keySet();
    }

    /**
     * 批量注册业务适配器提供的处理器
     */
    public void registerAdapterHandlers(Map<String, ActionHandler> adapterHandlers) {
        if (adapterHandlers != null) {
            for (Map.Entry<String, ActionHandler> entry : adapterHandlers.entrySet()) {
                handlers.put(entry.getKey(), entry.getValue());
                log.debug("注册业务适配器动作处理器: {}", entry.getKey());
            }
        }
    }
}
