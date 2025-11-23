package com.example.ruleengine.engine;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 规则执行结果
 */
@Data
public class RuleResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否匹配到规则
     */
    private boolean matched = false;

    /**
     * 匹配的规则编码列表
     */
    private List<String> matchedRuleCodes = new ArrayList<>();

    /**
     * 输出数据
     */
    private Map<String, Object> outputData = new HashMap<>();

    /**
     * 执行的动作列表
     */
    private List<ActionResult> actionResults = new ArrayList<>();

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 执行时间（毫秒）
     */
    private long executionTime;

    /**
     * 设置输出数据值
     */
    public void setOutput(String key, Object value) {
        outputData.put(key, value);
    }

    /**
     * 获取输出数据值
     */
    public Object getOutput(String key) {
        return outputData.get(key);
    }

    /**
     * 添加匹配的规则编码
     */
    public void addMatchedRuleCode(String ruleCode) {
        this.matched = true;
        this.matchedRuleCodes.add(ruleCode);
    }

    /**
     * 添加动作执行结果
     */
    public void addActionResult(ActionResult actionResult) {
        this.actionResults.add(actionResult);
    }

    /**
     * 动作执行结果
     */
    @Data
    public static class ActionResult implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 动作类型
         */
        private String actionType;

        /**
         * 是否成功
         */
        private boolean success;

        /**
         * 结果数据
         */
        private Object resultData;

        /**
         * 错误信息
         */
        private String errorMessage;

        public ActionResult() {
        }

        public ActionResult(String actionType, boolean success, Object resultData) {
            this.actionType = actionType;
            this.success = success;
            this.resultData = resultData;
        }

        public ActionResult(String actionType, boolean success, String errorMessage) {
            this.actionType = actionType;
            this.success = success;
            this.errorMessage = errorMessage;
        }
    }
}
