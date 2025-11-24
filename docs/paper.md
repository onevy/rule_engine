# 评估量表规则引擎实现方案

## 一、业务背景

评估量表是医疗健康领域常用的标准化评估工具，通过一系列问题收集患者信息，根据预设的计分规则计算得分，最终得出评估结论。本文档分析如何使用通用规则引擎实现评估量表的结构化规则计算。

---

## 二、业务需求分析

### 2.1 类型一：总分区间判定模型

**典型案例：老年人生活自理能力评估表**

#### 2.1.1 量表结构

| 评估维度 | 可自理(0分) | 轻度依赖(1分) | 中度依赖 | 不能自理       |
|---------|------------|--------------|---------|------------|
| 进餐 | 独立完成 | - | 需协助(3分) | 完全需要帮助(5分) |
| 梳洗 | 独立完成 | 需协助 | 部分完成(3分) | 完全需要帮助(7分) |
| 穿衣 | 独立完成 | - | 部分完成(3分) | 完全需要帮助(5分) |
| 如厕 | 不需协助 | 偶尔失禁(1分) | 经常失禁(5分) | 完全失禁(10分)  |
| 活动 | 独立完成 | 借助辅助(1分) | 借助较大外力(5分) | 卧床不起(10分)  |

#### 2.1.2 判定规则

```
总分 = 进餐得分 + 梳洗得分 + 穿衣得分 + 如厕得分 + 活动得分

判定结果：
- 0 ~ 3 分  → 可自理
- 4 ~ 8 分  → 轻度依赖
- 9 ~ 18 分 → 中度依赖
- ≥ 19 分   → 不能自理
```

#### 2.1.3 规则抽象——总分区间型（如老年人生活自理能力评估）

*   **逻辑**：
    1.  **输入**：所有评估题目的得分（例如：q1, q2, ... qN）。
    2.  **处理**：计算总分 `totalScore = Sum(q1...qN)`。
    3.  **判断**：根据 `totalScore` 所在的数值区间（例如 0-3, 4-8, 9-18, >19）判定结果。
    4.  **输出**：对应的评估等级（如"可自理"、"轻度依赖"等）。

---


### 2.2 类型二：多维度分组计算模型

**典型案例：中老年体质评估量表（老年人中医药健康管理服务记录表）**

#### 2.2.1 量表结构

- **题目数量**：33道题
- **选项分值**：每题5个选项，分值1-5分
  - 没有(1分) / 很少(2分) / 有时(3分) / 经常(4分) / 总是(5分)
- **体质类型**：9种

#### 2.2.2 体质类型与题目映射

| 体质类型 | 对应题目编号 | 计分说明 |
|---------|-------------|---------|
| 气虚质 | (2)(3)(4)(14) | 正向计分 |
| 阳虚质 | (11)(12)(13)(29) | 正向计分 |
| 阴虚质 | (10)(21)(26)(31) | 正向计分 |
| 痰湿质 | (9)(16)(28)(32) | 正向计分 |
| 湿热质 | (23)(25)(27)(30) | 正向计分 |
| 血瘀质 | (19)(22)(24)(33) | 正向计分 |
| 气郁质 | (5)(6)(7)(8) | 正向计分 |
| 特禀质 | (15)(17)(18)(20) | 正向计分 |
| 平和质 | (1)(2)(4)(5)(13) | 其中(2)(4)(5)(13)反向计分：1→5, 2→4, 3→3, 4→2, 5→1 |

#### 2.2.3 判定规则

**偏颇体质（8种）判定标准：**

| 条件 | 判定结果 |
|-----|---------|
| 各条目得分相加 ≥ 11 分 | 是 |
| 各条目得分相加 9 ~ 10 分 | 倾向是 |
| 各条目得分相加 < 8 分 | 否 |

**平和质判定标准：**

| 条件 | 判定结果 |
|-----|---------|
| 平和质得分 ≥ 17 分 且 其他8种体质得分均 < 8 分 | 是 |
| 平和质得分 ≥ 17 分 且 其他8种体质得分均 < 10 分 | 基本是 |
| 不满足上述条件 | 否 |

#### 2.2.4 规则抽象——多维度体质评估型（如中医体质评估）

*   **逻辑**：
    1.  **输入**：所有评估题目的得分{q1, q2, ..., q33}。
    2.  **处理**：
        *   将题目按“体质类型”分组（例如：气虚质题目组、阳虚质题目组）。
        *   分别计算每组的转化分或总分。
        *   例如：`Score_A = Sum(GroupA_Items)`。
    3.  **判断**：
        *   针对每种体质，根据其得分及其他条件（如“是否兼有其他体质”）判断结果。
        *   例如：“若 Score_A >= 11 则是倾向是”。
    4.  **输出**：每种体质的评估结果。


```
输入：33道题的得分 {q1, q2, ..., q33}

计算过程：
1. 反向计分处理：q2_r, q4_r, q5_r, q13_r = 6 - 原分值
2. 分组求和：
   - 气虚质得分 = q2 + q3 + q4 + q14
   - 阳虚质得分 = q11 + q12 + q13 + q29
   - 阴虚质得分 = q10 + q21 + q26 + q31
   - 痰湿质得分 = q9 + q16 + q28 + q32
   - 湿热质得分 = q23 + q25 + q27 + q30
   - 血瘀质得分 = q19 + q22 + q24 + q33
   - 气郁质得分 = q5 + q6 + q7 + q8
   - 特禀质得分 = q15 + q17 + q18 + q20
   - 平和质得分 = q1 + q2_r + q4_r + q5_r + q13_r

3. 判定各体质结果（需要条件组合判断）

输出：9种体质的判定结果
```
---

## 三、规则引擎能力评估

### 3.1 当前能力矩阵

| 能力需求 | 当前状态 | 详细说明 |
|---------|---------|---------|
| 字段值比较 | ✅ 支持 | EQ, NEQ, GT, GTE, LT, LTE |
| 区间判断 | ✅ 支持 | BETWEEN 操作符 |
| 集合判断 | ✅ 支持 | IN, NOT_IN |
| 条件组合(AND/OR) | ✅ 支持 | 通过条件组实现 |
| 多规则匹配 | ✅ 支持 | 返回所有匹配规则 |
| 多结果输出 | ✅ 支持 | outputData 支持多字段 |
| **聚合计算(SUM)** | ❌ 不支持 | 需要扩展 |
| **表达式计算** | ❌ 不支持 | 需要扩展 |
| **字段间运算** | ❌ 不支持 | 需要扩展 |
| **反向计分** | ❌ 不支持 | 需要扩展 |

### 3.2 可行性结论

**当前规则引擎无法直接支持评估量表的计算需求**，主要缺失：

1. **聚合计算能力**：无法对多个字段求和
2. **表达式计算能力**：无法进行字段间的数学运算
3. **数据预处理能力**：无法进行反向计分等转换

---

## 四、技术实现方案

### 4.1 方案对比

| 方案 | 描述 | 优点 | 缺点 | 推荐度 |
|-----|------|-----|-----|-------|
| 方案A | 适配器预处理 | 实现简单，不改动核心 | 计算逻辑硬编码，不灵活 | ★★★ |
| 方案B | 扩展规则引擎 | 灵活通用，支持更多场景 | 开发工作量大 | ★★★★ |
| 方案C | 混合方案 | 兼顾灵活性和实现成本 | 需要两种配置方式 | ★★★★★ |

**推荐：方案C（混合方案）**

- 短期：使用适配器完成分数计算，规则引擎负责区间判定
- 长期：扩展规则引擎支持聚合和表达式计算

---

### 4.2 方案C详细设计

#### 4.2.1 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                      评估量表业务层                           │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐    ┌─────────────────────────────────┐ │
│  │ 量表答案输入     │───▶│ AssessmentScaleAdapter          │ │
│  │ {q1:3,q2:4,...} │    │ ① 反向计分处理                   │ │
│  └─────────────────┘    │ ② 分组求和计算                   │ │
│                         │ ③ 构建规则引擎输入               │ │
│                         └───────────────┬─────────────────┘ │
├─────────────────────────────────────────┼───────────────────┤
│                      规则引擎核心        ▼                   │
│  ┌─────────────────────────────────────────────────────────┐│
│  │ 输入数据：                                               ││
│  │ {                                                       ││
│  │   totalScore: 15,        // 总分（类型一）               ││
│  │   qixu_score: 12,        // 气虚质得分（类型二）          ││
│  │   yangxu_score: 8,       // 阳虚质得分                   ││
│  │   ...                                                   ││
│  │ }                                                       ││
│  ├─────────────────────────────────────────────────────────┤│
│  │ 规则配置（BETWEEN区间判定）：                             ││
│  │ Rule1: totalScore BETWEEN [0,3] → 可自理                ││
│  │ Rule2: totalScore BETWEEN [4,8] → 轻度依赖              ││
│  │ Rule3: qixu_score >= 11 → 气虚质=是                     ││
│  │ ...                                                     ││
│  └───────────────────────────────┬─────────────────────────┘│
├──────────────────────────────────┼──────────────────────────┤
│                      输出结果     ▼                          │
│  {                                                          │
│    "selfCareAbility": "中度依赖",                            │
│    "constitutionResults": {                                 │
│      "气虚质": "是", "阳虚质": "否", "平和质": "基本是"        │
│    }                                                        │
│  }                                                          │
└─────────────────────────────────────────────────────────────┘
```

#### 4.2.2 数据模型设计

##### 输入数据结构

> **统一字段命名**：类型一和类型二均采用 `q1, q2, ... qN` 格式，支持任意数量的评估题目

**类型一：总分区间判定**

```json
{
  "sceneCode": "SELF_CARE_ASSESSMENT",
  "inputData": {
    "q1": 3,
    "q2": 3,
    "q3": 3,
    "q4": 5,
    "q5": 1
  }
}
```

**字段说明：**
| 字段 | 对应维度 | 示例值 |
|-----|---------|-------|
| q1 | 进餐 | 0/3/5 |
| q2 | 梳洗 | 0/1/3/7 |
| q3 | 穿衣 | 0/3/5 |
| q4 | 如厕 | 0/1/5/10 |
| q5 | 活动 | 0/1/5/10 |

> 注：具体字段与业务维度的映射关系通过场景元数据配置

**类型二：多维度体质评估**

```json
{
  "sceneCode": "CONSTITUTION_ASSESSMENT",
  "inputData": {
    "q1": 4, "q2": 3, "q3": 2, "q4": 3, "q5": 2,
    "q6": 2, "q7": 3, "q8": 2, "q9": 4, "q10": 3,
    "q11": 2, "q12": 3, "q13": 2, "q14": 3, "q15": 2,
    "q16": 3, "q17": 2, "q18": 3, "q19": 2, "q20": 3,
    "q21": 2, "q22": 3, "q23": 4, "q24": 2, "q25": 3,
    "q26": 2, "q27": 3, "q28": 4, "q29": 2, "q30": 3,
    "q31": 2, "q32": 3, "q33": 2
  }
}
```

##### 适配器处理后的数据

**类型一处理后：**

```json
{
  "totalScore": 15
}
```

**类型二处理后：**

```json
{
  "qixu_score": 11,
  "yangxu_score": 9,
  "yinxu_score": 9,
  "tanshi_score": 14,
  "shire_score": 13,
  "xueyu_score": 9,
  "qiyu_score": 9,
  "tebing_score": 10,
  "pinghe_score": 18,
  "other_max_score": 14
}
```

##### 输出数据结构

**类型一输出：**

```json
{
  "matched": true,
  "outputData": {
    "assessmentResult": "中度依赖",
    "totalScore": 15,
    "suggestion": "建议加强日常照护，重点关注如厕和活动能力"
  }
}
```

**类型二输出：**

```json
{
  "matched": true,
  "outputData": {
    "primaryConstitution": "痰湿质",
    "constitutionResults": {
      "气虚质": { "score": 11, "result": "是" },
      "阳虚质": { "score": 9, "result": "倾向是" },
      "阴虚质": { "score": 9, "result": "倾向是" },
      "痰湿质": { "score": 14, "result": "是" },
      "湿热质": { "score": 13, "result": "是" },
      "血瘀质": { "score": 9, "result": "倾向是" },
      "气郁质": { "score": 9, "result": "倾向是" },
      "特禀质": { "score": 10, "result": "倾向是" },
      "平和质": { "score": 18, "result": "否" }
    },
    "healthGuidance": ["情志调摄", "饮食调养", "起居调摄", "运动保健", "穴位保健"]
  }
}
```

---

### 4.3 业务适配器实现

#### 4.3.0 通用设计原则

> **核心思想**：将业务逻辑与数据结构解耦，通过配置化实现扩展性

**两类通用适配器：**

| 适配器类型 | 适用场景 | 字段模式 | 计算逻辑 |
|-----------|---------|---------|---------|
| TotalScoreAssessmentAdapter | 总分区间判定 | `q{n}` | SUM(所有q) → 区间判定 |
| GroupScoreAssessmentAdapter | 多维度分组计算 | `q{n}` | 分组SUM → 各组独立判定 |

> **统一字段模式**：两种类型均使用 `q{n}` 格式（q1, q2, ... qN），简化数据结构设计

**设计要点：**
1. **字段动态识别**：通过正则表达式匹配 `q\d+` 模式
2. **配置化映射**：字段含义和分组关系通过数据库元数据配置
3. **计算逻辑复用**：同一适配器支持不同数量题目的量表
4. **规则与计算分离**：适配器负责计算，规则引擎负责判定

```
┌──────────────────────────────────────────────────────────────────┐
│                        数据流转示意                               │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  原始输入                    适配器处理                规则判定   │
│  ────────                    ────────                ────────   │
│                                                                  │
│  类型一:                                                         │
│  {q1:3,q2:3,q3:3,...}   ──▶  {totalScore:15}  ──▶  "中度依赖"    │
│                                                                  │
│  类型二:                                                         │
│  {q1:4,q2:3,...,q33:2}  ──▶  {qixu_score:11,    ──▶  气虚质=是   │
│                               yangxu_score:9,        阳虚质=倾向是│
│                               ...}                   ...         │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

#### 4.3.1 通用总分计算适配器

> 设计原则：通过正则匹配 `q\d+` 模式自动识别题目字段，支持任意数量的评估题目

```java
@Component
public class TotalScoreAssessmentAdapter implements BusinessAdapter {

    // 题目字段匹配模式：q1, q2, ... qN
    private static final Pattern QUESTION_PATTERN = Pattern.compile("^q(\\d+)$");

    @Override
    public String getSceneCode() {
        return "TOTAL_SCORE_ASSESSMENT";  // 通用场景编码
    }

    @Override
    public Map<String, Object> preProcess(Object originalData) {
        Map<String, Object> input = (Map<String, Object>) originalData;
        Map<String, Object> processed = new HashMap<>();

        // 动态识别并累加所有 q{n} 字段
        int totalScore = 0;
        int itemCount = 0;

        for (Map.Entry<String, Object> entry : input.entrySet()) {
            String key = entry.getKey();
            Matcher matcher = QUESTION_PATTERN.matcher(key);

            if (matcher.matches() && entry.getValue() instanceof Number) {
                int score = ((Number) entry.getValue()).intValue();
                totalScore += score;
                itemCount++;
                // 保留原始题目得分
                processed.put(key, score);
            }
        }

        // 输出计算结果
        processed.put("totalScore", totalScore);
        processed.put("itemCount", itemCount);

        return processed;
    }

    @Override
    public Object postProcess(RuleResult ruleResult) {
        // 可以添加额外的健康建议等后处理逻辑
        return ruleResult.getOutputData();
    }
}
```

**适配器特点：**
- 自动识别 `q1, q2, ... qN` 格式的字段
- 支持任意数量的评估题目
- 输出 `totalScore`（总分）和 `itemCount`（题目数量）
- 同一适配器可复用于多种"总分区间判定"类型的量表

#### 4.3.2 多维度分组计算适配器

> 设计原则：从数据库配置读取分组映射关系，支持任意分组和反向计分配置

```java
@Component
public class GroupScoreAssessmentAdapter implements BusinessAdapter {

    @Autowired
    private AssessmentGroupMappingMapper groupMappingMapper;

    // 题目字段匹配模式：q1, q2, ... qN
    private static final Pattern QUESTION_PATTERN = Pattern.compile("^q(\\d+)$");

    @Override
    public String getSceneCode() {
        return "GROUP_SCORE_ASSESSMENT";  // 通用场景编码
    }

    @Override
    public Map<String, Object> preProcess(Object originalData) {
        Map<String, Object> input = (Map<String, Object>) originalData;
        String sceneCode = (String) input.get("sceneCode");
        Map<String, Object> processed = new HashMap<>();

        // 从数据库获取分组配置
        List<AssessmentGroupMapping> groupMappings = groupMappingMapper.selectBySceneCode(sceneCode);

        // 记录非平和质（或主体质）的最高分
        int maxOtherScore = 0;
        String primaryGroupCode = "pinghe";  // 可配置化

        for (AssessmentGroupMapping mapping : groupMappings) {
            String groupCode = mapping.getGroupCode();
            List<Integer> itemList = parseJsonArray(mapping.getItemList());
            Set<Integer> reverseItems = parseJsonArrayToSet(mapping.getReverseItems());

            // 计算该分组的总分
            int groupScore = 0;
            for (Integer itemNum : itemList) {
                int qScore = getQuestionScore(input, itemNum);
                // 检查是否需要反向计分
                if (reverseItems.contains(itemNum)) {
                    qScore = 6 - qScore;  // 反向计分：1→5, 2→4, 3→3, 4→2, 5→1
                }
                groupScore += qScore;
            }
            processed.put(groupCode + "_score", groupScore);

            // 记录非主体质的最高分
            if (!primaryGroupCode.equals(groupCode)) {
                maxOtherScore = Math.max(maxOtherScore, groupScore);
            }
        }

        // 添加其他分组最高分（用于复合条件判定）
        processed.put("other_max_score", maxOtherScore);

        return processed;
    }

    @Override
    public Object postProcess(RuleResult ruleResult) {
        // 整合各分组判定结果，生成健康指导建议
        return ruleResult.getOutputData();
    }

    private int getQuestionScore(Map<String, Object> input, int questionNum) {
        Object value = input.get("q" + questionNum);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }

    private List<Integer> parseJsonArray(String json) {
        // 解析 JSON 数组字符串为 List<Integer>
        // 实际实现使用 Jackson 或 Gson
        return JSON.parseArray(json, Integer.class);
    }

    private Set<Integer> parseJsonArrayToSet(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(parseJsonArray(json));
    }
}
```

**适配器特点：**
- 从数据库 `assessment_group_mapping` 表读取分组配置
- 支持配置化的题目分组和反向计分
- 自动计算各分组得分和其他分组最高分
- 同一适配器可复用于多种"多维度分组计算"类型的量表

---

### 4.4 规则配置示例

#### 4.4.1 类型一：自理能力评估规则

**规则组配置：**

| 规则编码 | 规则名称 | 优先级 | 执行模式 |
|---------|---------|-------|---------|
| SELF_CARE_001 | 可自理判定 | 100 | FIRST_MATCH |
| SELF_CARE_002 | 轻度依赖判定 | 90 | FIRST_MATCH |
| SELF_CARE_003 | 中度依赖判定 | 80 | FIRST_MATCH |
| SELF_CARE_004 | 不能自理判定 | 70 | FIRST_MATCH |

**规则条件配置：**

```sql
-- 规则1：可自理 (0-3分)
INSERT INTO rule_condition (rule_id, field_code, operator, field_value)
VALUES (1, 'totalScore', 'BETWEEN', '0,3');

-- 规则2：轻度依赖 (4-8分)
INSERT INTO rule_condition (rule_id, field_code, operator, field_value)
VALUES (2, 'totalScore', 'BETWEEN', '4,8');

-- 规则3：中度依赖 (9-18分)
INSERT INTO rule_condition (rule_id, field_code, operator, field_value)
VALUES (3, 'totalScore', 'BETWEEN', '9,18');

-- 规则4：不能自理 (≥19分)
INSERT INTO rule_condition (rule_id, field_code, operator, field_value)
VALUES (4, 'totalScore', 'GTE', '19');
```

**规则动作配置：**

```sql
-- 规则1动作：返回可自理结果
INSERT INTO rule_action (rule_id, action_type, action_params)
VALUES (1, 'RETURN', '{"assessmentResult": "可自理", "level": 1}');

-- 规则2动作：返回轻度依赖结果
INSERT INTO rule_action (rule_id, action_type, action_params)
VALUES (2, 'RETURN', '{"assessmentResult": "轻度依赖", "level": 2}');

-- 规则3动作：返回中度依赖结果
INSERT INTO rule_action (rule_id, action_type, action_params)
VALUES (3, 'RETURN', '{"assessmentResult": "中度依赖", "level": 3}');

-- 规则4动作：返回不能自理结果
INSERT INTO rule_action (rule_id, action_type, action_params)
VALUES (4, 'RETURN', '{"assessmentResult": "不能自理", "level": 4}');
```

#### 4.4.2 类型二：体质评估规则

**偏颇体质判定规则（以气虚质为例）：**

```sql
-- 气虚质=是 (≥11分)
INSERT INTO rule_condition (rule_id, field_code, operator, field_value)
VALUES (10, 'qixu_score', 'GTE', '11');
INSERT INTO rule_action (rule_id, action_type, action_params)
VALUES (10, 'RETURN', '{"qixu_result": "是"}');

-- 气虚质=倾向是 (9-10分)
INSERT INTO rule_condition (rule_id, field_code, operator, field_value)
VALUES (11, 'qixu_score', 'BETWEEN', '9,10');
INSERT INTO rule_action (rule_id, action_type, action_params)
VALUES (11, 'RETURN', '{"qixu_result": "倾向是"}');

-- 气虚质=否 (<8分)
INSERT INTO rule_condition (rule_id, field_code, operator, field_value)
VALUES (12, 'qixu_score', 'LT', '8');
INSERT INTO rule_action (rule_id, action_type, action_params)
VALUES (12, 'RETURN', '{"qixu_result": "否"}');
```

**平和质判定规则（复杂条件组合）：**

```sql
-- 平和质=是：得分≥17 且 其他体质最高分<8
-- 需要使用条件组实现 AND 逻辑
INSERT INTO rule_condition (rule_id, group_id, field_code, operator, field_value)
VALUES
(20, 1, 'pinghe_score', 'GTE', '17'),
(20, 1, 'other_max_score', 'LT', '8');

INSERT INTO rule_action (rule_id, action_type, action_params)
VALUES (20, 'RETURN', '{"pinghe_result": "是"}');

-- 平和质=基本是：得分≥17 且 其他体质最高分<10（但≥8）
INSERT INTO rule_condition (rule_id, group_id, field_code, operator, field_value)
VALUES
(21, 1, 'pinghe_score', 'GTE', '17'),
(21, 1, 'other_max_score', 'BETWEEN', '8,9');

INSERT INTO rule_action (rule_id, action_type, action_params)
VALUES (21, 'RETURN', '{"pinghe_result": "基本是"}');
```

---

### 4.5 生成的DRL示例

#### 类型一：自理能力评估

```drools
package com.example.rules.selfcare

import java.util.Map

rule "SELF_CARE_001_可自理"
    salience 100
    when
        $data : Map(
            ((Number)this["totalScore"]).intValue() >= 0 &&
            ((Number)this["totalScore"]).intValue() <= 3
        )
    then
        $data.put("assessmentResult", "可自理");
        $data.put("level", 1);
end

rule "SELF_CARE_002_轻度依赖"
    salience 90
    when
        $data : Map(
            ((Number)this["totalScore"]).intValue() >= 4 &&
            ((Number)this["totalScore"]).intValue() <= 8
        )
    then
        $data.put("assessmentResult", "轻度依赖");
        $data.put("level", 2);
end

rule "SELF_CARE_003_中度依赖"
    salience 80
    when
        $data : Map(
            ((Number)this["totalScore"]).intValue() >= 9 &&
            ((Number)this["totalScore"]).intValue() <= 18
        )
    then
        $data.put("assessmentResult", "中度依赖");
        $data.put("level", 3);
end

rule "SELF_CARE_004_不能自理"
    salience 70
    when
        $data : Map(
            ((Number)this["totalScore"]).intValue() >= 19
        )
    then
        $data.put("assessmentResult", "不能自理");
        $data.put("level", 4);
end
```

#### 类型二：体质评估（气虚质示例）

```drools
package com.example.rules.constitution

import java.util.Map

rule "CONSTITUTION_QIXU_YES"
    salience 100
    when
        $data : Map(
            ((Number)this["qixu_score"]).intValue() >= 11
        )
    then
        $data.put("qixu_result", "是");
end

rule "CONSTITUTION_QIXU_TEND"
    salience 90
    when
        $data : Map(
            ((Number)this["qixu_score"]).intValue() >= 9 &&
            ((Number)this["qixu_score"]).intValue() <= 10
        )
    then
        $data.put("qixu_result", "倾向是");
end

rule "CONSTITUTION_QIXU_NO"
    salience 80
    when
        $data : Map(
            ((Number)this["qixu_score"]).intValue() < 8
        )
    then
        $data.put("qixu_result", "否");
end

// 平和质判定（复杂条件）
rule "CONSTITUTION_PINGHE_YES"
    salience 100
    when
        $data : Map(
            ((Number)this["pinghe_score"]).intValue() >= 17 &&
            ((Number)this["other_max_score"]).intValue() < 8
        )
    then
        $data.put("pinghe_result", "是");
end

rule "CONSTITUTION_PINGHE_BASIC"
    salience 90
    when
        $data : Map(
            ((Number)this["pinghe_score"]).intValue() >= 17 &&
            ((Number)this["other_max_score"]).intValue() >= 8 &&
            ((Number)this["other_max_score"]).intValue() < 10
        )
    then
        $data.put("pinghe_result", "基本是");
end
```

---

## 五、长期扩展方案

### 5.1 新增聚合计算操作符

#### 5.1.1 数据库扩展

```sql
-- 新增聚合函数类型
ALTER TABLE rule_condition ADD COLUMN aggregate_function VARCHAR(20) DEFAULT NULL
COMMENT '聚合函数: SUM, AVG, MAX, MIN, COUNT';

ALTER TABLE rule_condition ADD COLUMN aggregate_fields TEXT DEFAULT NULL
COMMENT '聚合字段列表，JSON数组格式';
```

#### 5.1.2 条件配置示例

```json
{
  "fieldCode": "totalScore",
  "aggregateFunction": "SUM",
  "aggregateFields": ["q1", "q2", "q3", "q4", "q5"],
  "operator": "BETWEEN",
  "fieldValue": "0,3"
}
```

#### 5.1.3 DRL生成逻辑扩展

```java
// RuleCompilerImpl 扩展
private String buildAggregateExpression(RuleCondition condition) {
    String function = condition.getAggregateFunction();
    List<String> fields = parseAggregateFields(condition.getAggregateFields());

    if ("SUM".equals(function)) {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) sb.append(" + ");
            sb.append(String.format("((Number)$data.get(\"%s\")).doubleValue()", fields.get(i)));
        }
        sb.append(")");
        return sb.toString();
    }
    // ... 其他聚合函数
    return "";
}
```

### 5.2 新增表达式计算支持

#### 5.2.1 数据库表扩展

> 在 `rule_condition` 表中新增 `expression` 字段用于存储计算表达式

```sql
-- 扩展 rule_condition 表，新增表达式字段
ALTER TABLE rule_condition ADD COLUMN expression TEXT DEFAULT NULL
COMMENT '计算表达式，用于 EXPRESSION 操作符，支持 SpEL 语法';

-- 新增 EXPRESSION 操作符类型说明
-- EXPRESSION 操作符用于复杂的字段间计算，表达式结果与 field_value 进行比较
```

#### 5.2.2 新增EXPRESSION操作符配置示例

```sql
-- 新增表达式类型条件
-- expression: 计算表达式，结果将与 operator + field_value 进行比较
INSERT INTO rule_condition (rule_id, group_id, field_code, operator, field_value, expression)
VALUES (100, 1, 'calc_result', 'EXPRESSION', '>=17',
        '6 - #data["q2"] + 6 - #data["q4"] + #data["q1"]');
```

**EXPRESSION 操作符说明：**
| 属性 | 说明 | 示例 |
|-----|------|-----|
| field_code | 虚拟字段名，用于标识 | `calc_result` |
| operator | 固定为 `EXPRESSION` | `EXPRESSION` |
| field_value | 比较条件 | `>=17` |
| expression | SpEL 表达式 | `#data["q1"] + #data["q2"]` |

#### 5.2.3 集成SpEL表达式引擎

```java
@Component
public class ExpressionEvaluator {

    private final SpelExpressionParser parser = new SpelExpressionParser();

    public Object evaluate(String expression, Map<String, Object> context) {
        StandardEvaluationContext evalContext = new StandardEvaluationContext();
        evalContext.setVariable("data", context);

        Expression exp = parser.parseExpression(expression);
        return exp.getValue(evalContext);
    }
}
```

---

## 六、实施路线图

### 第一阶段：基础实现（1-2周）

- [x] 业务需求分析与设计文档
- [ ] 实现 SelfCareAssessmentAdapter 适配器
- [ ] 实现 ConstitutionAssessmentAdapter 适配器
- [ ] 配置两种量表的业务场景
- [ ] 配置规则和条件
- [ ] 单元测试与集成测试

### 第二阶段：规则引擎扩展（2-3周）
- [ ] 数据库表结构扩展
- [ ] RuleCompiler 支持聚合计算
- [ ] RuleCompiler 支持表达式计算
- [ ] 前端规则配置界面支持新操作符
- [ ] 完善测试用例

### 第三阶段：优化与推广（1周）

- [ ] 性能测试与优化
- [ ] 文档完善
- [ ] 其他评估量表接入

---

## 七、附录

### 7.1 量表元数据配置

#### 7.1.1 通用字段模式配置

> 采用 `q{n}` 通用命名模式，通过元数据配置字段与业务含义的映射关系

**场景配置表（business_scene）扩展：**

> 现有 `business_scene` 表需要新增以下字段以支持评估量表场景
>
> ⚠️ **执行顺序**：以下 ALTER TABLE 语句必须在 INSERT 语句之前执行

```sql
-- =============================================
-- 步骤1：扩展 business_scene 表结构（必须先执行）
-- =============================================
ALTER TABLE business_scene ADD COLUMN item_pattern VARCHAR(50) DEFAULT 'q{n}'
COMMENT '题目字段命名模式，统一使用 q{n} 格式';

ALTER TABLE business_scene ADD COLUMN item_count INT DEFAULT 0
COMMENT '题目数量，0表示动态数量（自动识别）';
```

**新增字段说明：**

| 字段名 | 类型 | 默认值 | 用途说明 |
|-------|------|-------|---------|
| `item_pattern` | VARCHAR(50) | `q{n}` | 题目字段命名模式，适配器通过此模式识别输入数据中的题目字段。例如 `q{n}` 表示字段命名为 q1, q2, ... qN |
| `item_count` | INT | 0 | 量表题目数量。0 表示动态数量，适配器会自动统计输入数据中匹配模式的字段数量 |

**场景数据配置：**

```sql
-- =============================================
-- 步骤2：插入场景数据（在 ALTER TABLE 执行后）
-- =============================================

-- 自理能力评估场景
INSERT INTO business_scene (scene_code, scene_name, scene_desc, adapter_class, item_pattern, item_count, create_by)
VALUES ('SELF_CARE_ASSESSMENT', '老年人生活自理能力评估', '评估老年人日常生活自理能力',
        'com.example.ruleengine.adapter.TotalScoreAssessmentAdapter', 'q{n}', 5, 'system');

-- 中老年体质评估场景
INSERT INTO business_scene (scene_code, scene_name, scene_desc, adapter_class, item_pattern, item_count, create_by)
VALUES ('CONSTITUTION_ASSESSMENT', '中老年体质评估', '中医药健康管理服务体质辨识',
        'com.example.ruleengine.adapter.GroupScoreAssessmentAdapter', 'q{n}', 33, 'system');
```

#### 7.1.2 通用字段元数据配置

> **统一字段格式**：所有量表均采用 `q{n}` 格式（q1, q2, ... qN）

**自理能力评估场景元数据：**

```sql
INSERT INTO field_metadata (scene_code, field_code, field_name, field_type, sort_order, value_range) VALUES
('SELF_CARE_ASSESSMENT', 'q1', '进餐', 'NUMBER', 1, '[{"label":"独立完成","value":0},{"label":"需协助","value":3},{"label":"完全需要帮助","value":5}]'),
('SELF_CARE_ASSESSMENT', 'q2', '梳洗', 'NUMBER', 2, '[{"label":"独立完成","value":0},{"label":"需协助","value":1},{"label":"部分完成","value":3},{"label":"完全需要帮助","value":7}]'),
('SELF_CARE_ASSESSMENT', 'q3', '穿衣', 'NUMBER', 3, '[{"label":"独立完成","value":0},{"label":"部分完成","value":3},{"label":"完全需要帮助","value":5}]'),
('SELF_CARE_ASSESSMENT', 'q4', '如厕', 'NUMBER', 4, '[{"label":"不需协助","value":0},{"label":"偶尔失禁","value":1},{"label":"经常失禁","value":5},{"label":"完全失禁","value":10}]'),
('SELF_CARE_ASSESSMENT', 'q5', '活动', 'NUMBER', 5, '[{"label":"独立完成","value":0},{"label":"借助辅助","value":1},{"label":"借助较大外力","value":5},{"label":"卧床不起","value":10}]');
```

**体质评估场景元数据：**

```sql
-- 题目元数据示例（33道题）
-- 注：场景配置已在 7.1.1 节的步骤2中插入
INSERT INTO field_metadata (scene_code, field_code, field_name, field_type, sort_order) VALUES
('CONSTITUTION_ASSESSMENT', 'q1', '您精力充沛吗？', 'NUMBER', 1),
('CONSTITUTION_ASSESSMENT', 'q2', '您容易疲乏吗？', 'NUMBER', 2),
-- ... 省略其他题目
('CONSTITUTION_ASSESSMENT', 'q33', '您舌下静脉瘀紫或增粗吗？', 'NUMBER', 33);
```

#### 7.1.3 分组映射配置（类型二专用）

> 用于配置题目与体质类型的映射关系，实现计算逻辑的配置化

**新增分组映射表：**

```sql
CREATE TABLE assessment_group_mapping (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    scene_code VARCHAR(50) NOT NULL COMMENT '场景编码',
    group_code VARCHAR(50) NOT NULL COMMENT '分组编码，如 qixu, yangxu',
    group_name VARCHAR(100) NOT NULL COMMENT '分组名称，如 气虚质',
    item_list VARCHAR(500) NOT NULL COMMENT '题目列表，JSON数组格式，如 [2,3,4,14]',
    reverse_items VARCHAR(500) DEFAULT NULL COMMENT '需要反向计分的题目，JSON数组格式',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

**体质评估分组映射配置：**

```sql
INSERT INTO assessment_group_mapping (scene_code, group_code, group_name, item_list, reverse_items, sort_order) VALUES
('CONSTITUTION_ASSESSMENT', 'qixu', '气虚质', '[2,3,4,14]', NULL, 1),
('CONSTITUTION_ASSESSMENT', 'yangxu', '阳虚质', '[11,12,13,29]', NULL, 2),
('CONSTITUTION_ASSESSMENT', 'yinxu', '阴虚质', '[10,21,26,31]', NULL, 3),
('CONSTITUTION_ASSESSMENT', 'tanshi', '痰湿质', '[9,16,28,32]', NULL, 4),
('CONSTITUTION_ASSESSMENT', 'shire', '湿热质', '[23,25,27,30]', NULL, 5),
('CONSTITUTION_ASSESSMENT', 'xueyu', '血瘀质', '[19,22,24,33]', NULL, 6),
('CONSTITUTION_ASSESSMENT', 'qiyu', '气郁质', '[5,6,7,8]', NULL, 7),
('CONSTITUTION_ASSESSMENT', 'tebing', '特禀质', '[15,17,18,20]', NULL, 8),
('CONSTITUTION_ASSESSMENT', 'pinghe', '平和质', '[1,2,4,5,13]', '[2,4,5,13]', 9);
```

### 7.2 体质类型中文映射

| 编码 | 中文名称 | 英文缩写 |
|-----|---------|---------|
| qixu | 气虚质 | QI_DEFICIENCY |
| yangxu | 阳虚质 | YANG_DEFICIENCY |
| yinxu | 阴虚质 | YIN_DEFICIENCY |
| tanshi | 痰湿质 | PHLEGM_DAMPNESS |
| shire | 湿热质 | DAMPNESS_HEAT |
| xueyu | 血瘀质 | BLOOD_STASIS |
| qiyu | 气郁质 | QI_STAGNATION |
| tebing | 特禀质 | SPECIAL |
| pinghe | 平和质 | BALANCED |
