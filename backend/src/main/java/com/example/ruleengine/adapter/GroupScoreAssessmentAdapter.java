package com.example.ruleengine.adapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.ruleengine.engine.BusinessAdapter;
import com.example.ruleengine.engine.RuleResult;
import com.example.ruleengine.engine.SimpleActionHandler;
import com.example.ruleengine.entity.AssessmentGroupMapping;
import com.example.ruleengine.mapper.AssessmentGroupMappingMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 多维度分组计算适配器
 *
 * 适用于"多维度分组计算"类型的评估量表，如：
 * - 中老年体质评估（9种体质判定）
 * - 其他需要将题目分组计算后独立判定的量表
 *
 * 工作流程：
 * 1. 从数据库读取分组配置（题目与体质类型映射）
 * 2. 根据配置进行反向计分处理
 * 3. 计算各分组的得分总和
 * 4. 将各分组得分传递给规则引擎进行独立判定
 */
@Slf4j
@Component
public class GroupScoreAssessmentAdapter implements BusinessAdapter {

    public static final String SCENE_CODE = "CONSTITUTION_ASSESSMENT";

    /**
     * 题目字段匹配模式：q1, q2, ... qN
     */
    private static final Pattern QUESTION_PATTERN = Pattern.compile("^q(\\d+)$");

    /**
     * 反向计分最大值（6 - 原分值）
     */
    private static final int REVERSE_SCORE_BASE = 6;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private AssessmentGroupMappingMapper groupMappingMapper;

    @Override
    public String getSceneCode() {
        return SCENE_CODE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> preProcess(Object originalData) {
        Map<String, Object> processed = new HashMap<>();

        if (originalData == null) {
            return processed;
        }

        Map<String, Object> data;
        if (originalData instanceof Map) {
            data = (Map<String, Object>) originalData;
        } else {
            log.warn("不支持的数据类型: {}", originalData.getClass().getName());
            return processed;
        }

        // 从数据库获取分组配置
        List<AssessmentGroupMapping> groupMappings = groupMappingMapper.selectBySceneCode(SCENE_CODE);

        if (groupMappings == null || groupMappings.isEmpty()) {
            log.warn("未找到场景 {} 的分组配置", SCENE_CODE);
            return processed;
        }

        // 记录非主体质（如非平和质）的最高分
        int maxOtherScore = 0;
        String primaryGroupCode = "pinghe";  // 平和质作为主体质

        // 遍历各分组进行计算
        for (AssessmentGroupMapping mapping : groupMappings) {
            String groupCode = mapping.getGroupCode();
            List<Integer> itemList = parseJsonArray(mapping.getItemList());
            Set<Integer> reverseItems = parseJsonArrayToSet(mapping.getReverseItems());

            // 计算该分组的总分
            int groupScore = 0;
            for (Integer itemNum : itemList) {
                int qScore = getQuestionScore(data, itemNum);

                // 检查是否需要反向计分（如平和质中的部分题目）
                if (reverseItems.contains(itemNum)) {
                    qScore = REVERSE_SCORE_BASE - qScore;
                }
                groupScore += qScore;
            }

            // 使用 {groupCode}_score 格式存储分组得分
            processed.put(groupCode + "_score", groupScore);

            // 记录非主体质的最高分（用于复合条件判定）
            if (!primaryGroupCode.equals(groupCode)) {
                maxOtherScore = Math.max(maxOtherScore, groupScore);
            }
        }

        // 添加其他分组最高分（用于平和质的复合判定条件）
        processed.put("other_max_score", maxOtherScore);

        // 保留原始题目得分
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (QUESTION_PATTERN.matcher(entry.getKey()).matches()) {
                processed.put(entry.getKey(), entry.getValue());
            }
        }

        log.debug("体质评估数据预处理完成: {}", processed);
        return processed;
    }

    @Override
    public Object postProcess(RuleResult ruleResult) {
        Map<String, Object> response = new HashMap<>();

        response.put("matched", ruleResult.isMatched());
        response.put("matchedRules", ruleResult.getMatchedRuleCodes());

        if (ruleResult.isMatched()) {
            Map<String, Object> outputData = ruleResult.getOutputData();

            // 整合各体质判定结果
            Map<String, Object> constitutionResults = new LinkedHashMap<>();

            // 体质类型映射（编码 -> 中文名称）
            Map<String, String> constitutionNames = getConstitutionNames();

            // 提取各体质判定结果
            for (Map.Entry<String, String> entry : constitutionNames.entrySet()) {
                String code = entry.getKey();
                String name = entry.getValue();
                String resultKey = code + "_result";

                if (outputData.containsKey(resultKey)) {
                    Map<String, Object> constitutionDetail = new HashMap<>();
                    constitutionDetail.put("result", outputData.get(resultKey));

                    // 如果有得分信息也一并返回
                    String scoreKey = code + "_score";
                    if (outputData.containsKey(scoreKey)) {
                        constitutionDetail.put("score", outputData.get(scoreKey));
                    }

                    constitutionResults.put(name, constitutionDetail);
                }
            }

            response.put("constitutionResults", constitutionResults);

            // 主要体质类型（得分最高的偏颇体质）
            if (outputData.containsKey("primaryConstitution")) {
                response.put("primaryConstitution", outputData.get("primaryConstitution"));
            }

            // 健康指导建议
            if (outputData.containsKey("healthGuidance")) {
                response.put("healthGuidance", outputData.get("healthGuidance"));
            }
        }

        response.put("executionTime", ruleResult.getExecutionTime());

        log.debug("体质评估结果后处理完成: {}", response);
        return response;
    }

    @Override
    public Map<String, SimpleActionHandler> getActionHandlers() {
        Map<String, SimpleActionHandler> handlers = new HashMap<>();

        // 生成体质评估报告
        handlers.put("GENERATE_CONSTITUTION_REPORT", (actionType, params, context, result) -> {
            log.info("生成体质评估报告: {}", params);
            result.setOutput("report_generated", true);
            return true;
        });

        // 生成健康指导建议
        handlers.put("GENERATE_HEALTH_GUIDANCE", (actionType, params, context, result) -> {
            log.info("生成健康指导建议: {}", params);
            if (params != null) {
                List<String> guidance = new ArrayList<>();
                if (params.containsKey("emotional")) guidance.add("情志调摄");
                if (params.containsKey("diet")) guidance.add("饮食调养");
                if (params.containsKey("lifestyle")) guidance.add("起居调摄");
                if (params.containsKey("exercise")) guidance.add("运动保健");
                if (params.containsKey("acupoint")) guidance.add("穴位保健");
                result.setOutput("healthGuidance", guidance);
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

        // 检查是否存在 q{n} 格式的题目字段
        long questionCount = data.keySet().stream()
                .filter(key -> QUESTION_PATTERN.matcher(key).matches())
                .count();

        if (questionCount == 0) {
            return "评估数据中必须包含题目字段（如 q1, q2 等）";
        }

        // 体质评估需要33道题
        if (questionCount < 33) {
            log.warn("体质评估题目数量不足，期望33题，实际{}题", questionCount);
        }

        return null;
    }

    /**
     * 获取题目得分
     */
    private int getQuestionScore(Map<String, Object> data, int questionNum) {
        Object value = data.get("q" + questionNum);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }

    /**
     * 解析JSON数组字符串为List<Integer>
     */
    private List<Integer> parseJsonArray(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<List<Integer>>() {});
        } catch (Exception e) {
            log.error("解析JSON数组失败: {}", json, e);
            return Collections.emptyList();
        }
    }

    /**
     * 解析JSON数组字符串为Set<Integer>
     */
    private Set<Integer> parseJsonArrayToSet(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(parseJsonArray(json));
    }

    /**
     * 获取体质类型映射（编码 -> 中文名称）
     */
    private Map<String, String> getConstitutionNames() {
        Map<String, String> names = new LinkedHashMap<>();
        names.put("qixu", "气虚质");
        names.put("yangxu", "阳虚质");
        names.put("yinxu", "阴虚质");
        names.put("tanshi", "痰湿质");
        names.put("shire", "湿热质");
        names.put("xueyu", "血瘀质");
        names.put("qiyu", "气郁质");
        names.put("tebing", "特禀质");
        names.put("pinghe", "平和质");
        return names;
    }
}
