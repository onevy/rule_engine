package com.example.ruleengine.adapter;

import com.example.ruleengine.engine.BusinessAdapter;
import com.example.ruleengine.engine.RuleResult;
import com.example.ruleengine.engine.SimpleActionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 传染病报卡业务适配器
 *
 * 处理传染病报卡相关的规则执行，包括：
 * - 诊断数据转换
 * - 检验数据转换
 * - 报卡生成逻辑
 */
@Slf4j
@Component
public class InfectiousDiseaseAdapter implements BusinessAdapter {

    public static final String SCENE_CODE = "INFECTIOUS_DISEASE_REPORT";

    @Override
    public String getSceneCode() {
        return SCENE_CODE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> preProcess(Object originalData) {
        Map<String, Object> result = new HashMap<>();

        if (originalData == null) {
            return result;
        }

        Map<String, Object> data;
        if (originalData instanceof Map) {
            data = (Map<String, Object>) originalData;
        } else {
            log.warn("不支持的数据类型: {}", originalData.getClass().getName());
            return result;
        }

        // 转换诊断数据
        if (data.containsKey("diagnosis")) {
            Object diagnosis = data.get("diagnosis");
            if (diagnosis instanceof Map) {
                Map<String, Object> diagnosisMap = (Map<String, Object>) diagnosis;
                result.put("diagnosis_code", diagnosisMap.get("code"));
                result.put("diagnosis_name", diagnosisMap.get("name"));
                result.put("diagnosis_type", diagnosisMap.get("type"));
            }
        }

        // 直接映射诊断编码和名称
        if (data.containsKey("diagnosis_code")) {
            result.put("diagnosis_code", data.get("diagnosis_code"));
        }
        if (data.containsKey("diagnosis_name")) {
            result.put("diagnosis_name", data.get("diagnosis_name"));
        }

        // 转换检验数据
        if (data.containsKey("lab_results")) {
            Object labResults = data.get("lab_results");
            if (labResults instanceof Map) {
                Map<String, Object> labMap = (Map<String, Object>) labResults;
                for (Map.Entry<String, Object> entry : labMap.entrySet()) {
                    result.put("lab_" + entry.getKey(), entry.getValue());
                }
            }
        }

        // 转换检查数据
        if (data.containsKey("exam_results")) {
            Object examResults = data.get("exam_results");
            if (examResults instanceof Map) {
                Map<String, Object> examMap = (Map<String, Object>) examResults;
                for (Map.Entry<String, Object> entry : examMap.entrySet()) {
                    result.put("exam_" + entry.getKey(), entry.getValue());
                }
            }
        }

        // 转换患者信息
        if (data.containsKey("patient")) {
            Object patient = data.get("patient");
            if (patient instanceof Map) {
                Map<String, Object> patientMap = (Map<String, Object>) patient;
                result.put("patient_age", patientMap.get("age"));
                result.put("patient_gender", patientMap.get("gender"));
                result.put("patient_id", patientMap.get("id"));
                result.put("patient_name", patientMap.get("name"));
            }
        }

        // 直接映射的字段
        copyIfExists(data, result, "patient_age");
        copyIfExists(data, result, "patient_gender");
        copyIfExists(data, result, "visit_type");
        copyIfExists(data, result, "department");
        copyIfExists(data, result, "doctor");

        log.debug("传染病报卡数据预处理完成: {}", result);
        return result;
    }

    @Override
    public Object postProcess(RuleResult ruleResult) {
        Map<String, Object> response = new HashMap<>();

        response.put("needReport", ruleResult.isMatched());
        response.put("matchedRules", ruleResult.getMatchedRuleCodes());

        if (ruleResult.isMatched()) {
            Map<String, Object> outputData = ruleResult.getOutputData();

            // 构建报卡信息
            Map<String, Object> reportCard = new HashMap<>();
            reportCard.put("diseaseCode", outputData.get("disease_code"));
            reportCard.put("diseaseName", outputData.get("disease_name"));
            reportCard.put("diseaseLevel", outputData.get("disease_level"));
            reportCard.put("reportType", outputData.get("report_type"));
            reportCard.put("reportTime", LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            reportCard.put("needReport", outputData.getOrDefault("need_report", true));

            response.put("reportCard", reportCard);

            // 提示信息
            if (outputData.containsKey("message")) {
                response.put("message", outputData.get("message"));
            }
        }

        response.put("executionTime", ruleResult.getExecutionTime());

        log.debug("传染病报卡结果后处理完成: {}", response);
        return response;
    }

    @Override
    public Map<String, SimpleActionHandler> getActionHandlers() {
        Map<String, SimpleActionHandler> handlers = new HashMap<>();

        // 生成报卡动作处理器
        handlers.put("GENERATE_REPORT_CARD", (actionType, params, context, result) -> {
            log.info("生成传染病报卡: {}", params);
            // 实际项目中这里会调用报卡服务生成报卡
            result.setOutput("report_generated", true);
            result.setOutput("report_time", LocalDateTime.now().toString());
            return true;
        });

        // 发送通知动作处理器
        handlers.put("SEND_NOTIFICATION", (actionType, params, context, result) -> {
            log.info("发送传染病报告通知: {}", params);
            // 实际项目中这里会调用通知服务
            result.setOutput("notification_sent", true);
            return true;
        });

        return handlers;
    }

    @Override
    public String validate(Map<String, Object> data) {
        // 验证必要字段
        if (!data.containsKey("diagnosis_code") && !data.containsKey("diagnosis")) {
            return "诊断信息不能为空";
        }
        return null;
    }

    private void copyIfExists(Map<String, Object> source, Map<String, Object> target, String key) {
        if (source.containsKey(key)) {
            target.put(key, source.get(key));
        }
    }
}
