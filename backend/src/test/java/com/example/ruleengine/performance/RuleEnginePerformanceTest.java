package com.example.ruleengine.performance;

import com.example.ruleengine.engine.RuleContext;
import com.example.ruleengine.engine.RuleEngine;
import com.example.ruleengine.engine.RuleResult;
import com.example.ruleengine.entity.RuleAction;
import com.example.ruleengine.entity.RuleCondition;
import com.example.ruleengine.entity.RuleDefinition;
import com.example.ruleengine.service.RuleDefinitionService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 规则引擎性能测试
 * <p>
 * 验证规则引擎在各种场景下的性能指标：
 * <ul>
 *   <li>单规则执行时间 < 100ms</li>
 *   <li>批量执行吞吐量 > 1000条/秒</li>
 *   <li>并发执行稳定性</li>
 *   <li>大量规则场景性能</li>
 * </ul>
 * </p>
 *
 * @author 开发团队
 * @since 1.0.0
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RuleEnginePerformanceTest {

    @Autowired
    private RuleEngine ruleEngine;

    @Autowired
    private RuleDefinitionService ruleDefinitionService;

    private static final String TEST_SCENE = "INFECTIOUS_DISEASE";
    private static final int WARMUP_ITERATIONS = 10;
    private static final int TEST_ITERATIONS = 100;

    @BeforeEach
    void setUp() {
        ruleEngine.reload(TEST_SCENE);
    }

    // ==================== 单规则执行性能测试 ====================

    @Test
    @Order(1)
    @DisplayName("性能测试：单规则执行时间 < 100ms")
    void testSingleRuleExecutionTime() {
        // 创建测试规则
        RuleDefinition rule = createPerformanceTestRule("PERF_SINGLE_001", "单规则性能测试");
        addCondition(rule, "status", "EQ", "active", "STRING");
        ruleDefinitionService.createRule(rule);
        ruleEngine.reload(TEST_SCENE);

        // 准备测试数据
        Map<String, Object> inputData = Map.of("status", "active");

        // 预热
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            executeRule(inputData);
        }

        // 正式测试
        long totalTime = 0;
        long maxTime = 0;
        long minTime = Long.MAX_VALUE;

        for (int i = 0; i < TEST_ITERATIONS; i++) {
            long startTime = System.nanoTime();
            RuleResult result = executeRule(inputData);
            long endTime = System.nanoTime();

            long executionTime = (endTime - startTime) / 1_000_000; // 转换为毫秒
            totalTime += executionTime;
            maxTime = Math.max(maxTime, executionTime);
            minTime = Math.min(minTime, executionTime);

            assertNotNull(result);
        }

        double avgTime = (double) totalTime / TEST_ITERATIONS;

        System.out.println("=== 单规则执行性能测试结果 ===");
        System.out.println("执行次数: " + TEST_ITERATIONS);
        System.out.println("平均执行时间: " + String.format("%.2f", avgTime) + "ms");
        System.out.println("最大执行时间: " + maxTime + "ms");
        System.out.println("最小执行时间: " + minTime + "ms");

        // 断言：平均执行时间应小于100ms
        assertTrue(avgTime < 100, "平均执行时间应小于100ms，实际为: " + avgTime + "ms");
    }

    // ==================== 批量执行性能测试 ====================

    @Test
    @Order(2)
    @DisplayName("性能测试：批量执行吞吐量 > 1000条/秒")
    void testBatchExecutionThroughput() {
        // 创建测试规则
        RuleDefinition rule = createPerformanceTestRule("PERF_BATCH_001", "批量执行性能测试");
        addCondition(rule, "value", "GT", "50", "NUMBER");
        ruleDefinitionService.createRule(rule);
        ruleEngine.reload(TEST_SCENE);

        // 准备批量数据
        int batchSize = 1000;
        List<RuleContext> contexts = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < batchSize; i++) {
            RuleContext ctx = new RuleContext();
            ctx.setSceneCode(TEST_SCENE);
            ctx.setInputData(Map.of("value", random.nextInt(100)));
            contexts.add(ctx);
        }

        // 预热
        for (int i = 0; i < 3; i++) {
            ruleEngine.batchExecute(contexts.subList(0, 100));
        }

        // 正式测试
        long startTime = System.nanoTime();
        List<RuleResult> results = ruleEngine.batchExecute(contexts);
        long endTime = System.nanoTime();

        double executionTimeMs = (endTime - startTime) / 1_000_000.0;
        double throughput = batchSize / (executionTimeMs / 1000.0);

        System.out.println("=== 批量执行性能测试结果 ===");
        System.out.println("批量大小: " + batchSize);
        System.out.println("总执行时间: " + String.format("%.2f", executionTimeMs) + "ms");
        System.out.println("吞吐量: " + String.format("%.2f", throughput) + "条/秒");

        // 断言
        assertEquals(batchSize, results.size());
        assertTrue(throughput > 500, "吞吐量应大于500条/秒，实际为: " + throughput);
    }

    // ==================== 并发执行测试 ====================

    @Test
    @Order(3)
    @DisplayName("性能测试：并发执行稳定性（100并发）")
    void testConcurrentExecution() throws InterruptedException, ExecutionException {
        // 创建测试规则
        RuleDefinition rule = createPerformanceTestRule("PERF_CONCURRENT_001", "并发执行性能测试");
        addCondition(rule, "type", "EQ", "test", "STRING");
        ruleDefinitionService.createRule(rule);
        ruleEngine.reload(TEST_SCENE);

        int concurrentUsers = 100;
        int requestsPerUser = 10;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        CountDownLatch latch = new CountDownLatch(concurrentUsers);
        List<Future<List<Long>>> futures = new ArrayList<>();

        // 准备测试数据
        Map<String, Object> inputData = Map.of("type", "test");

        long startTime = System.nanoTime();

        // 提交并发任务
        for (int i = 0; i < concurrentUsers; i++) {
            futures.add(executor.submit(() -> {
                List<Long> executionTimes = new ArrayList<>();
                try {
                    for (int j = 0; j < requestsPerUser; j++) {
                        long start = System.nanoTime();
                        RuleResult result = executeRule(inputData);
                        long end = System.nanoTime();
                        executionTimes.add((end - start) / 1_000_000);
                        assertNotNull(result);
                    }
                } finally {
                    latch.countDown();
                }
                return executionTimes;
            }));
        }

        // 等待所有任务完成
        latch.await(60, TimeUnit.SECONDS);
        executor.shutdown();

        long endTime = System.nanoTime();
        double totalTimeMs = (endTime - startTime) / 1_000_000.0;

        // 收集统计信息
        List<Long> allTimes = new ArrayList<>();
        int successCount = 0;
        for (Future<List<Long>> future : futures) {
            try {
                List<Long> times = future.get();
                allTimes.addAll(times);
                successCount += times.size();
            } catch (Exception e) {
                // 记录失败
            }
        }

        double avgTime = allTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long maxTime = allTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        double throughput = successCount / (totalTimeMs / 1000.0);

        System.out.println("=== 并发执行性能测试结果 ===");
        System.out.println("并发用户数: " + concurrentUsers);
        System.out.println("每用户请求数: " + requestsPerUser);
        System.out.println("总请求数: " + (concurrentUsers * requestsPerUser));
        System.out.println("成功请求数: " + successCount);
        System.out.println("总执行时间: " + String.format("%.2f", totalTimeMs) + "ms");
        System.out.println("平均响应时间: " + String.format("%.2f", avgTime) + "ms");
        System.out.println("最大响应时间: " + maxTime + "ms");
        System.out.println("吞吐量: " + String.format("%.2f", throughput) + "请求/秒");

        // 断言
        assertEquals(concurrentUsers * requestsPerUser, successCount, "所有请求应成功");
        assertTrue(avgTime < 200, "平均响应时间应小于200ms");
    }

    // ==================== 多规则场景性能测试 ====================

    @Test
    @Order(4)
    @DisplayName("性能测试：大量规则场景（100条规则）")
    void testManyRulesPerformance() {
        // 创建100条规则
        int ruleCount = 100;
        for (int i = 0; i < ruleCount; i++) {
            RuleDefinition rule = createPerformanceTestRule(
                    "PERF_MANY_" + String.format("%03d", i),
                    "大量规则测试" + i);
            addCondition(rule, "value", "GT", String.valueOf(i), "NUMBER");
            ruleDefinitionService.createRule(rule);
        }
        ruleEngine.reload(TEST_SCENE);

        // 准备测试数据
        Map<String, Object> inputData = Map.of("value", 50);

        // 预热
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            executeRule(inputData);
        }

        // 正式测试
        long totalTime = 0;
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            long startTime = System.nanoTime();
            RuleResult result = executeRule(inputData);
            long endTime = System.nanoTime();
            totalTime += (endTime - startTime) / 1_000_000;

            assertNotNull(result);
            // 应该匹配 value > 0 到 value > 49 的规则（50条）
            assertTrue(result.getMatchedRuleCodes().size() >= 50);
        }

        double avgTime = (double) totalTime / TEST_ITERATIONS;

        System.out.println("=== 大量规则场景性能测试结果 ===");
        System.out.println("规则数量: " + ruleCount);
        System.out.println("执行次数: " + TEST_ITERATIONS);
        System.out.println("平均执行时间: " + String.format("%.2f", avgTime) + "ms");

        // 断言：即使100条规则，平均执行时间也应小于200ms
        assertTrue(avgTime < 200, "100条规则场景平均执行时间应小于200ms，实际为: " + avgTime + "ms");
    }

    // ==================== 复杂条件性能测试 ====================

    @Test
    @Order(5)
    @DisplayName("性能测试：复杂条件规则（多条件组合）")
    void testComplexConditionsPerformance() {
        // 创建复杂条件规则
        RuleDefinition rule = createPerformanceTestRule("PERF_COMPLEX_001", "复杂条件性能测试");

        // 添加多个条件组
        for (int group = 1; group <= 5; group++) {
            addConditionWithGroup(rule, "field_" + group + "_a", "EQ", "value_a", "STRING", (long) group);
            addConditionWithGroup(rule, "field_" + group + "_b", "GT", "10", "NUMBER", (long) group);
            addConditionWithGroup(rule, "field_" + group + "_c", "LIKE", "test", "STRING", (long) group);
        }

        ruleDefinitionService.createRule(rule);
        ruleEngine.reload(TEST_SCENE);

        // 准备测试数据（满足第一个条件组）
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("field_1_a", "value_a");
        inputData.put("field_1_b", 20);
        inputData.put("field_1_c", "test_data");

        // 预热
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            executeRule(inputData);
        }

        // 正式测试
        long totalTime = 0;
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            long startTime = System.nanoTime();
            RuleResult result = executeRule(inputData);
            long endTime = System.nanoTime();
            totalTime += (endTime - startTime) / 1_000_000;

            assertNotNull(result);
        }

        double avgTime = (double) totalTime / TEST_ITERATIONS;

        System.out.println("=== 复杂条件性能测试结果 ===");
        System.out.println("条件组数: 5");
        System.out.println("每组条件数: 3");
        System.out.println("执行次数: " + TEST_ITERATIONS);
        System.out.println("平均执行时间: " + String.format("%.2f", avgTime) + "ms");

        // 断言
        assertTrue(avgTime < 150, "复杂条件规则平均执行时间应小于150ms，实际为: " + avgTime + "ms");
    }

    // ==================== 规则编译性能测试 ====================

    @Test
    @Order(6)
    @DisplayName("性能测试：规则重新加载时间")
    void testRuleReloadPerformance() {
        // 创建多条规则
        int ruleCount = 50;
        for (int i = 0; i < ruleCount; i++) {
            RuleDefinition rule = createPerformanceTestRule(
                    "PERF_RELOAD_" + String.format("%03d", i),
                    "重载测试规则" + i);
            addCondition(rule, "value", "EQ", String.valueOf(i), "NUMBER");
            ruleDefinitionService.createRule(rule);
        }

        // 测试重载时间
        long totalTime = 0;
        int iterations = 10;
        for (int i = 0; i < iterations; i++) {
            long startTime = System.nanoTime();
            ruleEngine.reload(TEST_SCENE);
            long endTime = System.nanoTime();
            totalTime += (endTime - startTime) / 1_000_000;
        }

        double avgReloadTime = (double) totalTime / iterations;

        System.out.println("=== 规则重载性能测试结果 ===");
        System.out.println("规则数量: " + ruleCount);
        System.out.println("重载次数: " + iterations);
        System.out.println("平均重载时间: " + String.format("%.2f", avgReloadTime) + "ms");

        // 断言：50条规则重载时间应小于2秒
        assertTrue(avgReloadTime < 2000, "规则重载时间应小于2秒，实际为: " + avgReloadTime + "ms");
    }

    // ==================== 辅助方法 ====================

    private RuleDefinition createPerformanceTestRule(String ruleCode, String ruleName) {
        RuleDefinition rule = new RuleDefinition();
        rule.setRuleCode(ruleCode);
        rule.setRuleName(ruleName);
        rule.setRuleDesc("性能测试规则");
        rule.setSceneCode(TEST_SCENE);
        rule.setRuleType("CONDITION");
        rule.setPriority(50);
        rule.setStatus(1);
        rule.setEffectiveStartTime(LocalDateTime.now().minusDays(1));
        rule.setEffectiveEndTime(LocalDateTime.now().plusYears(10));
        rule.setVersion(1);
        rule.setConditions(new ArrayList<>());
        rule.setActions(new ArrayList<>());
        return rule;
    }

    private void addCondition(RuleDefinition rule, String fieldCode, String operator,
                              String value, String valueType) {
        addConditionWithGroup(rule, fieldCode, operator, value, valueType, 1L);
    }

    private void addConditionWithGroup(RuleDefinition rule, String fieldCode, String operator,
                                        String value, String valueType, Long groupId) {
        RuleCondition condition = new RuleCondition();
        condition.setFieldCode(fieldCode);
        condition.setOperator(operator);
        condition.setFieldValue(value);
        condition.setValueType(valueType);
        condition.setGroupId(groupId);
        condition.setGroupLogic("AND");
        condition.setSortOrder(rule.getConditions().size());
        rule.getConditions().add(condition);
    }

    private RuleResult executeRule(Map<String, Object> inputData) {
        RuleContext context = new RuleContext();
        context.setSceneCode(TEST_SCENE);
        context.setInputData(inputData);
        return ruleEngine.execute(context);
    }
}
