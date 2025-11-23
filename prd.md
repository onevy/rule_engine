
## 通用规则引擎技术方案说明书 V2.0

### 一、整体架构设计

#### 1.1 架构理念
- **核心引擎与业务分离**：规则引擎作为独立组件，不依赖具体业务
- **插件化业务接入**：各业务通过插件方式接入规则引擎
- **统一配置界面**：所有业务场景共享规则配置界面
- **数据模型抽象**：使用通用数据结构，业务层自行转换

### 1.2 项目说明

在本项目中，前端使用 Vue3 +“查询构建器”(Query Builder)组件，允许业务人员通过可视化界面动态配置规则,并解析成Drools的语法后存储到数据库中。后端使用 Drools 框架执行这些规则，并记录日志。


#### 1.2 系统架构图

```
┌─────────────────────────────────────────────────────────────┐
│                    业务应用层 (Business Layer)                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  传染病报卡   │  │   处方审核    │  │   病案检查    │  ... │
│  │   Adapter    │  │   Adapter    │  │   Adapter    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                                ↓
┌─────────────────────────────────────────────────────────────┐
│              通用规则引擎层 (Rule Engine Core)                 │
│  ┌────────────────────────────────────────────────────┐     │
│  │                  Rule Engine API                    │     │
│  ├────────────────────────────────────────────────────┤     │
│  │   规则管理  │  规则执行  │  规则编译  │  规则验证    │     │
│  ├────────────────────────────────────────────────────┤     │
│  │              Drools Engine             │     │
│  └────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
                                ↓
┌─────────────────────────────────────────────────────────────┐
│                   数据持久层 (Data Layer)                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ 规则元数据DB  │  │   规则内容DB  │  │   执行日志DB  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
```

### 二、数据库设计（通用化）

```sql
-- 1. 业务场景定义表
CREATE TABLE rule_scene (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    scene_code VARCHAR(50) UNIQUE NOT NULL COMMENT '场景编码',
    scene_name VARCHAR(100) NOT NULL COMMENT '场景名称',
    scene_desc VARCHAR(500) COMMENT '场景描述',
    input_schema TEXT COMMENT '输入数据模型定义(JSON Schema)',
    output_schema TEXT COMMENT '输出数据模型定义(JSON Schema)',
    adapter_class VARCHAR(200) COMMENT '适配器类名',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用',
    created_by VARCHAR(50),
    created_time DATETIME,
    updated_by VARCHAR(50),
    updated_time DATETIME
) COMMENT='业务场景定义表';

-- 2. 规则组表（支持规则分组管理）
CREATE TABLE rule_group (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_code VARCHAR(50) UNIQUE NOT NULL COMMENT '规则组编码',
    group_name VARCHAR(100) NOT NULL COMMENT '规则组名称',
    scene_id BIGINT NOT NULL COMMENT '所属场景ID',
    parent_id BIGINT COMMENT '父级规则组ID',
    group_desc VARCHAR(500) COMMENT '规则组描述',
    execution_mode VARCHAR(20) DEFAULT 'ALL' COMMENT '执行模式:ALL-全部执行,FIRST-首个匹配',
    priority INT DEFAULT 0 COMMENT '优先级',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用',
    created_time DATETIME,
    updated_time DATETIME,
    INDEX idx_scene_id (scene_id)
) COMMENT='规则组表';

-- 3. 规则定义表（通用化）
CREATE TABLE rule_definition (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_code VARCHAR(100) UNIQUE NOT NULL COMMENT '规则编码',
    rule_name VARCHAR(200) NOT NULL COMMENT '规则名称',
    rule_desc VARCHAR(500) COMMENT '规则描述',
    group_id BIGINT NOT NULL COMMENT '所属规则组ID',
    rule_type VARCHAR(50) DEFAULT 'CONDITION' COMMENT '规则类型:CONDITION-条件规则,DECISION_TABLE-决策表,SCRIPT-脚本规则',
    priority INT DEFAULT 0 COMMENT '执行优先级',
    version INT DEFAULT 1 COMMENT '版本号',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用',
    effective_time DATETIME COMMENT '生效时间',
    expiry_time DATETIME COMMENT '失效时间',
    created_by VARCHAR(50),
    created_time DATETIME,
    updated_by VARCHAR(50),
    updated_time DATETIME,
    INDEX idx_group_id (group_id),
    INDEX idx_rule_type (rule_type)
) COMMENT='规则定义表';

-- 4. 规则内容表（存储实际规则）
CREATE TABLE rule_content (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_id BIGINT NOT NULL COMMENT '规则ID',
    content_type VARCHAR(20) DEFAULT 'JSON' COMMENT '内容类型:JSON,DRL,GROOVY,JAVASCRIPT',
    rule_content LONGTEXT COMMENT '规则内容',
    compiled_content LONGTEXT COMMENT '编译后的内容',
    checksum VARCHAR(64) COMMENT '内容校验和',
    created_time DATETIME,
    UNIQUE KEY uk_rule_id (rule_id)
) COMMENT='规则内容表';

-- 5. 规则条件表（通用条件配置）
CREATE TABLE rule_condition (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_id BIGINT NOT NULL COMMENT '规则ID',
    condition_group INT DEFAULT 0 COMMENT '条件组（同组AND，不同组OR）',
    left_type VARCHAR(20) DEFAULT 'FIELD' COMMENT '左值类型:FIELD-字段,CONSTANT-常量,EXPRESSION-表达式',
    left_value VARCHAR(500) COMMENT '左值',
    operator VARCHAR(20) COMMENT '操作符:EQ,NEQ,GT,GTE,LT,LTE,IN,NOT_IN,LIKE,BETWEEN,IS_NULL,IS_NOT_NULL',
    right_type VARCHAR(20) DEFAULT 'CONSTANT' COMMENT '右值类型',
    right_value TEXT COMMENT '右值',
    value_type VARCHAR(20) DEFAULT 'STRING' COMMENT '值类型:STRING,NUMBER,BOOLEAN,DATE,ARRAY,OBJECT',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_time DATETIME,
    INDEX idx_rule_id (rule_id),
    INDEX idx_group (condition_group)
) COMMENT='规则条件表';

-- 6. 规则动作表
CREATE TABLE rule_action (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_id BIGINT NOT NULL COMMENT '规则ID',
    action_type VARCHAR(50) COMMENT '动作类型:RETURN-返回结果,CALL_SERVICE-调用服务,SEND_MESSAGE-发送消息',
    action_code VARCHAR(100) COMMENT '动作编码',
    action_params TEXT COMMENT '动作参数(JSON)',
    sort_order INT DEFAULT 0 COMMENT '执行顺序',
    created_time DATETIME,
    INDEX idx_rule_id (rule_id)
) COMMENT='规则动作表';

-- 7. 规则元数据表（定义可配置的字段）
CREATE TABLE rule_metadata (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    scene_id BIGINT NOT NULL COMMENT '场景ID',
    field_code VARCHAR(100) NOT NULL COMMENT '字段编码',
    field_name VARCHAR(100) COMMENT '字段名称',
    field_type VARCHAR(50) COMMENT '字段类型',
    field_source VARCHAR(50) COMMENT '字段来源:INPUT-输入,COMPUTE-计算,CONSTANT-常量',
    value_range TEXT COMMENT '值域定义(JSON)',
    compute_expression TEXT COMMENT '计算表达式',
    description VARCHAR(500) COMMENT '字段说明',
    sort_order INT DEFAULT 0,
    created_time DATETIME,
    INDEX idx_scene_id (scene_id),
    UNIQUE KEY uk_scene_field (scene_id, field_code)
) COMMENT='规则元数据表';

-- 8. 规则执行记录表
CREATE TABLE rule_execution_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    trace_id VARCHAR(64) COMMENT '追踪ID',
    scene_code VARCHAR(50) COMMENT '场景编码',
    rule_group_id BIGINT COMMENT '规则组ID',
    rule_id BIGINT COMMENT '规则ID',
    input_data LONGTEXT COMMENT '输入数据',
    output_data LONGTEXT COMMENT '输出数据',
    matched_rules TEXT COMMENT '匹配的规则列表',
    execution_result VARCHAR(20) COMMENT '执行结果:SUCCESS,FAIL,ERROR',
    error_message TEXT COMMENT '错误信息',
    execution_time BIGINT COMMENT '执行耗时(ms)',
    created_time DATETIME,
    INDEX idx_trace_id (trace_id),
    INDEX idx_scene_code (scene_code),
    INDEX idx_created_time (created_time)
) COMMENT='规则执行记录表';

-- 9. 规则版本表
CREATE TABLE rule_version (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_id BIGINT NOT NULL COMMENT '规则ID',
    version_no INT NOT NULL COMMENT '版本号',
    rule_content LONGTEXT COMMENT '规则内容快照',
    change_log VARCHAR(500) COMMENT '变更说明',
    is_current TINYINT DEFAULT 0 COMMENT '是否当前版本',
    created_by VARCHAR(50),
    created_time DATETIME,
    INDEX idx_rule_id (rule_id),
    UNIQUE KEY uk_rule_version (rule_id, version_no)
) COMMENT='规则版本表';
```

### 三、核心代码实现（业务解耦版）

#### 3.1 规则引擎核心接口定义

```java
// ============ 核心接口定义 ============

/**
 * 规则引擎核心接口
 */
public interface RuleEngine {
    /**
     * 执行规则
     * @param context 执行上下文
     * @return 执行结果
     */
    RuleResult execute(RuleContext context);
    
    /**
     * 批量执行规则
     * @param contexts 执行上下文列表
     * @return 执行结果列表
     */
    List<RuleResult> batchExecute(List<RuleContext> contexts);
    
    /**
     * 重新加载规则
     * @param sceneCode 场景编码
     */
    void reload(String sceneCode);
    
    /**
     * 验证规则
     * @param ruleContent 规则内容
     * @return 验证结果
     */
    ValidationResult validate(String ruleContent);
}

/**
 * 规则上下文（通用）
 */
@Data
public class RuleContext {
    /**
     * 追踪ID（用于日志追踪）
     */
    private String traceId;
    
    /**
     * 场景编码
     */
    private String sceneCode;
    
    /**
     * 规则组编码（可选，不指定则执行场景下所有规则）
     */
    private String groupCode;
    
    /**
     * 输入数据（通用Map结构）
     */
    private Map<String, Object> inputData;
    
    /**
     * 扩展参数
     */
    private Map<String, Object> extParams;
    
    /**
     * 元数据
     */
    private Map<String, Object> metadata;
}

/**
 * 规则执行结果
 */
@Data
public class RuleResult {
    /**
     * 是否匹配规则
     */
    private boolean matched;
    
    /**
     * 匹配的规则列表
     */
    private List<MatchedRule> matchedRules;
    
    /**
     * 执行的动作结果
     */
    private List<ActionResult> actionResults;
    
    /**
     * 输出数据
     */
    private Map<String, Object> outputData;
    
    /**
     * 执行耗时
     */
    private long executionTime;
    
    /**
     * 错误信息（如果有）
     */
    private String errorMessage;
}

/**
 * 业务适配器接口
 */
public interface BusinessAdapter {
    /**
     * 获取场景编码
     */
    String getSceneCode();
    
    /**
     * 预处理输入数据
     * @param originalData 原始业务数据
     * @return 转换后的规则引擎输入数据
     */
    Map<String, Object> preProcess(Object originalData);
    
    /**
     * 后处理输出数据
     * @param ruleResult 规则引擎执行结果
     * @return 业务响应对象
     */
    Object postProcess(RuleResult ruleResult);
    
    /**
     * 自定义动作处理器
     * @return 动作处理器映射
     */
    default Map<String, ActionHandler> getActionHandlers() {
        return new HashMap<>();
    }
}

/**
 * 动作处理器接口
 */
public interface ActionHandler {
    /**
     * 执行动作
     * @param context 上下文
     * @param params 参数
     * @return 执行结果
     */
    Object execute(RuleContext context, Map<String, Object> params);
}
```

#### 3.2 规则引擎核心实现

```java
@Component
@Slf4j
public class DroolsRuleEngine implements RuleEngine {
    
    @Autowired
    private RuleDefinitionService ruleDefinitionService;
    
    @Autowired
    private RuleCompiler ruleCompiler;
    
    @Autowired
    private RuleExecutionLogger executionLogger;
    
    // 规则缓存（按场景）
    private final Map<String, KieBase> kieBaseCache = new ConcurrentHashMap<>();
    
    @Override
    public RuleResult execute(RuleContext context) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        try {
            // 1. 获取场景对应的KieBase
            KieBase kieBase = getOrLoadKieBase(context.getSceneCode());
            
            // 2. 创建会话
            KieSession session = kieBase.newKieSession();
            
            try {
                // 3. 设置全局变量
                session.setGlobal("ruleResult", new RuleResult());
                session.setGlobal("context", context);
                
                // 4. 插入事实
                session.insert(context.getInputData());
                
                // 5. 执行规则
                int firedRules = session.fireAllRules();
                
                // 6. 获取结果
                RuleResult result = (RuleResult) session.getGlobal("ruleResult");
                
                stopWatch.stop();
                result.setExecutionTime(stopWatch.getTotalTimeMillis());
                
                // 7. 记录执行日志
                executionLogger.log(context, result);
                
                log.info("规则执行完成，场景：{}，触发规则数：{}，耗时：{}ms",
                    context.getSceneCode(), firedRules, result.getExecutionTime());
                
                return result;
            } finally {
                session.dispose();
            }
        } catch (Exception e) {
            log.error("规则执行失败", e);
            RuleResult errorResult = new RuleResult();
            errorResult.setMatched(false);
            errorResult.setErrorMessage(e.getMessage());
            stopWatch.stop();
            errorResult.setExecutionTime(stopWatch.getTotalTimeMillis());
            return errorResult;
        }
    }
    
    @Override
    public List<RuleResult> batchExecute(List<RuleContext> contexts) {
        return contexts.parallelStream()
            .map(this::execute)
            .collect(Collectors.toList());
    }
    
    @Override
    public void reload(String sceneCode) {
        log.info("重新加载场景规则：{}", sceneCode);
        kieBaseCache.remove(sceneCode);
        getOrLoadKieBase(sceneCode);
    }
    
    @Override
    public ValidationResult validate(String ruleContent) {
        return ruleCompiler.validate(ruleContent);
    }
    
    private KieBase getOrLoadKieBase(String sceneCode) {
        return kieBaseCache.computeIfAbsent(sceneCode, this::loadKieBase);
    }
    
    private KieBase loadKieBase(String sceneCode) {
        log.info("加载场景规则：{}", sceneCode);
        
        // 1. 查询场景下的所有规则
        List<RuleDefinition> rules = ruleDefinitionService.findBySceneCode(sceneCode);
        
        // 2. 编译规则
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        
        for (RuleDefinition rule : rules) {
            String drl = ruleCompiler.compile(rule);
            String path = "src/main/resources/rules/" + sceneCode + "/" + 
                         rule.getRuleCode() + ".drl";
            kieFileSystem.write(path, drl);
        }
        
        // 3. 构建KieBase
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();
        
        if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
            throw new RuleCompileException("规则编译失败：" + 
                kieBuilder.getResults().getMessages());
        }
        
        KieContainer kieContainer = kieServices.newKieContainer(
            kieServices.getRepository().getDefaultReleaseId());
        
        return kieContainer.getKieBase();
    }
}

/**
 * 规则编译器
 */
@Component
@Slf4j
public class RuleCompiler {
    
    @Autowired
    private RuleConditionService conditionService;
    
    @Autowired
    private RuleActionService actionService;
    
    /**
     * 编译规则
     */
    public String compile(RuleDefinition rule) {
        switch (rule.getRuleType()) {
            case CONDITION:
                return compileConditionRule(rule);
            case DECISION_TABLE:
                return compileDecisionTable(rule);
            case SCRIPT:
                return compileScriptRule(rule);
            default:
                throw new UnsupportedOperationException(
                    "不支持的规则类型：" + rule.getRuleType());
        }
    }
    
    /**
     * 编译条件规则
     */
    private String compileConditionRule(RuleDefinition rule) {
        StringBuilder drl = new StringBuilder();
        
        // 包声明
        drl.append("package rules.").append(rule.getGroupId()).append(";\n\n");
        
        // 导入
        drl.append("import java.util.Map;\n");
        drl.append("import java.util.List;\n");
        drl.append("import com.ruleengine.core.RuleContext;\n");
        drl.append("import com.ruleengine.core.RuleResult;\n");
        drl.append("import com.ruleengine.core.MatchedRule;\n\n");
        
        // 全局变量
        drl.append("global RuleResult ruleResult;\n");
        drl.append("global RuleContext context;\n\n");
        
        // 规则
        drl.append("rule \"").append(rule.getRuleCode()).append("\"\n");
        drl.append("    salience ").append(rule.getPriority()).append("\n");
        
        // 规则属性
        if (rule.getEffectiveTime() != null) {
            drl.append("    date-effective \"")
               .append(rule.getEffectiveTime()).append("\"\n");
        }
        if (rule.getExpiryTime() != null) {
            drl.append("    date-expires \"")
               .append(rule.getExpiryTime()).append("\"\n");
        }
        
        // when部分
        drl.append("when\n");
        drl.append(buildConditions(rule.getId()));
        
        // then部分
        drl.append("then\n");
        drl.append(buildActions(rule.getId()));
        drl.append("end\n");
        
        return drl.toString();
    }
    
    /**
     * 构建条件
     */
    private String buildConditions(Long ruleId) {
        List<RuleCondition> conditions = conditionService.findByRuleId(ruleId);
        
        if (conditions.isEmpty()) {
            return "    $data : Map()\n";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("    $data : Map(\n");
        
        // 按组分组
        Map<Integer, List<RuleCondition>> groups = conditions.stream()
            .collect(Collectors.groupingBy(RuleCondition::getConditionGroup));
        
        List<String> groupConditions = new ArrayList<>();
        for (List<RuleCondition> group : groups.values()) {
            List<String> andConditions = group.stream()
                .map(this::buildSingleCondition)
                .collect(Collectors.toList());
            
            if (andConditions.size() == 1) {
                groupConditions.add(andConditions.get(0));
            } else {
                groupConditions.add("(" + String.join(" && ", andConditions) + ")");
            }
        }
        
        sb.append("        ").append(String.join(" || ", groupConditions));
        sb.append("\n    )\n");
        
        return sb.toString();
    }
    
    /**
     * 构建单个条件
     */
    private String buildSingleCondition(RuleCondition condition) {
        String field = "$data.get(\"" + condition.getLeftValue() + "\")";
        String operator = condition.getOperator();
        String value = condition.getRightValue();
        
        switch (operator.toUpperCase()) {
            case "EQ":
                return field + " == " + formatValue(value, condition.getValueType());
            case "NEQ":
                return field + " != " + formatValue(value, condition.getValueType());
            case "GT":
                return field + " > " + formatValue(value, condition.getValueType());
            case "GTE":
                return field + " >= " + formatValue(value, condition.getValueType());
            case "LT":
                return field + " < " + formatValue(value, condition.getValueType());
            case "LTE":
                return field + " <= " + formatValue(value, condition.getValueType());
            case "IN":
                return field + " memberOf " + formatArray(value);
            case "NOT_IN":
                return field + " not memberOf " + formatArray(value);
            case "LIKE":
                return field + " matches \"" + value + "\"";
            case "BETWEEN":
                String[] range = value.split(",");
                return "(" + field + " >= " + range[0] + " && " + 
                       field + " <= " + range[1] + ")";
            case "IS_NULL":
                return field + " == null";
            case "IS_NOT_NULL":
                return field + " != null";
            default:
                throw new IllegalArgumentException("不支持的操作符：" + operator);
        }
    }
    
    /**
     * 构建动作
     */
    private String buildActions(Long ruleId) {
        List<RuleAction> actions = actionService.findByRuleId(ruleId);
        
        StringBuilder sb = new StringBuilder();
        
        // 添加匹配记录
        sb.append("    MatchedRule matched = new MatchedRule();\n");
        sb.append("    matched.setRuleId(").append(ruleId).append("L);\n");
        sb.append("    matched.setRuleCode(\"").append(ruleId).append("\");\n");
        sb.append("    ruleResult.getMatchedRules().add(matched);\n");
        sb.append("    ruleResult.setMatched(true);\n\n");
        
        // 执行动作
        for (RuleAction action : actions) {
            sb.append(buildSingleAction(action));
        }
        
        return sb.toString();
    }
    
    /**
     * 构建单个动作
     */
    private String buildSingleAction(RuleAction action) {
        switch (action.getActionType()) {
            case "RETURN":
                return "    ruleResult.getOutputData().putAll(" + 
                       action.getActionParams() + ");\n";
            case "MODIFY":
                return "    $data.putAll(" + action.getActionParams() + ");\n" +
                       "    update($data);\n";
            case "LOG":
                return "    System.out.println(\"" + 
                       action.getActionParams() + "\");\n";
            default:
                return "    // Unsupported action: " + 
                       action.getActionType() + "\n";
        }
    }
    
    private String formatValue(String value, String type) {
        switch (type.toUpperCase()) {
            case "STRING":
                return "\"" + value + "\"";
            case "NUMBER":
                return value;
            case "BOOLEAN":
                return value.toLowerCase();
            default:
                return value;
        }
    }
    
    private String formatArray(String value) {
        String[] items = value.split(",");
        return "[" + Arrays.stream(items)
            .map(item -> "\"" + item.trim() + "\"")
            .collect(Collectors.joining(", ")) + "]";
    }
}
```

#### 3.3 业务适配器实现示例

```java
// ============ 传染病报卡适配器 ============
@Component
public class InfectiousDiseaseAdapter implements BusinessAdapter {
    
    @Override
    public String getSceneCode() {
        return "INFECTIOUS_DISEASE_REPORT";
    }
    
    @Override
    public Map<String, Object> preProcess(Object originalData) {
        InfectiousDiseaseRequest request = (InfectiousDiseaseRequest) originalData;
        
        Map<String, Object> inputData = new HashMap<>();
        
        // 诊断数据
        inputData.put("diagnosis_code", request.getDiagnosisCode());
        inputData.put("diagnosis_name", request.getDiagnosisName());
        inputData.put("diagnosis_type", request.getDiagnosisType());
        
        // 检验数据
        inputData.put("lab_items", request.getLabItems());
        inputData.put("lab_results", request.getLabResults());
        
        // 检查数据
        inputData.put("exam_items", request.getExamItems());
        inputData.put("exam_results", request.getExamResults());
        
        // 患者数据
        inputData.put("patient_age", request.getPatientAge());
        inputData.put("patient_gender", request.getPatientGender());
        
        return inputData;
    }
    
    @Override
    public Object postProcess(RuleResult ruleResult) {
        InfectiousDiseaseResponse response = new InfectiousDiseaseResponse();
        
        if (ruleResult.isMatched()) {
            List<String> diseases = (List<String>) 
                ruleResult.getOutputData().get("report_diseases");
            response.setNeedReport(true);
            response.setReportDiseases(diseases);
            response.setReportCards(generateReportCards(diseases));
        } else {
            response.setNeedReport(false);
        }
        
        return response;
    }
    
    @Override
    public Map<String, ActionHandler> getActionHandlers() {
        Map<String, ActionHandler> handlers = new HashMap<>();
        
        // 发送报卡通知
        handlers.put("SEND_REPORT_NOTIFICATION", (context, params) -> {
            String diseaseCode = (String) params.get("disease_code");
            // 调用消息服务发送通知
            return "通知已发送";
        });
        
        // 生成报卡
        handlers.put("GENERATE_REPORT_CARD", (context, params) -> {
            // 生成报卡逻辑
            return "报卡已生成";
        });
        
        return handlers;
    }
    
    private List<ReportCard> generateReportCards(List<String> diseases) {
        // 生成报卡的具体逻辑
        return new ArrayList<>();
    }
}

// ============ 处方审核适配器 ============
@Component
public class PrescriptionAuditAdapter implements BusinessAdapter {
    
    @Override
    public String getSceneCode() {
        return "PRESCRIPTION_AUDIT";
    }
    
    @Override
    public Map<String, Object> preProcess(Object originalData) {
        PrescriptionRequest request = (PrescriptionRequest) originalData;
        
        Map<String, Object> inputData = new HashMap<>();
        
        // 药品信息
        inputData.put("drug_codes", extractDrugCodes(request.getDrugs()));
        inputData.put("drug_doses", extractDrugDoses(request.getDrugs()));
        inputData.put("drug_interactions", checkInteractions(request.getDrugs()));
        
        // 诊断信息
        inputData.put("diagnosis_codes", request.getDiagnosisCodes());
        
        // 患者信息
        inputData.put("patient_age", request.getPatientAge());
        inputData.put("patient_weight", request.getPatientWeight());
        inputData.put("patient_allergies", request.getAllergies());
        inputData.put("patient_pregnancy", request.isPregnant());
        inputData.put("patient_liver_function", request.getLiverFunction());
        inputData.put("patient_kidney_function", request.getKidneyFunction());
        
        return inputData;
    }
    
    @Override
    public Object postProcess(RuleResult ruleResult) {
        PrescriptionAuditResponse response = new PrescriptionAuditResponse();
        
        if (ruleResult.isMatched()) {
            response.setAuditPassed(false);
            response.setAuditProblems(extractProblems(ruleResult));
            response.setSuggestions(extractSuggestions(ruleResult));
        } else {
            response.setAuditPassed(true);
        }
        
        return response;
    }
    
    @Override
    public Map<String, ActionHandler> getActionHandlers() {
        Map<String, ActionHandler> handlers = new HashMap<>();
        
        // 药品相互作用检查
        handlers.put("CHECK_DRUG_INTERACTION", (context, params) -> {
            // 调用药品知识库检查相互作用
            return checkDrugInteraction(params);
        });
        
        // 剂量调整建议
        handlers.put("SUGGEST_DOSE_ADJUSTMENT", (context, params) -> {
            // 根据患者情况计算推荐剂量
            return calculateRecommendedDose(params);
        });
        
        return handlers;
    }
    
    private List<String> extractDrugCodes(List<Drug> drugs) {
        return drugs.stream()
            .map(Drug::getCode)
            .collect(Collectors.toList());
    }
    
    private Map<String, Double> extractDrugDoses(List<Drug> drugs) {
        return drugs.stream()
            .collect(Collectors.toMap(Drug::getCode, Drug::getDose));
    }
    
    private List<String> checkInteractions(List<Drug> drugs) {
        // 检查药品相互作用
        return new ArrayList<>();
    }
    
    private List<AuditProblem> extractProblems(RuleResult result) {
        // 提取审核问题
        return new ArrayList<>();
    }
    
    private List<String> extractSuggestions(RuleResult result) {
        // 提取建议
        return new ArrayList<>();
    }
    
    private Object checkDrugInteraction(Map<String, Object> params) {
        // 药品相互作用检查逻辑
        return null;
    }
    
    private Object calculateRecommendedDose(Map<String, Object> params) {
        // 剂量计算逻辑
        return null;
    }
}

// ============ 病案质控适配器 ============
@Component
public class MedicalRecordQCAdapter implements BusinessAdapter {
    
    @Override
    public String getSceneCode() {
        return "MEDICAL_RECORD_QC";
    }
    
    @Override
    public Map<String, Object> preProcess(Object originalData) {
        MedicalRecordRequest request = (MedicalRecordRequest) originalData;
        
        Map<String, Object> inputData = new HashMap<>();
        
        // 病案基本信息
        inputData.put("admission_date", request.getAdmissionDate());
        inputData.put("discharge_date", request.getDischargeDate());
        inputData.put("length_of_stay", calculateLOS(request));
        
        // 诊断信息
        inputData.put("main_diagnosis", request.getMainDiagnosis());
        inputData.put("other_diagnoses", request.getOtherDiagnoses());
        inputData.put("operation_codes", request.getOperationCodes());
        
        // 费用信息
        inputData.put("total_cost", request.getTotalCost());
        inputData.put("drug_cost_ratio", calculateDrugCostRatio(request));
        
        // 质控指标
        inputData.put("has_informed_consent", request.hasInformedConsent());
        inputData.put("has_operation_record", request.hasOperationRecord());
        inputData.put("has_discharge_summary", request.hasDischargeSummary());
        
        return inputData;
    }
    
    @Override
    public Object postProcess(RuleResult ruleResult) {
        MedicalRecordQCResponse response = new MedicalRecordQCResponse();
        
        response.setQualified(!ruleResult.isMatched());
        
        if (ruleResult.isMatched()) {
            response.setQualityProblems(extractQualityProblems(ruleResult));
            response.setQualityScore(calculateQualityScore(ruleResult));
            response.setImprovementSuggestions(
                extractImprovementSuggestions(ruleResult));
        } else {
            response.setQualityScore(100.0);
        }
        
        return response;
    }
    
    private int calculateLOS(MedicalRecordRequest request) {
        // 计算住院天数
        return 0;
    }
    
    private double calculateDrugCostRatio(MedicalRecordRequest request) {
        // 计算药占比
        return 0.0;
    }
    
    private List<QualityProblem> extractQualityProblems(RuleResult result) {
        // 提取质量问题
        return new ArrayList<>();
    }
    
    private double calculateQualityScore(RuleResult result) {
        // 计算质量分数
        return 85.0;
    }
    
    private List<String> extractImprovementSuggestions(RuleResult result) {
        // 提取改进建议
        return new ArrayList<>();
    }
}
```

#### 3.4 统一的Controller层

```java
@RestController
@RequestMapping("/api/rule-engine")
@Slf4j
public class RuleEngineController {
    
    @Autowired
    private RuleEngine ruleEngine;
    
    @Autowired
    private Map<String, BusinessAdapter> adapterMap;
    
    @Autowired
    private RuleConfigService ruleConfigService;
    
    /**
     * 通用规则执行接口
     */
    @PostMapping("/execute")
    public Result<Object> execute(@RequestBody ExecuteRequest request) {
        try {
            // 1. 获取业务适配器
            BusinessAdapter adapter = adapterMap.get(request.getSceneCode());
            if (adapter == null) {
                return Result.error("未找到场景适配器：" + request.getSceneCode());
            }
            
            // 2. 预处理数据
            Map<String, Object> inputData = adapter.preProcess(request.getBusinessData());
            
            // 3. 构建规则上下文
            RuleContext context = new RuleContext();
            context.setTraceId(UUID.randomUUID().toString());
            context.setSceneCode(request.getSceneCode());
            context.setInputData(inputData);
            context.setExtParams(request.getExtParams());
            
            // 4. 执行规则
            RuleResult ruleResult = ruleEngine.execute(context);
            
            // 5. 后处理数据
            Object businessResult = adapter.postProcess(ruleResult);
            
            return Result.success(businessResult);
        } catch (Exception e) {
            log.error("规则执行失败", e);
            return Result.error("规则执行失败：" + e.getMessage());
        }
    }
    
    /**
     * 批量执行
     */
    @PostMapping("/batch-execute")
    public Result<List<Object>> batchExecute(@RequestBody BatchExecuteRequest request) {
        // 批量执行逻辑
        return Result.success(new ArrayList<>());
    }
    
    /**
     * 规则测试
     */
    @PostMapping("/test")
    public Result<RuleTestResult> testRule(@RequestBody RuleTestRequest request) {
        // 测试逻辑
        return Result.success(new RuleTestResult());
    }
    
    /**
     * 刷新规则缓存
     */
    @PostMapping("/reload/{sceneCode}")
    public Result<Void> reloadRules(@PathVariable String sceneCode) {
        ruleEngine.reload(sceneCode);
        return Result.success();
    }
    
    // ===== 规则配置管理接口 =====
    
    /**
     * 创建规则
     */
    @PostMapping("/rule/create")
    public Result<Long> createRule(@RequestBody @Valid RuleDefinitionDTO dto) {
        Long ruleId = ruleConfigService.createRule(dto);
        return Result.success(ruleId);
    }
    
    /**
     * 更新规则
     */
    @PutMapping("/rule/update/{ruleId}")
    public Result<Void> updateRule(@PathVariable Long ruleId, 
                                  @RequestBody @Valid RuleDefinitionDTO dto) {
        ruleConfigService.updateRule(ruleId, dto);
        return Result.success();
    }
    
    /**
     * 删除规则
     */
    @DeleteMapping("/rule/delete/{ruleId}")
    public Result<Void> deleteRule(@PathVariable Long ruleId) {
        ruleConfigService.deleteRule(ruleId);
        return Result.success();
    }
    
    /**
     * 查询规则列表
     */
    @GetMapping("/rule/list")
    public Result<Page<RuleDefinitionVO>> listRules(RuleQueryDTO query) {
        Page<RuleDefinitionVO> page = ruleConfigService.queryRules(query);
        return Result.success(page);
    }
    
    /**
     * 获取场景元数据
     */
    @GetMapping("/metadata/{sceneCode}")
    public Result<SceneMetadata> getSceneMetadata(@PathVariable String sceneCode) {
        SceneMetadata metadata = ruleConfigService.getSceneMetadata(sceneCode);
        return Result.success(metadata);
    }
}
```

### 四、前端通用配置界面

```vue
<template>
  <div class="rule-engine-config">
    <!-- 场景选择 -->
    <el-card class="scene-selector">
      <el-form :model="configForm" ref="formRef">
        <el-form-item label="业务场景">
          <el-select 
            v-model="configForm.sceneCode" 
            @change="onSceneChange"
            placeholder="请选择业务场景">
            <el-option 
              v-for="scene in scenes" 
              :key="scene.sceneCode"
              :label="scene.sceneName"
              :value="scene.sceneCode">
              <span style="float: left">{{ scene.sceneName }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">
                {{ scene.sceneCode }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="规则组">
          <el-cascader
            v-model="configForm.groupId"
            :options="ruleGroups"
            :props="{ 
              value: 'id', 
              label: 'groupName', 
              children: 'children' 
            }"
            @change="onGroupChange"
            clearable
            placeholder="请选择规则组">
          </el-cascader>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 规则配置 -->
    <el-card class="rule-config" v-if="configForm.sceneCode">
      <template #header>
        <div class="card-header">
          <span>规则配置</span>
          <div>
            <el-button type="primary" size="small" @click="addRule">
              新增规则
            </el-button>
            <el-button size="small" @click="importRules">
              导入规则
            </el-button>
            <el-button size="small" @click="exportRules">
              导出规则
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 规则列表 -->
      <el-table :data="rules" style="width: 100%">
        <el-table-column type="expand">
          <template #default="props">
            <rule-detail-panel 
              :rule="props.row" 
              :metadata="sceneMetadata"
              @update="updateRule">
            </rule-detail-panel>
          </template>
        </el-table-column>
        
        <el-table-column prop="ruleCode" label="规则编码" width="150" />
        <el-table-column prop="ruleName" label="规则名称" />
        <el-table-column prop="ruleType" label="规则类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ getRuleTypeLabel(row.ruleType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80" />
        <el-table-column prop="isActive" label="状态" width="80">
          <template #default="{ row }">
            <el-switch 
              v-model="row.isActive"
              @change="toggleRule(row)">
            </el-switch>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="text" @click="editRule(row)">编辑</el-button>
            <el-button type="text" @click="testRule(row)">测试</el-button>
            <el-button type="text" @click="copyRule(row)">复制</el-button>
            <el-button type="text" @click="deleteRule(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange">
      </el-pagination>
    </el-card>
    
    <!-- 规则编辑对话框 -->
    <rule-edit-dialog
      v-model="editDialogVisible"
      :rule="currentRule"
      :scene-code="configForm.sceneCode"
      :metadata="sceneMetadata"
      @save="handleRuleSave">
    </rule-edit-dialog>
    
    <!-- 规则测试对话框 -->
    <rule-test-dialog
      v-model="testDialogVisible"
      :rule="currentRule"
      :metadata="sceneMetadata"
      @test="handleRuleTest">
    </rule-test-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import RuleDetailPanel from './components/RuleDetailPanel.vue'
import RuleEditDialog from './components/RuleEditDialog.vue'
import RuleTestDialog from './components/RuleTestDialog.vue'
import { 
  getScenes, 
  getRuleGroups, 
  getRules, 
  getSceneMetadata,
  createRule, 
  updateRule, 
  deleteRule,
  toggleRuleStatus,
  testRule as testRuleApi
} from '@/api/rule-engine'

// 表单数据
const configForm = reactive({
  sceneCode: '',
  groupId: null
})

// 场景列表
const scenes = ref([])
// 规则组列表
const ruleGroups = ref([])
// 规则列表
const rules = ref([])
// 场景元数据
const sceneMetadata = ref({})
// 当前编辑的规则
const currentRule = ref(null)
// 编辑对话框
const editDialogVisible = ref(false)
// 测试对话框
const testDialogVisible = ref(false)

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0
})

// 加载场景列表
const loadScenes = async () => {
  try {
    const res = await getScenes()
    scenes.value = res.data
  } catch (error) {
    ElMessage.error('加载场景列表失败')
  }
}

// 场景变更
const onSceneChange = async (sceneCode) => {
  if (!sceneCode) return
  
  // 加载规则组
  await loadRuleGroups(sceneCode)
  
  // 加载场景元数据
  await loadSceneMetadata(sceneCode)
  
  // 加载规则列表
  await loadRules()
}

// 加载规则组
const loadRuleGroups = async (sceneCode) => {
  try {
    const res = await getRuleGroups(sceneCode)
    ruleGroups.value = res.data
  } catch (error) {
    ElMessage.error('加载规则组失败')
  }
}

// 加载场景元数据
const loadSceneMetadata = async (sceneCode) => {
  try {
    const res = await getSceneMetadata(sceneCode)
    sceneMetadata.value = res.data
  } catch (error) {
    ElMessage.error('加载场景元数据失败')
  }
}

// 加载规则列表
const loadRules = async () => {
  try {
    const params = {
      sceneCode: configForm.sceneCode,
      groupId: configForm.groupId,
      page: pagination.current,
      size: pagination.pageSize
    }
    const res = await getRules(params)
    rules.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('加载规则列表失败')
  }
}

// 新增规则
const addRule = () => {
  currentRule.value = {
    sceneCode: configForm.sceneCode,
    groupId: configForm.groupId,
    ruleType: 'CONDITION',
    priority: 0,
    isActive: true,
    conditions: [],
    actions: []
  }
  editDialogVisible.value = true
}

// 编辑规则
const editRule = (rule) => {
  currentRule.value = { ...rule }
  editDialogVisible.value = true
}

// 测试规则
const testRule = (rule) => {
  currentRule.value = rule
  testDialogVisible.value = true
}

// 复制规则
const copyRule = async (rule) => {
  try {
    const newRule = { 
      ...rule, 
      ruleCode: rule.ruleCode + '_copy',
      ruleName: rule.ruleName + '_副本',
      id: null 
    }
    await createRule(newRule)
    ElMessage.success('规则复制成功')
    loadRules()
  } catch (error) {
    ElMessage.error('规则复制失败')
  }
}

// 删除规则
const deleteRule = async (rule) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除规则【${rule.ruleName}】吗？`,
      '提示',
      { type: 'warning' }
    )
    
    await deleteRuleApi(rule.id)
    ElMessage.success('删除成功')
    loadRules()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 切换规则状态
const toggleRule = async (rule) => {
  try {
    await toggleRuleStatus(rule.id, rule.isActive)
    ElMessage.success('状态更新成功')
  } catch (error) {
    ElMessage.error('状态更新失败')
    rule.isActive = !rule.isActive
  }
}

// 保存规则
const handleRuleSave = async (rule) => {
  try {
    if (rule.id) {
      await updateRule(rule.id, rule)
      ElMessage.success('规则更新成功')
    } else {
      await createRule(rule)
      ElMessage.success('规则创建成功')
    }
    editDialogVisible.value = false
    loadRules()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

// 规则测试
const handleRuleTest = async (testData) => {
  try {
    const res = await testRuleApi({
      ruleId: currentRule.value.id,
      inputData: testData
    })
    ElMessage.success('测试完成，请查看结果')
  } catch (error) {
    ElMessage.error('测试失败')
  }
}

// 获取规则类型标签
const getRuleTypeLabel = (type) => {
  const labels = {
    'CONDITION': '条件规则',
    'DECISION_TABLE': '决策表',
    'SCRIPT': '脚本规则'
  }
  return labels[type] || type
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadRules()
}

const handleCurrentChange = (page) => {
  pagination.current = page
  loadRules()
}

onMounted(() => {
  loadScenes()
})
</script>

<style scoped>
.rule-engine-config {
  padding: 20px;
}

.scene-selector {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
```

### 五、系统优势

1. **业务解耦**：核心引擎与具体业务完全分离，通过适配器模式接入
2. **高复用性**：同一套规则引擎可服务于多个业务场景
3. **灵活扩展**：新业务场景只需实现适配器即可接入
4. **统一管理**：所有业务规则在一个平台统一配置管理
5. **标准化**：统一的数据模型和接口规范
6. **易维护**：业务逻辑变更不影响规则引擎核心

这个方案将规则引擎做成了一个通用平台，各业务通过适配器接入，实现了真正的业务解耦。需要进一步优化或添加其他功能吗？