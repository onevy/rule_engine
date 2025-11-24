package com.example.ruleengine.adapter;

import com.example.ruleengine.engine.RuleResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 评估量表适配器单元测试
 *
 * 测试内容：
 * - TotalScoreAssessmentAdapter 总分计算和预处理
 * - 数据验证功能
 */
public class AssessmentAdapterTest {

    @Nested
    @DisplayName("总分区间判定适配器测试")
    class TotalScoreAssessmentAdapterTest {

        private TotalScoreAssessmentAdapter adapter;

        @BeforeEach
        void setUp() {
            adapter = new TotalScoreAssessmentAdapter();
        }

        @Test
        @DisplayName("测试场景编码")
        void testSceneCode() {
            assertEquals("SELF_CARE_ASSESSMENT", adapter.getSceneCode());
        }

        @Test
        @DisplayName("测试正常数据预处理 - 计算总分")
        void testPreProcessWithValidData() {
            Map<String, Object> inputData = new HashMap<>();
            inputData.put("q1", 3);  // 进餐：需协助
            inputData.put("q2", 3);  // 梳洗：部分完成
            inputData.put("q3", 3);  // 穿衣：部分完成
            inputData.put("q4", 5);  // 如厕：经常失禁
            inputData.put("q5", 1);  // 活动：借助辅助

            Map<String, Object> result = adapter.preProcess(inputData);

            // 验证总分计算
            assertEquals(15, result.get("totalScore"));
            assertEquals(5, result.get("itemCount"));
            assertEquals(5, result.get("maxQuestionNum"));

            // 验证原始字段保留
            assertEquals(3, result.get("q1"));
            assertEquals(3, result.get("q2"));
        }

        @Test
        @DisplayName("测试空数据预处理")
        void testPreProcessWithNullData() {
            Map<String, Object> result = adapter.preProcess(null);

            assertEquals(0, result.get("totalScore"));
            assertEquals(0, result.get("itemCount"));
        }

        @Test
        @DisplayName("测试可自理场景 - 总分0-3分")
        void testSelfCareLevel1() {
            Map<String, Object> inputData = new HashMap<>();
            inputData.put("q1", 0);  // 进餐：独立完成
            inputData.put("q2", 0);  // 梳洗：独立完成
            inputData.put("q3", 0);  // 穿衣：独立完成
            inputData.put("q4", 0);  // 如厕：不需协助
            inputData.put("q5", 1);  // 活动：借助辅助

            Map<String, Object> result = adapter.preProcess(inputData);

            assertEquals(1, result.get("totalScore"));
            // 总分1分，属于可自理范围 (0-3分)
        }

        @Test
        @DisplayName("测试不能自理场景 - 总分≥19分")
        void testSelfCareLevel4() {
            Map<String, Object> inputData = new HashMap<>();
            inputData.put("q1", 5);  // 进餐：完全需要帮助
            inputData.put("q2", 7);  // 梳洗：完全需要帮助
            inputData.put("q3", 5);  // 穿衣：完全需要帮助
            inputData.put("q4", 10); // 如厕：完全失禁
            inputData.put("q5", 10); // 活动：卧床不起

            Map<String, Object> result = adapter.preProcess(inputData);

            assertEquals(37, result.get("totalScore"));
            // 总分37分，属于不能自理范围 (≥19分)
        }

        @Test
        @DisplayName("测试非标准字段忽略")
        void testIgnoreNonQuestionFields() {
            Map<String, Object> inputData = new HashMap<>();
            inputData.put("q1", 3);
            inputData.put("q2", 3);
            inputData.put("patientName", "张三");
            inputData.put("assessmentDate", "2024-01-01");

            Map<String, Object> result = adapter.preProcess(inputData);

            assertEquals(6, result.get("totalScore"));
            assertEquals(2, result.get("itemCount"));
            // 非 q{n} 格式的字段不应被计算
        }

        @Test
        @DisplayName("测试数据验证 - 空数据")
        void testValidateEmptyData() {
            String error = adapter.validate(new HashMap<>());
            assertNotNull(error);
            assertEquals("评估数据不能为空", error);
        }

        @Test
        @DisplayName("测试数据验证 - 无题目字段")
        void testValidateNoQuestionFields() {
            Map<String, Object> data = new HashMap<>();
            data.put("patientName", "张三");

            String error = adapter.validate(data);
            assertNotNull(error);
            assertTrue(error.contains("题目字段"));
        }

        @Test
        @DisplayName("测试数据验证 - 有效数据")
        void testValidateValidData() {
            Map<String, Object> data = new HashMap<>();
            data.put("q1", 3);

            String error = adapter.validate(data);
            assertNull(error);
        }

        @Test
        @DisplayName("测试后处理 - 匹配结果")
        void testPostProcessMatched() {
            RuleResult ruleResult = new RuleResult();
            ruleResult.setMatched(true);
            ruleResult.getMatchedRuleCodes().add("SELF_CARE_LEVEL_3");
            ruleResult.getOutputData().put("assessmentResult", "中度依赖");
            ruleResult.getOutputData().put("level", 3);
            ruleResult.getOutputData().put("totalScore", 15);
            ruleResult.getOutputData().put("suggestion", "建议加强日常照护");
            ruleResult.setExecutionTime(10L);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = (Map<String, Object>) adapter.postProcess(ruleResult);

            assertTrue((Boolean) response.get("matched"));
            assertEquals("中度依赖", response.get("assessmentResult"));
            assertEquals(3, response.get("level"));
            assertEquals("建议加强日常照护", response.get("suggestion"));
        }

        @Test
        @DisplayName("测试动作处理器注册")
        void testActionHandlers() {
            var handlers = adapter.getActionHandlers();

            assertNotNull(handlers);
            assertTrue(handlers.containsKey("GENERATE_ASSESSMENT_REPORT"));
            assertTrue(handlers.containsKey("PROVIDE_HEALTH_SUGGESTION"));
        }
    }

    @Nested
    @DisplayName("动态题目数量测试")
    class DynamicQuestionCountTest {

        private TotalScoreAssessmentAdapter adapter;

        @BeforeEach
        void setUp() {
            adapter = new TotalScoreAssessmentAdapter();
        }

        @Test
        @DisplayName("测试任意数量题目 - 10题")
        void testTenQuestions() {
            Map<String, Object> inputData = new HashMap<>();
            for (int i = 1; i <= 10; i++) {
                inputData.put("q" + i, 2);
            }

            Map<String, Object> result = adapter.preProcess(inputData);

            assertEquals(20, result.get("totalScore"));
            assertEquals(10, result.get("itemCount"));
            assertEquals(10, result.get("maxQuestionNum"));
        }

        @Test
        @DisplayName("测试非连续题号")
        void testNonConsecutiveQuestions() {
            Map<String, Object> inputData = new HashMap<>();
            inputData.put("q1", 5);
            inputData.put("q5", 3);
            inputData.put("q10", 2);

            Map<String, Object> result = adapter.preProcess(inputData);

            assertEquals(10, result.get("totalScore"));
            assertEquals(3, result.get("itemCount"));
            assertEquals(10, result.get("maxQuestionNum"));
        }
    }
}
