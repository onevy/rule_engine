package com.example.ruleengine.adapter;

import com.example.ruleengine.engine.RuleContext;
import com.example.ruleengine.engine.RuleEngine;
import com.example.ruleengine.engine.RuleResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 评估量表规则引擎集成测试
 *
 * 测试完整的评估流程：
 * 1. 输入原始评估数据
 * 2. 适配器预处理（计算总分/分组得分）
 * 3. 规则引擎执行判定
 * 4. 适配器后处理（格式化输出）
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AssessmentIntegrationTest {

    @Autowired
    private RuleEngine ruleEngine;

    @Nested
    @DisplayName("自理能力评估集成测试")
    class SelfCareAssessmentIntegrationTest {

        private static final String SCENE_CODE = "SELF_CARE_ASSESSMENT";

        @BeforeEach
        void setUp() {
            ruleEngine.reload(SCENE_CODE);
        }

        @Test
        @DisplayName("测试可自理判定 - 总分0-3分")
        void testSelfCareLevel1() {
            Map<String, Object> inputData = new HashMap<>();
            inputData.put("q1", 0);  // 进餐：独立完成
            inputData.put("q2", 0);  // 梳洗：独立完成
            inputData.put("q3", 0);  // 穿衣：独立完成
            inputData.put("q4", 0);  // 如厕：不需协助
            inputData.put("q5", 1);  // 活动：借助辅助

            RuleContext context = new RuleContext();
            context.setSceneCode(SCENE_CODE);
            context.setInputData(inputData);

            RuleResult result = ruleEngine.execute(context);

            assertTrue(result.isMatched());
            assertTrue(result.getMatchedRuleCodes().contains("SELF_CARE_LEVEL_1"));
            assertEquals("可自理", result.getOutputData().get("assessmentResult"));
            assertEquals(1, result.getOutputData().get("level"));
        }

        @Test
        @DisplayName("测试轻度依赖判定 - 总分4-8分")
        void testSelfCareLevel2() {
            Map<String, Object> inputData = new HashMap<>();
            inputData.put("q1", 0);  // 进餐：独立完成
            inputData.put("q2", 1);  // 梳洗：需协助
            inputData.put("q3", 3);  // 穿衣：部分完成
            inputData.put("q4", 1);  // 如厕：偶尔失禁
            inputData.put("q5", 1);  // 活动：借助辅助

            RuleContext context = new RuleContext();
            context.setSceneCode(SCENE_CODE);
            context.setInputData(inputData);

            RuleResult result = ruleEngine.execute(context);

            assertTrue(result.isMatched());
            assertTrue(result.getMatchedRuleCodes().contains("SELF_CARE_LEVEL_2"));
            assertEquals("轻度依赖", result.getOutputData().get("assessmentResult"));
            assertEquals(2, result.getOutputData().get("level"));
        }

        @Test
        @DisplayName("测试中度依赖判定 - 总分9-18分")
        void testSelfCareLevel3() {
            Map<String, Object> inputData = new HashMap<>();
            inputData.put("q1", 3);  // 进餐：需协助
            inputData.put("q2", 3);  // 梳洗：部分完成
            inputData.put("q3", 3);  // 穿衣：部分完成
            inputData.put("q4", 5);  // 如厕：经常失禁
            inputData.put("q5", 1);  // 活动：借助辅助

            RuleContext context = new RuleContext();
            context.setSceneCode(SCENE_CODE);
            context.setInputData(inputData);

            RuleResult result = ruleEngine.execute(context);

            assertTrue(result.isMatched());
            assertTrue(result.getMatchedRuleCodes().contains("SELF_CARE_LEVEL_3"));
            assertEquals("中度依赖", result.getOutputData().get("assessmentResult"));
            assertEquals(3, result.getOutputData().get("level"));
        }

        @Test
        @DisplayName("测试不能自理判定 - 总分≥19分")
        void testSelfCareLevel4() {
            Map<String, Object> inputData = new HashMap<>();
            inputData.put("q1", 5);  // 进餐：完全需要帮助
            inputData.put("q2", 7);  // 梳洗：完全需要帮助
            inputData.put("q3", 5);  // 穿衣：完全需要帮助
            inputData.put("q4", 10); // 如厕：完全失禁
            inputData.put("q5", 10); // 活动：卧床不起

            RuleContext context = new RuleContext();
            context.setSceneCode(SCENE_CODE);
            context.setInputData(inputData);

            RuleResult result = ruleEngine.execute(context);

            assertTrue(result.isMatched());
            assertTrue(result.getMatchedRuleCodes().contains("SELF_CARE_LEVEL_4"));
            assertEquals("不能自理", result.getOutputData().get("assessmentResult"));
            assertEquals(4, result.getOutputData().get("level"));
        }
    }

    @Nested
    @DisplayName("体质评估集成测试")
    class ConstitutionAssessmentIntegrationTest {

        private static final String SCENE_CODE = "CONSTITUTION_ASSESSMENT";

        @BeforeEach
        void setUp() {
            ruleEngine.reload(SCENE_CODE);
        }

        @Test
        @DisplayName("测试气虚质=是判定")
        void testQixuYes() {
            // 构造33题的评估数据，气虚质题目(2,3,4,14)高分
            Map<String, Object> inputData = createBaseConstitutionData();
            inputData.put("q2", 4);  // 容易疲乏
            inputData.put("q3", 4);  // 气短
            inputData.put("q4", 4);  // 心慌
            inputData.put("q14", 4); // 容易感冒

            RuleContext context = new RuleContext();
            context.setSceneCode(SCENE_CODE);
            context.setInputData(inputData);

            RuleResult result = ruleEngine.execute(context);

            assertTrue(result.isMatched());
            // 气虚质得分 = 4+4+4+4 = 16 >= 11，判定为"是"
            assertEquals("是", result.getOutputData().get("qixu_result"));
        }

        @Test
        @DisplayName("测试气虚质=倾向是判定")
        void testQixuTend() {
            Map<String, Object> inputData = createBaseConstitutionData();
            inputData.put("q2", 3);  // 容易疲乏
            inputData.put("q3", 2);  // 气短
            inputData.put("q4", 2);  // 心慌
            inputData.put("q14", 3); // 容易感冒

            RuleContext context = new RuleContext();
            context.setSceneCode(SCENE_CODE);
            context.setInputData(inputData);

            RuleResult result = ruleEngine.execute(context);

            assertTrue(result.isMatched());
            // 气虚质得分 = 3+2+2+3 = 10，在9-10分范围内，判定为"倾向是"
            assertEquals("倾向是", result.getOutputData().get("qixu_result"));
        }

        @Test
        @DisplayName("测试平和质=是判定")
        void testPingheYes() {
            // 平和质题目(1,2,4,5,13)，其中2,4,5,13反向计分
            // 平和质得分≥17 且 其他体质最高分<8
            Map<String, Object> inputData = createLowScoreConstitutionData();
            inputData.put("q1", 5);   // 精力充沛（正向）
            inputData.put("q2", 1);   // 容易疲乏（反向：6-1=5）
            inputData.put("q4", 1);   // 心慌（反向：6-1=5）
            inputData.put("q5", 1);   // 情绪低落（反向：6-1=5）
            inputData.put("q13", 1);  // 容易感冒（反向：6-1=5）
            // 平和质得分 = 5 + 5 + 5 + 5 + 5 = 25

            RuleContext context = new RuleContext();
            context.setSceneCode(SCENE_CODE);
            context.setInputData(inputData);

            RuleResult result = ruleEngine.execute(context);

            assertTrue(result.isMatched());
            assertEquals("是", result.getOutputData().get("pinghe_result"));
        }

        /**
         * 创建基础体质评估数据（33题，默认低分）
         */
        private Map<String, Object> createBaseConstitutionData() {
            Map<String, Object> data = new HashMap<>();
            for (int i = 1; i <= 33; i++) {
                data.put("q" + i, 1);  // 默认所有题目低分
            }
            return data;
        }

        /**
         * 创建低分体质数据（所有偏颇体质得分<8）
         */
        private Map<String, Object> createLowScoreConstitutionData() {
            Map<String, Object> data = new HashMap<>();
            for (int i = 1; i <= 33; i++) {
                data.put("q" + i, 1);  // 默认最低分
            }
            return data;
        }
    }
}
