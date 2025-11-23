package com.example.ruleengine.engine;

import com.example.ruleengine.entity.RuleDefinition;

import java.util.List;

/**
 * 规则编译器接口
 *
 * 负责将数据库中的规则定义转换为Drools DRL格式
 */
public interface RuleCompiler {

    /**
     * 编译单个规则
     *
     * @param rule 规则定义
     * @return DRL格式的规则内容
     */
    String compile(RuleDefinition rule);

    /**
     * 批量编译规则
     *
     * @param rules 规则定义列表
     * @return DRL格式的规则内容（包含所有规则）
     */
    String compile(List<RuleDefinition> rules);

    /**
     * 编译场景下的所有规则
     *
     * @param sceneCode 场景编码
     * @return DRL格式的规则内容
     */
    String compileByScene(String sceneCode);

    /**
     * 验证规则语法
     *
     * @param rule 规则定义
     * @return 验证结果，null表示验证通过，否则返回错误信息
     */
    String validate(RuleDefinition rule);
}
