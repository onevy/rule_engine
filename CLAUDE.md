# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在此代码库中工作时提供指导说明。

## 项目概述

这是一个为医疗健康应用设计的**通用规则引擎**平台。系统使用：
- **后端**：Spring Boot 2.7.14 + JDK 11 + MyBatis-Plus + Drools 规则引擎
- **前端**：Vue 3.x + Element Plus，包含查询构建器组件
- **数据库**：MySQL 8.0+

核心概念是一个**业务无关的规则引擎**，不同的医疗健康领域（传染病上报、处方审核、病历质控）可以通过**适配器插件**进行集成。

## 架构原则

1. **核心引擎与业务分离**：规则引擎独立于具体业务逻辑运行
2. **插件化业务集成**：每个业务场景实现 `BusinessAdapter` 接口
3. **统一配置界面**：所有业务场景共享相同的规则配置界面，允许业务人员通过可视化界面动态配置规则,并解析成Drools的语法后存储到数据库中
4. **抽象数据模型**：使用通用的 Map<String, Object> 结构；业务层处理转换

## 系统架构

```
业务层（适配器）
    ↓
规则引擎核心（Drools）
    ↓
数据持久层
```

## 核心接口

### RuleEngine 接口
- `execute(RuleContext context)`：为给定上下文执行规则
- `batchExecute(List<RuleContext> contexts)`：批量执行
- `reload(String sceneCode)`：重新加载场景的规则
- `validate(String ruleContent)`：验证规则语法

### BusinessAdapter 接口
每个业务场景必须实现：
- `getSceneCode()`：返回唯一的场景标识符
- `preProcess(Object originalData)`：将业务数据转换为规则引擎格式
- `postProcess(RuleResult ruleResult)`：将规则结果转换回业务格式
- `getActionHandlers()`：注册自定义动作处理器（可选）

## 数据库模式

系统使用 9 个核心表：

1. **rule_scene**：业务场景定义，包含输入/输出模式
2. **rule_group**：规则分组，包含执行模式（ALL/FIRST）
3. **rule_definition**：规则元数据（编码、名称、类型、优先级、版本）
4. **rule_content**：实际规则内容（JSON、DRL、Groovy、JavaScript）
5. **rule_condition**：条件配置，包含操作符（EQ、GT、IN、LIKE 等）
6. **rule_action**：动作定义（RETURN、CALL_SERVICE、SEND_MESSAGE）
7. **rule_metadata**：场景特定的字段定义，用于界面配置
8. **rule_execution_log**：执行审计日志
9. **rule_version**：规则版本历史

## 规则类型

- **CONDITION**：标准条件规则
- **DECISION_TABLE**：决策表规则
- **SCRIPT**：脚本规则

## 规则执行流程

1. 业务适配器将业务数据转换为 `Map<String, Object>`
2. 使用场景编码和输入数据创建 `RuleContext`
3. 规则引擎为场景加载/缓存 `KieBase`
4. 通过 Drools 会话执行规则
5. 收集包含匹配规则和动作的 `RuleResult`
6. 业务适配器将结果转换回业务格式
7. 将执行记录写入数据库

## 前端查询构建器

Vue 前端使用查询构建器组件，允许业务用户：
- 选择场景和规则组
- 可视化配置条件（字段、操作符、值）
- 定义动作（返回数据、修改数据、记录消息）
- 界面生成 Drools DRL 语法并存储到数据库

## 条件操作符

支持的操作符：`EQ`、`NEQ`、`GT`、`GTE`、`LT`、`LTE`、`IN`、`NOT_IN`、`LIKE`、`BETWEEN`、`IS_NULL`、`IS_NOT_NULL`

条件支持：
- **条件组**：同组 = AND，不同组 = OR
- **值类型**：STRING、NUMBER、BOOLEAN、DATE、ARRAY、OBJECT
- **左/右类型**：FIELD、CONSTANT、EXPRESSION

## 添加新业务场景

集成新业务场景的步骤：

1. 在 `rule_scene` 表中插入场景定义，包含输入/输出模式
2. 实现包含场景特定逻辑的 `BusinessAdapter` 接口
3. 将适配器注册为 Spring `@Component`
4. 在 `rule_metadata` 中定义字段元数据，用于界面配置
5. 使用统一的 `/api/rule-engine/execute` 端点

## API 端点

**规则执行：**
- `POST /api/rule-engine/execute`：为场景执行规则
- `POST /api/rule-engine/batch-execute`：批量执行
- `POST /api/rule-engine/test`：使用示例数据测试规则
- `POST /api/rule-engine/reload/{sceneCode}`：重新加载规则缓存

**规则配置：**
- `POST /api/rule-engine/rule/create`：创建新规则
- `PUT /api/rule-engine/rule/update/{ruleId}`：更新现有规则
- `DELETE /api/rule-engine/rule/delete/{ruleId}`：删除规则
- `GET /api/rule-engine/rule/list`：分页查询规则
- `GET /api/rule-engine/metadata/{sceneCode}`：获取场景元数据用于界面

## 业务适配器示例

PRD 描述了三个参考实现：

1. **InfectiousDiseaseAdapter**：分析诊断/化验/检查数据，判断是否需要上报传染病
2. **PrescriptionAuditAdapter**：检查药物相互作用、剂量、禁忌症与患者情况的匹配
3. **MedicalRecordQCAdapter**：病历质控检查（完整性、有效性、费用比例）

## 规则编译器

`RuleCompiler` 组件：
- 将数据库规则定义转换为 Drools DRL 语法
- 支持多种规则类型（CONDITION、DECISION_TABLE、SCRIPT）
- 构建具有正确 AND/OR 分组的条件
- 生成动作（RETURN、MODIFY、LOG）
- 编译前验证规则语法

## 性能考虑

- 规则按场景编码编译并缓存在 `KieBase` 缓存中
- 规则更新后使用 `reload()` 刷新缓存
- 支持并行批量执行
- 执行日志包含计时指标（毫秒）

## 开发工作流

由于当前是设计文档仓库：

1. 根据 SQL 定义实现数据库模式
2. 创建 Spring Boot 项目结构
3. 实现核心接口（RuleEngine、BusinessAdapter、ActionHandler）
4. 构建带有 KieBase 管理的 DroolsRuleEngine
5. 实现用于 DRL 生成的 RuleCompiler
6. 创建集成查询构建器的 Vue 3 前端
7. 实现示例业务适配器
8. 添加带有示例场景的综合测试

## 关键设计模式

- **策略模式**：针对不同场景的不同业务适配器
- **建造者模式**：规则编译和 DRL 生成
- **模板方法模式**：带有钩子的通用规则执行流程
- **旁路缓存模式**：KieBase 懒加载缓存
