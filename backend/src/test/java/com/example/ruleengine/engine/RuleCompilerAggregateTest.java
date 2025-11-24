package com.example.ruleengine.engine;

import com.example.ruleengine.entity.RuleCondition;
import com.example.ruleengine.entity.RuleDefinition;
import com.example.ruleengine.service.RuleActionService;
import com.example.ruleengine.service.RuleConditionService;
import com.example.ruleengine.service.RuleDefinitionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * RuleCompiler 聚合和表达式功能测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("规则编译器聚合/表达式测试")
public class RuleCompilerAggregateTest {

    @Mock
    private RuleDefinitionService ruleDefinitionService;

    @Mock
    private RuleConditionService ruleConditionService;

    @Mock
    private RuleActionService ruleActionService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Object ruleCompiler;

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
    }

    @Nested
    @DisplayName("聚合条件构建测试")
    class AggregateConditionTest {

        @Test
        @DisplayName("测试 SUM 聚合表达式构建")
        void testBuildSumAggregateExpression() throws Exception {
            Method method = ruleCompiler.getClass().getDeclaredMethod(
                    "buildAggregateExpression", String.class, List.class);
            method.setAccessible(true);

            List<String> fields = Arrays.asList("q1", "q2", "q3");
            String result = (String) method.invoke(ruleCompiler, "SUM", fields);

            assertNotNull(result);
            assertTrue(result.contains("$data.get(\"q1\")"));
            assertTrue(result.contains("$data.get(\"q2\")"));
            assertTrue(result.contains("$data.get(\"q3\")"));
            assertTrue(result.contains("+"));
        }

        @Test
        @DisplayName("测试 AVG 聚合表达式构建")
        void testBuildAvgAggregateExpression() throws Exception {
            Method method = ruleCompiler.getClass().getDeclaredMethod(
                    "buildAggregateExpression", String.class, List.class);
            method.setAccessible(true);

            List<String> fields = Arrays.asList("q1", "q2", "q3");
            String result = (String) method.invoke(ruleCompiler, "AVG", fields);

            assertNotNull(result);
            assertTrue(result.contains("/ 3"));  // 除以字段数量
        }

        @Test
        @DisplayName("测试 MAX 聚合表达式构建")
        void testBuildMaxAggregateExpression() throws Exception {
            Method method = ruleCompiler.getClass().getDeclaredMethod(
                    "buildAggregateExpression", String.class, List.class);
            method.setAccessible(true);

            List<String> fields = Arrays.asList("q1", "q2");
            String result = (String) method.invoke(ruleCompiler, "MAX", fields);

            assertNotNull(result);
            assertTrue(result.contains("Math.max"));
        }

        @Test
        @DisplayName("测试 MIN 聚合表达式构建")
        void testBuildMinAggregateExpression() throws Exception {
            Method method = ruleCompiler.getClass().getDeclaredMethod(
                    "buildAggregateExpression", String.class, List.class);
            method.setAccessible(true);

            List<String> fields = Arrays.asList("q1", "q2");
            String result = (String) method.invoke(ruleCompiler, "MIN", fields);

            assertNotNull(result);
            assertTrue(result.contains("Math.min"));
        }

        @Test
        @DisplayName("测试 COUNT 聚合表达式构建")
        void testBuildCountAggregateExpression() throws Exception {
            Method method = ruleCompiler.getClass().getDeclaredMethod(
                    "buildAggregateExpression", String.class, List.class);
            method.setAccessible(true);

            List<String> fields = Arrays.asList("q1", "q2", "q3");
            String result = (String) method.invoke(ruleCompiler, "COUNT", fields);

            assertNotNull(result);
            assertTrue(result.contains("!= null ? 1 : 0"));
        }

        @Test
        @DisplayName("测试聚合条件完整构建")
        void testBuildAggregateCondition() throws Exception {
            Method method = ruleCompiler.getClass().getDeclaredMethod(
                    "buildAggregateCondition", RuleCondition.class);
            method.setAccessible(true);

            RuleCondition condition = new RuleCondition();
            condition.setRuleId(1L);
            condition.setAggregateFunction("SUM");
            condition.setAggregateFields("[\"q1\",\"q2\",\"q3\"]");
            condition.setOperator("GTE");
            condition.setFieldValue("10");

            String result = (String) method.invoke(ruleCompiler, condition);

            assertNotNull(result);
            assertTrue(result.contains(">= 10"));
        }

        @Test
        @DisplayName("测试聚合条件 BETWEEN 操作符")
        void testBuildAggregateConditionBetween() throws Exception {
            Method method = ruleCompiler.getClass().getDeclaredMethod(
                    "buildAggregateCondition", RuleCondition.class);
            method.setAccessible(true);

            RuleCondition condition = new RuleCondition();
            condition.setRuleId(1L);
            condition.setAggregateFunction("SUM");
            condition.setAggregateFields("[\"q1\",\"q2\",\"q3\"]");
            condition.setOperator("BETWEEN");
            condition.setFieldValue("0,10");

            String result = (String) method.invoke(ruleCompiler, condition);

            assertNotNull(result);
            assertTrue(result.contains(">= 0"));
            assertTrue(result.contains("<= 10"));
        }
    }

    @Nested
    @DisplayName("表达式条件构建测试")
    class ExpressionConditionTest {

        @Test
        @DisplayName("测试 SpEL 到 DRL 语法转换")
        void testConvertSpelToDrl() throws Exception {
            Method method = ruleCompiler.getClass().getDeclaredMethod(
                    "convertSpelToDrl", String.class);
            method.setAccessible(true);

            // 测试 #data["field"] 语法
            String result1 = (String) method.invoke(ruleCompiler, "#data[\"q1\"] + #data[\"q2\"]");
            assertNotNull(result1);
            assertTrue(result1.contains("$data.get(\"q1\")"));
            assertTrue(result1.contains("$data.get(\"q2\")"));

            // 测试 #data['field'] 语法
            String result2 = (String) method.invoke(ruleCompiler, "#data['q1'] + #data['q2']");
            assertNotNull(result2);
            assertTrue(result2.contains("$data.get(\"q1\")"));

            // 测试 #data.field 语法
            String result3 = (String) method.invoke(ruleCompiler, "#data.q1 + #data.q2");
            assertNotNull(result3);
            assertTrue(result3.contains("$data.get(\"q1\")"));
        }

        @Test
        @DisplayName("测试表达式条件构建")
        void testBuildExpressionCondition() throws Exception {
            Method method = ruleCompiler.getClass().getDeclaredMethod(
                    "buildExpressionCondition", RuleCondition.class);
            method.setAccessible(true);

            RuleCondition condition = new RuleCondition();
            condition.setRuleId(1L);
            condition.setOperator("EXPRESSION");
            condition.setExpression("#data['q1'] + #data['q2']");
            condition.setFieldValue(">=10");

            String result = (String) method.invoke(ruleCompiler, condition);

            assertNotNull(result);
            assertTrue(result.contains(">= 10"));
        }

        @Test
        @DisplayName("测试反向计分表达式")
        void testReverseScoreExpression() throws Exception {
            Method method = ruleCompiler.getClass().getDeclaredMethod(
                    "convertSpelToDrl", String.class);
            method.setAccessible(true);

            // 反向计分表达式：6 - #data["q2"]
            String result = (String) method.invoke(ruleCompiler, "6 - #data[\"q2\"]");
            assertNotNull(result);
            assertTrue(result.contains("6 -"));
            assertTrue(result.contains("$data.get(\"q2\")"));
        }
    }

    @Nested
    @DisplayName("操作符提取测试")
    class OperatorExtractionTest {

        @Test
        @DisplayName("测试提取比较操作符")
        void testExtractOperator() throws Exception {
            Method method = ruleCompiler.getClass().getDeclaredMethod(
                    "extractOperator", String.class);
            method.setAccessible(true);

            assertEquals(">=", method.invoke(ruleCompiler, ">=17"));
            assertEquals("<=", method.invoke(ruleCompiler, "<=8"));
            assertEquals(">", method.invoke(ruleCompiler, ">10"));
            assertEquals("<", method.invoke(ruleCompiler, "<5"));
            assertEquals("!=", method.invoke(ruleCompiler, "!=0"));
            assertEquals("==", method.invoke(ruleCompiler, "==100"));
            assertEquals("==", method.invoke(ruleCompiler, "=100"));
        }

        @Test
        @DisplayName("测试提取比较值")
        void testExtractValue() throws Exception {
            Method method = ruleCompiler.getClass().getDeclaredMethod(
                    "extractValue", String.class);
            method.setAccessible(true);

            assertEquals("17", method.invoke(ruleCompiler, ">=17"));
            assertEquals("8", method.invoke(ruleCompiler, "<=8"));
            assertEquals("10", method.invoke(ruleCompiler, ">10"));
            assertEquals("5", method.invoke(ruleCompiler, "<5"));
            assertEquals("0", method.invoke(ruleCompiler, "!=0"));
        }
    }

    @Nested
    @DisplayName("完整规则编译测试")
    class FullRuleCompileTest {

        @Test
        @DisplayName("测试编译含聚合条件的规则")
        void testCompileRuleWithAggregate() throws Exception {
            RuleDefinition rule = new RuleDefinition();
            rule.setId(1L);
            rule.setRuleCode("TEST_AGGREGATE_RULE");
            rule.setRuleName("测试聚合规则");
            rule.setSceneCode("TEST_SCENE");
            rule.setPriority(100);

            RuleCondition condition = new RuleCondition();
            condition.setId(1L);
            condition.setRuleId(1L);
            condition.setGroupId(1L);
            condition.setFieldCode("totalScore");
            condition.setAggregateFunction("SUM");
            condition.setAggregateFields("[\"q1\",\"q2\",\"q3\",\"q4\",\"q5\"]");
            condition.setOperator("BETWEEN");
            condition.setFieldValue("0,3");
            condition.setValueType("CONSTANT");

            when(ruleConditionService.listByRuleId(1L)).thenReturn(Collections.singletonList(condition));
            when(ruleActionService.listByRuleId(1L)).thenReturn(Collections.emptyList());

            Method method = ruleCompiler.getClass().getDeclaredMethod("compile", RuleDefinition.class);
            String drl = (String) method.invoke(ruleCompiler, rule);

            assertNotNull(drl);
            assertTrue(drl.contains("rule \"TEST_AGGREGATE_RULE\""));
            assertTrue(drl.contains("salience 100"));
            assertTrue(drl.contains("$data.get(\"q1\")"));
            assertTrue(drl.contains(">= 0"));
            assertTrue(drl.contains("<= 3"));
        }

        @Test
        @DisplayName("测试编译含表达式条件的规则")
        void testCompileRuleWithExpression() throws Exception {
            RuleDefinition rule = new RuleDefinition();
            rule.setId(1L);
            rule.setRuleCode("TEST_EXPRESSION_RULE");
            rule.setRuleName("测试表达式规则");
            rule.setSceneCode("TEST_SCENE");
            rule.setPriority(100);

            RuleCondition condition = new RuleCondition();
            condition.setId(1L);
            condition.setRuleId(1L);
            condition.setGroupId(1L);
            condition.setFieldCode("calcResult");
            condition.setOperator("EXPRESSION");
            condition.setExpression("#data['q1'] + (6 - #data['q2'])");
            condition.setFieldValue(">=17");
            condition.setValueType("CONSTANT");

            when(ruleConditionService.listByRuleId(1L)).thenReturn(Collections.singletonList(condition));
            when(ruleActionService.listByRuleId(1L)).thenReturn(Collections.emptyList());

            Method method = ruleCompiler.getClass().getDeclaredMethod("compile", RuleDefinition.class);
            String drl = (String) method.invoke(ruleCompiler, rule);

            assertNotNull(drl);
            assertTrue(drl.contains("rule \"TEST_EXPRESSION_RULE\""));
            assertTrue(drl.contains(">= 17"));
        }
    }
}
