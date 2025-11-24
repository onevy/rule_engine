package com.example.ruleengine.adapter;

import com.example.ruleengine.engine.BusinessAdapter;
import com.example.ruleengine.engine.RuleResult;
import com.example.ruleengine.engine.SimpleActionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 总分区间判定适配器
 *
 * 适用于"总分区间判定"类型的评估量表，如：
 * - 老年人生活自理能力评估
 * - 其他需要汇总所有题目得分后进行区间判定的量表
 *
 * 工作流程：
 * 1. 识别所有 q{n} 格式的题目字段
 * 2. 计算所有题目得分的总和
 * 3. 将总分传递给规则引擎进行区间判定
 */
@Slf4j
@Component
public class TotalScoreAssessmentAdapter implements BusinessAdapter {

    public static final String SCENE_CODE = "SELF_CARE_ASSESSMENT";

    /**
     * 题目字段匹配模式：q1, q2, ... qN
     */
    private static final Pattern QUESTION_PATTERN = Pattern.compile("^q(\\d+)$");

    @Override
    public String getSceneCode() {
        return SCENE_CODE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> preProcess(Object originalData) {
        Map<String, Object> processed = new HashMap<>();

        if (originalData == null) {
            processed.put("totalScore", 0);
            processed.put("itemCount", 0);
            return processed;
        }

        Map<String, Object> data;
        if (originalData instanceof Map) {
            data = (Map<String, Object>) originalData;
        } else {
            log.warn("不支持的数据类型: {}", originalData.getClass().getName());
            processed.put("totalScore", 0);
            processed.put("itemCount", 0);
            return processed;
        }

        // 动态识别并累加所有 q{n} 字段
        int totalScore = 0;
        int itemCount = 0;
        int maxQuestionNum = 0;

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Matcher matcher = QUESTION_PATTERN.matcher(key);

            if (matcher.matches()) {
                int questionNum = Integer.parseInt(matcher.group(1));
                maxQuestionNum = Math.max(maxQuestionNum, questionNum);

                if (entry.getValue() instanceof Number) {
                    int score = ((Number) entry.getValue()).intValue();
                    totalScore += score;
                    itemCount++;
                    // 保留原始题目得分，用于结果追溯
                    processed.put(key, score);
                }
            }
        }

        // 输出计算结果
        processed.put("totalScore", totalScore);
        processed.put("itemCount", itemCount);
        processed.put("maxQuestionNum", maxQuestionNum);

        log.debug("总分评估数据预处理完成: totalScore={}, itemCount={}", totalScore, itemCount);
        return processed;
    }

    @Override
    public Object postProcess(RuleResult ruleResult) {
        Map<String, Object> response = new HashMap<>();

        response.put("matched", ruleResult.isMatched());
        response.put("matchedRules", ruleResult.getMatchedRuleCodes());

        if (ruleResult.isMatched()) {
            Map<String, Object> outputData = ruleResult.getOutputData();

            // 提取评估结果
            response.put("assessmentResult", outputData.get("assessmentResult"));
            response.put("level", outputData.get("level"));
            response.put("totalScore", outputData.get("totalScore"));

            // 健康建议
            if (outputData.containsKey("suggestion")) {
                response.put("suggestion", outputData.get("suggestion"));
            }

            // 其他输出字段
            if (outputData.containsKey("message")) {
                response.put("message", outputData.get("message"));
            }
        }

        response.put("executionTime", ruleResult.getExecutionTime());

        log.debug("总分评估结果后处理完成: {}", response);
        return response;
    }

    @Override
    public Map<String, SimpleActionHandler> getActionHandlers() {
        Map<String, SimpleActionHandler> handlers = new HashMap<>();

        // 生成评估报告动作处理器
        handlers.put("GENERATE_ASSESSMENT_REPORT", (actionType, params, context, result) -> {
            log.info("生成自理能力评估报告: {}", params);
            result.setOutput("report_generated", true);
            return true;
        });

        // 健康建议动作处理器
        handlers.put("PROVIDE_HEALTH_SUGGESTION", (actionType, params, context, result) -> {
            log.info("生成健康建议: {}", params);
            if (params != null && params.containsKey("suggestion")) {
                result.setOutput("suggestion", params.get("suggestion"));
            }
            return true;
        });

        return handlers;
    }

    @Override
    public String validate(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return "评估数据不能为空";
        }

        // 检查是否存在至少一个 q{n} 格式的题目字段
        boolean hasQuestion = data.keySet().stream()
                .anyMatch(key -> QUESTION_PATTERN.matcher(key).matches());

        if (!hasQuestion) {
            return "评估数据中必须包含至少一个题目字段（如 q1, q2 等）";
        }

        return null;
    }
}
