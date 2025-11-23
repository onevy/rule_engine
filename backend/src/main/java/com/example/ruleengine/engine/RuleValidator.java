package com.example.ruleengine.engine;

import com.example.ruleengine.entity.RuleDefinition;

import java.util.List;

/**
 * 规则验证器接口
 */
public interface RuleValidator {

    /**
     * 验证规则定义
     *
     * @param rule 规则定义
     * @return 验证结果，空列表表示验证通过
     */
    List<String> validate(RuleDefinition rule);

    /**
     * 验证DRL语法
     *
     * @param drl DRL规则内容
     * @return 验证结果，null表示验证通过，否则返回错误信息
     */
    String validateDrl(String drl);

    /**
     * 验证条件配置
     *
     * @param jsonConfig JSON格式的条件配置
     * @return 验证结果，空列表表示验证通过
     */
    List<String> validateConditions(String jsonConfig);
}
