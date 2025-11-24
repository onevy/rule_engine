package com.example.ruleengine.engine;

import com.example.ruleengine.entity.RuleCondition;
import com.example.ruleengine.entity.RuleDefinition;
import com.example.ruleengine.service.RuleActionService;
import com.example.ruleengine.service.RuleConditionService;
import com.example.ruleengine.service.RuleDefinitionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 规则引擎性能测试
 * 测试规则编译和表达式计算的性能
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("规则引擎性能测试")
public class RuleEnginePerformanceTest {

    @Mock
    private RuleDefinitionService ruleDefinitionService;

    @Mock
    private RuleConditionService ruleConditionService;

    @Mock
    private RuleActionService ruleActionService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Object ruleCompiler;
    private ExpressionEvaluator expressionEvaluator;

    @BeforeEach
    void setUp() throws Exception {
        // 使用反射创建 RuleCompilerImpl 实例
        Class<?> clazz = Class.forName("com.example.ruleengine.engine.impl.RuleCompilerImpl");
        var constructor = clazz.getDeclaredConstructor(
                RuleDefinitionService.class,
                RuleConditionService.class,
                RuleActionService.class,
                ObjectMapper.class
        );
        ruleCompiler = constructor.newInstance(
                ruleDefinitionService,
                ruleConditionService,
                ruleActionService,
                objectMapper
        );

        expressionEvaluator = new ExpressionEvaluator();
    }

    @Nested
    @DisplayName("表达式计算性能测试")
    class ExpressionPerformanceTest {

        @Test
        @DisplayName("单次表达式计算性能")
        void testSingleExpressionPerformance() {
            Map<String, Object> context = createTestContext(10);

            // 预热
            for (int i = 0; i < 100; i++) {
                expressionEvaluator.evaluateAsNumber("#data['q1'] + #data['q2']", context);
            }

            // 测试
            long startTime = System.nanoTime();
            int iterations = 1000;

            for (int i = 0; i < iterations; i++) {
                expressionEvaluator.evaluateAsNumber("#data['q1'] + #data['q2']", context);
            }

            long endTime = System.nanoTime();
            double avgTimeMs = (endTime - startTime) / 1_000_000.0 / iterations;

            System.out.printf("单次表达式计算平均耗时: %.3f ms%n", avgTimeMs);
            assertTrue(avgTimeMs < 1.0, "单次表达式计算应在1ms内完成");
        }

        @Test
        @DisplayName("复杂表达式计算性能")
        void testComplexExpressionPerformance() {
            Map<String, Object> context = createTestContext(20);

            String complexExpression =
                    "#data['q1'] + (6 - #data['q2']) + (6 - #data['q3']) + " +
                    "(6 - #data['q4']) + (6 - #data['q5']) + #data['q6'] + " +
                    "#data['q7'] + #data['q8'] + #data['q9'] + #data['q10']";

            // 预热
            for (int i = 0; i < 100; i++) {
                expressionEvaluator.evaluateAsNumber(complexExpression, context);
            }

            // 测试
            long startTime = System.nanoTime();
            int iterations = 1000;

            for (int i = 0; i < iterations; i++) {
                expressionEvaluator.evaluateAsNumber(complexExpression, context);
            }

            long endTime = System.nanoTime();
            double avgTimeMs = (endTime - startTime) / 1_000_000.0 / iterations;

            System.out.printf("复杂表达式计算平均耗时: %.3f ms%n", avgTimeMs);
            assertTrue(avgTimeMs < 5.0, "复杂表达式计算应在5ms内完成");
        }

        @Test
        @DisplayName("聚合函数性能测试")
        void testAggregatePerformance() {
            Map<String, Object> context = createTestContext(100);
            List<String> fields = new ArrayList<>();
            for (int i = 1; i <= 100; i++) {
                fields.add("q" + i);
            }

            // 预热
            for (int i = 0; i < 100; i++) {
                expressionEvaluator.evaluateAggregate("SUM", fields, context);
            }

            // 测试各聚合函数
            String[] functions = {"SUM", "AVG", "MAX", "MIN", "COUNT"};
            for (String func : functions) {
                long startTime = System.nanoTime();
                int iterations = 1000;

                for (int i = 0; i < iterations; i++) {
                    expressionEvaluator.evaluateAggregate(func, fields, context);
                }

                long endTime = System.nanoTime();
                double avgTimeMs = (endTime - startTime) / 1_000_000.0 / iterations;

                System.out.printf("%s 聚合函数(100字段)平均耗时: %.3f ms%n", func, avgTimeMs);
                assertTrue(avgTimeMs < 1.0, func + " 聚合应在1ms内完成");
            }
        }
    }

    @Nested
    @DisplayName("规则编译性能测试")
    class CompilePerformanceTest {

        @Test
        @DisplayName("单规则编译性能")
        void testSingleRuleCompilePerformance() throws Exception {
            RuleDefinition rule = createTestRule(1);
            List<RuleCondition> conditions = createTestConditions(1, 5);

            when(ruleConditionService.listByRuleId(1L)).thenReturn(conditions);
            when(ruleActionService.listByRuleId(1L)).thenReturn(Collections.emptyList());

            Method method = ruleCompiler.getClass().getDeclaredMethod("compile", RuleDefinition.class);

            // 预热
            for (int i = 0; i < 10; i++) {
                method.invoke(ruleCompiler, rule);
            }

            // 测试
            long startTime = System.nanoTime();
            int iterations = 100;

            for (int i = 0; i < iterations; i++) {
                method.invoke(ruleCompiler, rule);
            }

            long endTime = System.nanoTime();
            double avgTimeMs = (endTime - startTime) / 1_000_000.0 / iterations;

            System.out.printf("单规则编译平均耗时: %.3f ms%n", avgTimeMs);
            assertTrue(avgTimeMs < 10.0, "单规则编译应在10ms内完成");
        }

        @Test
        @DisplayName("复杂规则编译性能（聚合条件）")
        void testComplexRuleWithAggregatePerformance() throws Exception {
            RuleDefinition rule = createTestRule(1);
            List<RuleCondition> conditions = createAggregateConditions(1);

            when(ruleConditionService.listByRuleId(1L)).thenReturn(conditions);
            when(ruleActionService.listByRuleId(1L)).thenReturn(Collections.emptyList());

            Method method = ruleCompiler.getClass().getDeclaredMethod("compile", RuleDefinition.class);

            // 预热
            for (int i = 0; i < 10; i++) {
                method.invoke(ruleCompiler, rule);
            }

            // 测试
            long startTime = System.nanoTime();
            int iterations = 100;

            for (int i = 0; i < iterations; i++) {
                method.invoke(ruleCompiler, rule);
            }

            long endTime = System.nanoTime();
            double avgTimeMs = (endTime - startTime) / 1_000_000.0 / iterations;

            System.out.printf("聚合规则编译平均耗时: %.3f ms%n", avgTimeMs);
            assertTrue(avgTimeMs < 20.0, "聚合规则编译应在20ms内完成");
        }
    }

    @Nested
    @DisplayName("并发性能测试")
    class ConcurrencyPerformanceTest {

        @Test
        @DisplayName("并发表达式计算")
        void testConcurrentExpressionEvaluation() throws Exception {
            int threadCount = 10;
            int iterationsPerThread = 100;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            Map<String, Object> context = createTestContext(20);
            String expression = "#data['q1'] + #data['q2'] + #data['q3']";

            // 预热
            for (int i = 0; i < 100; i++) {
                expressionEvaluator.evaluateAsNumber(expression, context);
            }

            long startTime = System.nanoTime();

            List<Future<Boolean>> futures = new ArrayList<>();
            for (int t = 0; t < threadCount; t++) {
                futures.add(executor.submit(() -> {
                    for (int i = 0; i < iterationsPerThread; i++) {
                        double result = expressionEvaluator.evaluateAsNumber(expression, context);
                        if (result == 0) return false; // 表示计算出错
                    }
                    return true;
                }));
            }

            // 等待所有任务完成
            for (Future<Boolean> future : futures) {
                assertTrue(future.get(30, TimeUnit.SECONDS));
            }

            long endTime = System.nanoTime();
            double totalTimeMs = (endTime - startTime) / 1_000_000.0;
            int totalOperations = threadCount * iterationsPerThread;
            double throughput = totalOperations / (totalTimeMs / 1000.0);

            System.out.printf("并发测试 (%d线程, %d次/线程):%n", threadCount, iterationsPerThread);
            System.out.printf("  总耗时: %.2f ms%n", totalTimeMs);
            System.out.printf("  吞吐量: %.0f ops/sec%n", throughput);

            executor.shutdown();
            assertTrue(throughput > 1000, "吞吐量应大于1000 ops/sec");
        }

        @Test
        @DisplayName("并发聚合计算")
        void testConcurrentAggregateEvaluation() throws Exception {
            int threadCount = 10;
            int iterationsPerThread = 100;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            Map<String, Object> context = createTestContext(50);
            List<String> fields = IntStream.rangeClosed(1, 50)
                    .mapToObj(i -> "q" + i)
                    .collect(java.util.stream.Collectors.toList());

            // 预热
            for (int i = 0; i < 100; i++) {
                expressionEvaluator.evaluateAggregate("SUM", fields, context);
            }

            long startTime = System.nanoTime();

            List<Future<Boolean>> futures = new ArrayList<>();
            for (int t = 0; t < threadCount; t++) {
                futures.add(executor.submit(() -> {
                    for (int i = 0; i < iterationsPerThread; i++) {
                        double result = expressionEvaluator.evaluateAggregate("SUM", fields, context);
                        if (result == 0) return false;
                    }
                    return true;
                }));
            }

            // 等待所有任务完成
            for (Future<Boolean> future : futures) {
                assertTrue(future.get(30, TimeUnit.SECONDS));
            }

            long endTime = System.nanoTime();
            double totalTimeMs = (endTime - startTime) / 1_000_000.0;
            int totalOperations = threadCount * iterationsPerThread;
            double throughput = totalOperations / (totalTimeMs / 1000.0);

            System.out.printf("聚合并发测试 (%d线程, %d次/线程):%n", threadCount, iterationsPerThread);
            System.out.printf("  总耗时: %.2f ms%n", totalTimeMs);
            System.out.printf("  吞吐量: %.0f ops/sec%n", throughput);

            executor.shutdown();
            assertTrue(throughput > 5000, "聚合吞吐量应大于5000 ops/sec");
        }
    }

    // ==================== 辅助方法 ====================

    private Map<String, Object> createTestContext(int fieldCount) {
        Map<String, Object> context = new HashMap<>();
        Random random = new Random(42); // 固定种子保证可重复性
        for (int i = 1; i <= fieldCount; i++) {
            context.put("q" + i, random.nextInt(5) + 1); // 1-5分
        }
        return context;
    }

    private RuleDefinition createTestRule(long id) {
        RuleDefinition rule = new RuleDefinition();
        rule.setId(id);
        rule.setRuleCode("TEST_RULE_" + id);
        rule.setRuleName("测试规则" + id);
        rule.setSceneCode("TEST_SCENE");
        rule.setPriority(100);
        return rule;
    }

    private List<RuleCondition> createTestConditions(long ruleId, int count) {
        List<RuleCondition> conditions = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            RuleCondition condition = new RuleCondition();
            condition.setId((long) i);
            condition.setRuleId(ruleId);
            condition.setGroupId(1L);
            condition.setFieldCode("q" + i);
            condition.setOperator("GTE");
            condition.setFieldValue("3");
            condition.setValueType("CONSTANT");
            conditions.add(condition);
        }
        return conditions;
    }

    private List<RuleCondition> createAggregateConditions(long ruleId) {
        List<RuleCondition> conditions = new ArrayList<>();

        // 聚合条件
        RuleCondition aggregateCondition = new RuleCondition();
        aggregateCondition.setId(1L);
        aggregateCondition.setRuleId(ruleId);
        aggregateCondition.setGroupId(1L);
        aggregateCondition.setFieldCode("totalScore");
        aggregateCondition.setAggregateFunction("SUM");
        aggregateCondition.setAggregateFields("[\"q1\",\"q2\",\"q3\",\"q4\",\"q5\"]");
        aggregateCondition.setOperator("GTE");
        aggregateCondition.setFieldValue("15");
        aggregateCondition.setValueType("CONSTANT");
        conditions.add(aggregateCondition);

        // 表达式条件
        RuleCondition expressionCondition = new RuleCondition();
        expressionCondition.setId(2L);
        expressionCondition.setRuleId(ruleId);
        expressionCondition.setGroupId(1L);
        expressionCondition.setFieldCode("calcScore");
        expressionCondition.setOperator("EXPRESSION");
        expressionCondition.setExpression("#data['q1'] + (6 - #data['q2'])");
        expressionCondition.setFieldValue(">=8");
        expressionCondition.setValueType("CONSTANT");
        conditions.add(expressionCondition);

        return conditions;
    }
}
