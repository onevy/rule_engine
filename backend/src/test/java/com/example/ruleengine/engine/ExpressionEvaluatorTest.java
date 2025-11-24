package com.example.ruleengine.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExpressionEvaluator 单元测试
 */
@DisplayName("表达式计算器测试")
public class ExpressionEvaluatorTest {

    private ExpressionEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new ExpressionEvaluator();
    }

    @Nested
    @DisplayName("基本表达式计算测试")
    class BasicExpressionTest {

        @Test
        @DisplayName("测试简单数学表达式")
        void testSimpleMath() {
            Map<String, Object> context = new HashMap<>();
            context.put("a", 10);
            context.put("b", 5);

            // 加法
            assertEquals(15.0, evaluator.evaluateAsNumber("#data['a'] + #data['b']", context));

            // 减法
            assertEquals(5.0, evaluator.evaluateAsNumber("#data['a'] - #data['b']", context));

            // 乘法
            assertEquals(50.0, evaluator.evaluateAsNumber("#data['a'] * #data['b']", context));

            // 除法
            assertEquals(2.0, evaluator.evaluateAsNumber("#data['a'] / #data['b']", context));
        }

        @Test
        @DisplayName("测试反向计分计算")
        void testReverseScoring() {
            Map<String, Object> context = new HashMap<>();
            context.put("q2", 4);
            context.put("q4", 3);
            context.put("q5", 2);

            // 反向计分：6 - 原分值
            assertEquals(2.0, evaluator.evaluateAsNumber("6 - #data['q2']", context));
            assertEquals(3.0, evaluator.evaluateAsNumber("6 - #data['q4']", context));
            assertEquals(4.0, evaluator.evaluateAsNumber("6 - #data['q5']", context));
        }

        @Test
        @DisplayName("测试复杂表达式")
        void testComplexExpression() {
            Map<String, Object> context = new HashMap<>();
            context.put("q1", 5);
            context.put("q2", 4);
            context.put("q4", 3);
            context.put("q5", 2);
            context.put("q13", 1);

            // 平和质得分 = q1 + (6-q2) + (6-q4) + (6-q5) + (6-q13)
            // = 5 + 2 + 3 + 4 + 5 = 19
            double result = evaluator.evaluateAsNumber(
                    "#data['q1'] + (6 - #data['q2']) + (6 - #data['q4']) + (6 - #data['q5']) + (6 - #data['q13'])",
                    context
            );
            assertEquals(19.0, result);
        }

        @Test
        @DisplayName("测试布尔表达式")
        void testBooleanExpression() {
            Map<String, Object> context = new HashMap<>();
            context.put("score", 17);
            context.put("otherMax", 7);

            assertTrue(evaluator.evaluateAsBoolean("#data['score'] >= 17", context));
            assertTrue(evaluator.evaluateAsBoolean("#data['otherMax'] < 8", context));
            assertTrue(evaluator.evaluateAsBoolean(
                    "#data['score'] >= 17 and #data['otherMax'] < 8", context));
        }
    }

    @Nested
    @DisplayName("聚合函数测试")
    class AggregateFunctionTest {

        @Test
        @DisplayName("测试 SUM 聚合函数")
        void testSum() {
            Map<String, Object> context = new HashMap<>();
            context.put("q1", 3);
            context.put("q2", 4);
            context.put("q3", 5);

            double result = evaluator.evaluateAggregate("SUM",
                    Arrays.asList("q1", "q2", "q3"), context);
            assertEquals(12.0, result);
        }

        @Test
        @DisplayName("测试 AVG 聚合函数")
        void testAvg() {
            Map<String, Object> context = new HashMap<>();
            context.put("q1", 3);
            context.put("q2", 4);
            context.put("q3", 5);

            double result = evaluator.evaluateAggregate("AVG",
                    Arrays.asList("q1", "q2", "q3"), context);
            assertEquals(4.0, result);
        }

        @Test
        @DisplayName("测试 MAX 聚合函数")
        void testMax() {
            Map<String, Object> context = new HashMap<>();
            context.put("q1", 3);
            context.put("q2", 7);
            context.put("q3", 5);

            double result = evaluator.evaluateAggregate("MAX",
                    Arrays.asList("q1", "q2", "q3"), context);
            assertEquals(7.0, result);
        }

        @Test
        @DisplayName("测试 MIN 聚合函数")
        void testMin() {
            Map<String, Object> context = new HashMap<>();
            context.put("q1", 3);
            context.put("q2", 7);
            context.put("q3", 5);

            double result = evaluator.evaluateAggregate("MIN",
                    Arrays.asList("q1", "q2", "q3"), context);
            assertEquals(3.0, result);
        }

        @Test
        @DisplayName("测试 COUNT 聚合函数")
        void testCount() {
            Map<String, Object> context = new HashMap<>();
            context.put("q1", 3);
            context.put("q2", 7);
            context.put("q3", null);

            double result = evaluator.evaluateAggregate("COUNT",
                    Arrays.asList("q1", "q2", "q3"), context);
            assertEquals(2.0, result);  // q3 为 null，不计入
        }

        @Test
        @DisplayName("测试空字段列表")
        void testEmptyFields() {
            Map<String, Object> context = new HashMap<>();
            context.put("q1", 3);

            double result = evaluator.evaluateAggregate("SUM",
                    Arrays.asList(), context);
            assertEquals(0.0, result);
        }

        @Test
        @DisplayName("测试缺失字段处理")
        void testMissingFields() {
            Map<String, Object> context = new HashMap<>();
            context.put("q1", 3);
            // q2, q3 不存在

            double result = evaluator.evaluateAggregate("SUM",
                    Arrays.asList("q1", "q2", "q3"), context);
            assertEquals(3.0, result);  // 只有 q1 有值
        }
    }

    @Nested
    @DisplayName("表达式验证测试")
    class ValidationTest {

        @Test
        @DisplayName("测试有效表达式验证")
        void testValidExpression() {
            String error = evaluator.validate("#data['q1'] + #data['q2']");
            assertNull(error);
        }

        @Test
        @DisplayName("测试无效表达式验证")
        void testInvalidExpression() {
            // 使用一个明确无效的表达式
            String error = evaluator.validate("#data['q1'] && ||");
            assertNotNull(error);
            assertTrue(error.contains("语法错误"));
        }
    }

    @Nested
    @DisplayName("评估量表场景测试")
    class AssessmentScenarioTest {

        @Test
        @DisplayName("测试自理能力评估总分计算")
        void testSelfCareAssessmentTotalScore() {
            Map<String, Object> context = new HashMap<>();
            context.put("q1", 3);  // 进餐
            context.put("q2", 3);  // 梳洗
            context.put("q3", 3);  // 穿衣
            context.put("q4", 5);  // 如厕
            context.put("q5", 1);  // 活动

            double totalScore = evaluator.evaluateAggregate("SUM",
                    Arrays.asList("q1", "q2", "q3", "q4", "q5"), context);
            assertEquals(15.0, totalScore);
        }

        @Test
        @DisplayName("测试体质评估气虚质得分计算")
        void testConstitutionQixuScore() {
            Map<String, Object> context = new HashMap<>();
            // 气虚质题目: q2, q3, q4, q14
            context.put("q2", 4);
            context.put("q3", 4);
            context.put("q4", 4);
            context.put("q14", 4);

            double qixuScore = evaluator.evaluateAggregate("SUM",
                    Arrays.asList("q2", "q3", "q4", "q14"), context);
            assertEquals(16.0, qixuScore);
            assertTrue(qixuScore >= 11);  // 判定为"是"
        }

        @Test
        @DisplayName("测试体质评估平和质得分计算（含反向计分）")
        void testConstitutionPingheScore() {
            Map<String, Object> context = new HashMap<>();
            // 平和质题目: q1(正向), q2(反向), q4(反向), q5(反向), q13(反向)
            context.put("q1", 5);   // 精力充沛
            context.put("q2", 1);   // 容易疲乏（反向：6-1=5）
            context.put("q4", 1);   // 心慌（反向：6-1=5）
            context.put("q5", 1);   // 情绪低落（反向：6-1=5）
            context.put("q13", 1);  // 容易感冒（反向：6-1=5）

            // 平和质得分 = q1 + (6-q2) + (6-q4) + (6-q5) + (6-q13)
            double pingheScore = evaluator.evaluateAsNumber(
                    "#data['q1'] + (6 - #data['q2']) + (6 - #data['q4']) + (6 - #data['q5']) + (6 - #data['q13'])",
                    context
            );
            assertEquals(25.0, pingheScore);
            assertTrue(pingheScore >= 17);  // 满足平和质条件之一
        }
    }

    @Nested
    @DisplayName("静态聚合函数测试")
    class StaticAggregateFunctionsTest {

        @Test
        @DisplayName("测试静态 sum 函数")
        void testStaticSum() {
            assertEquals(15.0, ExpressionEvaluator.sum(1, 2, 3, 4, 5));
            assertEquals(0.0, ExpressionEvaluator.sum());
        }

        @Test
        @DisplayName("测试静态 avg 函数")
        void testStaticAvg() {
            assertEquals(3.0, ExpressionEvaluator.avg(1, 2, 3, 4, 5));
            assertEquals(0.0, ExpressionEvaluator.avg());
        }

        @Test
        @DisplayName("测试静态 max 函数")
        void testStaticMax() {
            assertEquals(5.0, ExpressionEvaluator.max(1, 2, 3, 4, 5));
            assertEquals(0.0, ExpressionEvaluator.max());
        }

        @Test
        @DisplayName("测试静态 min 函数")
        void testStaticMin() {
            assertEquals(1.0, ExpressionEvaluator.min(1, 2, 3, 4, 5));
            assertEquals(0.0, ExpressionEvaluator.min());
        }

        @Test
        @DisplayName("测试静态 count 函数")
        void testStaticCount() {
            assertEquals(5, ExpressionEvaluator.count(1, 2, 3, 4, 5));
            assertEquals(3, ExpressionEvaluator.count(1, null, 3, null, 5));
            assertEquals(0, ExpressionEvaluator.count());
        }
    }
}
