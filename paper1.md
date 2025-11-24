# 评估量表模型规则引擎实现分析与设计

## 1. 业务需求分析

根据需求描述，需要实现两种类型的评估量表计算：

### 1.1 总分区间型（如老年人生活自理能力评估）
*   **逻辑**：
    1.  **输入**：所有评估题目的得分（例如：Item1, Item2, ... ItemN）。
    2.  **处理**：计算总分 `Total = Sum(Item1...ItemN)`。
    3.  **判断**：根据 `Total` 所在的数值区间（例如 0-3, 4-8, 9-18, >19）判定结果。
    4.  **输出**：对应的评估等级（如“可自理”、“轻度依赖”等）。

### 1.2 多维度体质评估型（如中医体质评估）
*   **逻辑**：
    1.  **输入**：所有评估题目的得分。
    2.  **处理**：
        *   将题目按“体质类型”分组（例如：气虚质题目组、阳虚质题目组）。
        *   分别计算每组的转化分或总分。
        *   例如：`Score_A = Sum(GroupA_Items)`。
    3.  **判断**：
        *   针对每种体质，根据其得分及其他条件（如“是否兼有其他体质”）判断结果。
        *   例如：“若 Score_A >= 11 则是倾向是”。
    4.  **输出**：每种体质的评估结果。

---

## 2. 规则引擎现状分析

经过对 `onevy/rule_engine` 代码库的分析，得出以下结论：

### 2.1 核心能力
*   **基于 Drools**：底层使用 Drools 规则引擎，具备强大的规则推理能力。
*   **结构化配置**：通过 `RuleCondition`（条件）和 `RuleAction`（动作）表配置规则。
*   **条件表达**：支持 `EQ` (等于), `GT` (大于), `BETWEEN` (介于) 等操作符，但**仅限于单字段比较**。
*   **扩展机制**：提供了 `BusinessAdapter` 接口，允许在规则执行前后进行数据处理 (`preProcess`, `postProcess`)。

### 2.2 存在差距 (Gap Analysis)
*   **缺乏计算能力**：目前的 `RuleCompilerImpl` 将配置直接翻译为 Drools 的 `eval` 表达式，例如 `eval($data.get("field") > 10)`。它**不支持**在规则条件中定义复杂的算术运算（如 `Item1 + Item2 > 10`）或聚合函数（Sum, Avg）。
*   **配置限制**：UI/数据库层面的 `RuleCondition` 结构不支持定义“多字段求和”这种逻辑。

---

## 3. 技术设计方案

鉴于上述分析，建议采用 **“适配器预计算 + 规则引擎判定”** 的方案。此方案利用现有的 `BusinessAdapter` 扩展点来弥补规则引擎计算能力的不足，无需修改规则引擎核心代码即可满足需求。

### 3.1 方案架构

1.  **数据预处理 (BusinessAdapter)**：
    *   在进入规则引擎前，通过 Java 代码计算“总分”或“维度分”。
    *   将计算结果作为新的字段注入到 `RuleContext` 中。
2.  **规则判定 (Rule Engine)**：
    *   规则配置不再关注具体的题目加和，而是直接针对“总分”或“维度分”进行区间判断。

### 3.2 详细设计

#### 3.2.1 场景一：总分区间型（老年人评估）

**1. 适配器实现 (`ElderlyAssessmentAdapter`)**

```java
@Component
public class ElderlyAssessmentAdapter implements BusinessAdapter {

    @Override
    public String getSceneCode() {
        return "ELDERLY_ASSESSMENT";
    }

    @Override
    public Map<String, Object> preProcess(Object originalData) {
        Map<String, Object> input = (Map<String, Object>) originalData;
        
        // 1. 获取所有题目得分
        // 假设 input 中包含 "q1", "q2", ... "qN"
        
        // 2. 计算总分
        double totalScore = 0;
        for (Map.Entry<String, Object> entry : input.entrySet()) {
            if (entry.getKey().startsWith("q") && entry.getValue() instanceof Number) {
                totalScore += ((Number) entry.getValue()).doubleValue();
            }
        }
        
        // 3. 将总分注入数据上下文
        input.put("total_score", totalScore);
        return input;
    }
}
```

**2. 规则配置 (Rule Definition)**

*   **规则1（可自理）**：
    *   条件：`field="total_score"`, `operator="BETWEEN"`, `value="[0, 3]"`
    *   动作：`returnResult="能力完好"`
*   **规则2（轻度依赖）**：
    *   条件：`field="total_score"`, `operator="BETWEEN"`, `value="[4, 8]"`
    *   动作：`returnResult="轻度依赖"`
*   ...以此类推。

#### 3.2.2 场景二：多维度体质评估

**1. 适配器实现 (`ConstitutionAssessmentAdapter`)**

```java
@Component
public class ConstitutionAssessmentAdapter implements BusinessAdapter {

    // 定义各体质对应的题目映射
    private static final Map<String, List<String>> TYPE_MAPPING = new HashMap<>();
    static {
        TYPE_MAPPING.put("qi_xu", Arrays.asList("q2", "q3", "q4", "q14")); // 气虚质题目
        TYPE_MAPPING.put("yang_xu", Arrays.asList("q11", "q12", "q13", "q29")); // 阳虚质题目
        // ... 其他体质
    }

    @Override
    public String getSceneCode() {
        return "CONSTITUTION_ASSESSMENT";
    }

    @Override
    public Map<String, Object> preProcess(Object originalData) {
        Map<String, Object> input = (Map<String, Object>) originalData;
        Map<String, Object> result = new HashMap<>(input);

        // 针对每种体质计算得分
        for (Map.Entry<String, List<String>> entry : TYPE_MAPPING.entrySet()) {
            String type = entry.getKey();
            List<String> questions = entry.getValue();
            
            double sum = 0;
            for (String q : questions) {
                Object val = input.get(q);
                if (val instanceof Number) {
                    sum += ((Number) val).doubleValue();
                }
            }
            
            // 注入该体质的得分
            result.put(type + "_score", sum);
        }
        
        return result;
    }
}
```

**2. 规则配置 (Rule Definition)**

*   **规则1（气虚质-是）**：
    *   条件：`field="qi_xu_score"`, `operator="GTE"`, `value="11"`
    *   动作：`returnResult="气虚质:是"`
*   **规则2（气虚质-倾向是）**：
    *   条件：
        *   `field="qi_xu_score"`, `operator="BETWEEN"`, `value="[9, 10]"`
    *   动作：`returnResult="气虚质:倾向是"`

---

## 4. 结论

**结论**：该需求**可以使用**本规则引擎实现。

**建议**：
1.  **采用适配器模式**：利用 `BusinessAdapter.preProcess` 封装复杂的算术运算（求和、分组计算）。
2.  **保持规则纯粹**：规则引擎专注于“业务决策”（即分数与结果的对应关系），而非“数值计算”。
3.  **未来扩展**：如果希望在规则配置界面直接定义计算公式（如 `Score = q1 + q2`），则需要对 `RuleCompilerImpl` 进行深度改造，支持表达式解析或脚本执行（Script Type Rule）。但在当前阶段，适配器模式是成本最低且最稳定的方案。
