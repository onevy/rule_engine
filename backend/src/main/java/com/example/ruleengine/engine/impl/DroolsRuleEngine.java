package com.example.ruleengine.engine.impl;

import com.example.ruleengine.engine.ActionHandler;
import com.example.ruleengine.engine.ActionHandlerRegistry;
import com.example.ruleengine.engine.BusinessAdapter;
import com.example.ruleengine.engine.RuleCompiler;
import com.example.ruleengine.engine.RuleContext;
import com.example.ruleengine.engine.RuleEngine;
import com.example.ruleengine.engine.RuleResult;
import com.example.ruleengine.engine.SimpleActionHandler;
import com.example.ruleengine.entity.RuleAction;
import com.example.ruleengine.entity.RuleExecutionLog;
import com.example.ruleengine.service.BusinessSceneService;
import com.example.ruleengine.service.RuleActionService;
import com.example.ruleengine.service.RuleExecutionLogService;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Drools规则引擎实现
 */
@Slf4j
@Component
public class DroolsRuleEngine implements RuleEngine {

    @Autowired
    private RuleCompiler ruleCompiler;

    @Autowired
    private BusinessSceneService businessSceneService;

    @Autowired
    private RuleExecutionLogService ruleExecutionLogService;

    @Autowired
    private RuleActionService ruleActionService;

    @Autowired
    private ActionHandlerRegistry actionHandlerRegistry;

    /**
     * KieBase缓存，按场景编码缓存
     */
    private final Map<String, KieBase> kieBaseCache = new ConcurrentHashMap<>();

    /**
     * 业务适配器映射
     */
    private final Map<String, BusinessAdapter> adapterMap = new ConcurrentHashMap<>();

    @Autowired(required = false)
    public void setBusinessAdapters(List<BusinessAdapter> adapters) {
        if (adapters != null) {
            for (BusinessAdapter adapter : adapters) {
                adapterMap.put(adapter.getSceneCode(), adapter);
                log.info("注册业务适配器: {}", adapter.getSceneCode());
            }
        }
    }

    @PostConstruct
    public void init() {
        log.info("Drools规则引擎初始化完成");
    }

    @Override
    public RuleResult execute(RuleContext context) {
        long startTime = System.currentTimeMillis();
        RuleResult result = new RuleResult();
        String traceId = StringUtils.hasText(context.getTraceId())
                ? context.getTraceId()
                : UUID.randomUUID().toString();

        try {
            String sceneCode = context.getSceneCode();
            if (!StringUtils.hasText(sceneCode)) {
                throw new IllegalArgumentException("场景编码不能为空");
            }

            // 获取业务适配器并预处理数据
            BusinessAdapter adapter = adapterMap.get(sceneCode);
            if (adapter != null) {
                Map<String, Object> processedData = adapter.preProcess(context.getInputData());
                context.setInputData(processedData);
            }

            // 获取或创建KieBase
            KieBase kieBase = getOrCreateKieBase(sceneCode);
            if (kieBase == null) {
                throw new RuntimeException("无法加载场景规则: " + sceneCode);
            }

            // 创建KieSession并执行规则
            KieSession kieSession = kieBase.newKieSession();
            try {
                // 设置全局变量
                kieSession.setGlobal("ruleResult", result);

                // 插入上下文数据
                kieSession.insert(context);

                // 执行规则
                int firedRules = kieSession.fireAllRules();
                log.debug("场景 {} 触发了 {} 条规则", sceneCode, firedRules);

            } finally {
                kieSession.dispose();
            }

            // 执行动作处理器
            executeActionHandlers(result, context);

            // 业务适配器后处理
            if (adapter != null) {
                adapter.postProcess(result);
            }

        } catch (Exception e) {
            log.error("规则执行异常: sceneCode={}, traceId={}", context.getSceneCode(), traceId, e);
            result.setErrorMessage(e.getMessage());
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            result.setExecutionTime(executionTime);

            // 记录执行日志
            logExecution(context, result, traceId, executionTime);
        }

        return result;
    }

    @Override
    public List<RuleResult> batchExecute(List<RuleContext> contexts) {
        return contexts.parallelStream()
                .map(this::execute)
                .collect(Collectors.toList());
    }

    @Override
    public void reload(String sceneCode) {
        log.info("重新加载场景规则: {}", sceneCode);
        kieBaseCache.remove(sceneCode);
        getOrCreateKieBase(sceneCode);
    }

    @Override
    public void reloadAll() {
        log.info("重新加载所有规则");
        Set<String> sceneCodes = new HashSet<>(kieBaseCache.keySet());
        kieBaseCache.clear();

        for (String sceneCode : sceneCodes) {
            getOrCreateKieBase(sceneCode);
        }
    }

    @Override
    public String validate(String ruleContent) {
        try {
            KieServices kieServices = KieServices.Factory.get();
            KieFileSystem kfs = kieServices.newKieFileSystem();
            kfs.write("src/main/resources/rules/validation.drl", ruleContent);

            KieBuilder kieBuilder = kieServices.newKieBuilder(kfs);
            kieBuilder.buildAll();

            if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
                return kieBuilder.getResults().getMessages(Message.Level.ERROR)
                        .stream()
                        .map(Message::getText)
                        .collect(Collectors.joining("\n"));
            }
            return null;
        } catch (Exception e) {
            return "规则验证异常: " + e.getMessage();
        }
    }

    @Override
    public void clearCache(String sceneCode) {
        kieBaseCache.remove(sceneCode);
        log.info("清除场景规则缓存: {}", sceneCode);
    }

    @Override
    public void clearAllCache() {
        kieBaseCache.clear();
        log.info("清除所有规则缓存");
    }

    /**
     * 获取或创建KieBase
     */
    private KieBase getOrCreateKieBase(String sceneCode) {
        return kieBaseCache.computeIfAbsent(sceneCode, code -> {
            try {
                log.info("编译场景规则: {}", code);
                String drl = ruleCompiler.compileByScene(code);

                if (!StringUtils.hasText(drl)) {
                    log.warn("场景 {} 没有可用的规则", code);
                    return createEmptyKieBase();
                }

                return createKieBase(drl, code);
            } catch (Exception e) {
                log.error("创建KieBase失败: sceneCode={}", code, e);
                return null;
            }
        });
    }

    /**
     * 创建KieBase
     */
    private KieBase createKieBase(String drl, String sceneCode) {
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kfs = kieServices.newKieFileSystem();

        String path = "src/main/resources/rules/" + sceneCode.toLowerCase() + ".drl";
        kfs.write(path, drl);

        KieBuilder kieBuilder = kieServices.newKieBuilder(kfs);
        kieBuilder.buildAll();

        if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
            String errors = kieBuilder.getResults().getMessages(Message.Level.ERROR)
                    .stream()
                    .map(Message::getText)
                    .collect(Collectors.joining("\n"));
            log.error("规则编译错误: {}", errors);
            throw new RuntimeException("规则编译失败: " + errors);
        }

        KieContainer kieContainer = kieServices.newKieContainer(
                kieServices.getRepository().getDefaultReleaseId());

        return kieContainer.getKieBase();
    }

    /**
     * 创建空的KieBase
     */
    private KieBase createEmptyKieBase() {
        String emptyDrl = "package rules.empty;\n\n// Empty rule base\n";

        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kfs = kieServices.newKieFileSystem();
        kfs.write("src/main/resources/rules/empty.drl", emptyDrl);

        KieBuilder kieBuilder = kieServices.newKieBuilder(kfs);
        kieBuilder.buildAll();

        KieContainer kieContainer = kieServices.newKieContainer(
                kieServices.getRepository().getDefaultReleaseId());

        return kieContainer.getKieBase();
    }

    /**
     * 执行动作处理器
     */
    private void executeActionHandlers(RuleResult result, RuleContext context) {
        if (result.getMatchedRuleCodes().isEmpty()) {
            return;
        }

        // 获取业务适配器的动作处理器
        BusinessAdapter adapter = adapterMap.get(context.getSceneCode());
        Map<String, SimpleActionHandler> adapterHandlers = null;
        if (adapter != null) {
            adapterHandlers = adapter.getActionHandlers();
        }

        for (String ruleCode : result.getMatchedRuleCodes()) {
            // 获取规则对应的动作
            List<RuleAction> actions = ruleActionService.listByRuleCode(ruleCode);
            if (actions == null || actions.isEmpty()) {
                continue;
            }

            for (RuleAction action : actions) {
                String actionType = action.getActionType();
                Map<String, Object> params = parseActionParams(action.getActionParams());

                // 优先使用业务适配器的处理器
                if (adapterHandlers != null && adapterHandlers.containsKey(actionType)) {
                    try {
                        SimpleActionHandler handler = adapterHandlers.get(actionType);
                        boolean success = handler.execute(actionType, params, context, result);
                        result.addActionResult(new RuleResult.ActionResult(actionType, success, params));
                    } catch (Exception e) {
                        log.error("执行业务动作处理器失败: actionType={}", actionType, e);
                        result.addActionResult(new RuleResult.ActionResult(actionType, false, e.getMessage()));
                    }
                }
                // 使用全局注册的处理器
                else if (actionHandlerRegistry.hasHandler(actionType)) {
                    try {
                        ActionHandler handler = actionHandlerRegistry.getHandler(actionType);
                        RuleResult.ActionResult actionResult = handler.execute(context, result, params);
                        result.addActionResult(actionResult);
                    } catch (Exception e) {
                        log.error("执行全局动作处理器失败: actionType={}", actionType, e);
                        result.addActionResult(new RuleResult.ActionResult(actionType, false, e.getMessage()));
                    }
                } else {
                    log.warn("未找到动作处理器: actionType={}", actionType);
                }
            }
        }
    }

    /**
     * 解析动作参数
     */
    private Map<String, Object> parseActionParams(String actionParams) {
        if (actionParams == null || actionParams.isEmpty()) {
            return new HashMap<>();
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(actionParams, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("解析动作参数失败: {}", actionParams, e);
            return new HashMap<>();
        }
    }

    /**
     * 记录执行日志
     */
    private void logExecution(RuleContext context, RuleResult result, String traceId, long executionTime) {
        try {
            RuleExecutionLog log = new RuleExecutionLog();
            log.setTraceId(traceId);
            log.setSceneCode(context.getSceneCode());
            log.setBusinessId(context.getBusinessId());
            log.setInputData(toJsonString(context.getInputData()));
            log.setOutputData(toJsonString(result.getOutputData()));
            log.setHitRules(String.join(",", result.getMatchedRuleCodes()));
            log.setExecutionTime(Math.toIntExact(executionTime));
            log.setExecuteTime(LocalDateTime.now());

            if (result.getErrorMessage() != null) {
                log.setExecutionResult("ERROR");
                log.setErrorMessage(result.getErrorMessage());
            } else if (result.isMatched()) {
                log.setExecutionResult("HIT");
            } else {
                log.setExecutionResult("MISS");
            }

            ruleExecutionLogService.logExecution(log);
        } catch (Exception e) {
            DroolsRuleEngine.log.error("记录执行日志失败", e);
        }
    }

    /**
     * 对象转JSON字符串
     */
    private String toJsonString(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }
}
