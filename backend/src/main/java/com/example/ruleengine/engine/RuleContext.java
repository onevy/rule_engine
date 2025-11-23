package com.example.ruleengine.engine;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 规则执行上下文
 */
@Data
public class RuleContext implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 场景编码
     */
    private String sceneCode;

    /**
     * 业务ID
     */
    private String businessId;

    /**
     * 追踪ID
     */
    private String traceId;

    /**
     * 输入数据
     */
    private Map<String, Object> inputData = new HashMap<>();

    /**
     * 扩展属性
     */
    private Map<String, Object> attributes = new HashMap<>();

    /**
     * 获取输入数据值
     */
    public Object getInput(String key) {
        return inputData.get(key);
    }

    /**
     * 设置输入数据值
     */
    public void setInput(String key, Object value) {
        inputData.put(key, value);
    }

    /**
     * 获取扩展属性
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * 设置扩展属性
     */
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }
}
