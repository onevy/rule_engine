package com.example.ruleengine.adapter;

import com.example.ruleengine.engine.BusinessAdapter;
import com.example.ruleengine.engine.RuleResult;
import com.example.ruleengine.engine.SimpleActionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 处方审核业务适配器
 *
 * 处理处方审核相关的规则执行，包括：
 * - 药品数据转换
 * - 患者信息处理
 * - 药物相互作用检查
 * - 剂量审核
 * - 禁忌症检查
 */
@Slf4j
@Component
public class PrescriptionAuditAdapter implements BusinessAdapter {

    public static final String SCENE_CODE = "PRESCRIPTION_AUDIT";

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

        // 转换患者信息
        if (data.containsKey("patient")) {
            Object patient = data.get("patient");
            if (patient instanceof Map) {
                Map<String, Object> patientMap = (Map<String, Object>) patient;
                result.put("patient_id", patientMap.get("id"));
                result.put("patient_name", patientMap.get("name"));
                result.put("patient_age", patientMap.get("age"));
                result.put("patient_gender", patientMap.get("gender"));
                result.put("patient_weight", patientMap.get("weight"));
                result.put("patient_height", patientMap.get("height"));

                // 肝肾功能指标
                result.put("liver_function", patientMap.get("liverFunction"));
                result.put("kidney_function", patientMap.get("kidneyFunction"));
                result.put("creatinine_clearance", patientMap.get("creatinineClearance"));

                // 过敏史
                if (patientMap.containsKey("allergies")) {
                    Object allergies = patientMap.get("allergies");
                    if (allergies instanceof List) {
                        result.put("patient_allergies", allergies);
                        result.put("allergy_count", ((List<?>) allergies).size());
                    }
                }

                // 特殊人群标识
                result.put("is_pregnant", patientMap.getOrDefault("isPregnant", false));
                result.put("is_lactating", patientMap.getOrDefault("isLactating", false));
                result.put("is_pediatric", patientMap.getOrDefault("isPediatric", false));
                result.put("is_elderly", patientMap.getOrDefault("isElderly", false));
            }
        }

        // 直接映射患者基本信息
        copyIfExists(data, result, "patient_age");
        copyIfExists(data, result, "patient_gender");
        copyIfExists(data, result, "patient_weight");

        // 转换处方药品列表
        if (data.containsKey("medications")) {
            Object medications = data.get("medications");
            if (medications instanceof List) {
                List<Map<String, Object>> medList = (List<Map<String, Object>>) medications;
                result.put("medication_count", medList.size());

                // 提取所有药品编码用于相互作用检查
                List<String> drugCodes = new ArrayList<>();
                List<String> drugNames = new ArrayList<>();
                double totalDailyDose = 0;

                for (int i = 0; i < medList.size(); i++) {
                    Map<String, Object> med = medList.get(i);
                    String prefix = "med_" + i + "_";

                    result.put(prefix + "code", med.get("drugCode"));
                    result.put(prefix + "name", med.get("drugName"));
                    result.put(prefix + "dose", med.get("dose"));
                    result.put(prefix + "dose_unit", med.get("doseUnit"));
                    result.put(prefix + "frequency", med.get("frequency"));
                    result.put(prefix + "route", med.get("route"));
                    result.put(prefix + "duration", med.get("duration"));
                    result.put(prefix + "quantity", med.get("quantity"));

                    if (med.get("drugCode") != null) {
                        drugCodes.add(med.get("drugCode").toString());
                    }
                    if (med.get("drugName") != null) {
                        drugNames.add(med.get("drugName").toString());
                    }

                    // 计算日剂量
                    if (med.get("dailyDose") != null) {
                        try {
                            totalDailyDose += Double.parseDouble(med.get("dailyDose").toString());
                        } catch (NumberFormatException e) {
                            // 忽略解析错误
                        }
                    }
                }

                result.put("drug_codes", drugCodes);
                result.put("drug_names", drugNames);
                result.put("total_daily_dose", totalDailyDose);
            }
        }

        // 单个药品信息（用于单药审核）
        if (data.containsKey("medication")) {
            Object medication = data.get("medication");
            if (medication instanceof Map) {
                Map<String, Object> medMap = (Map<String, Object>) medication;
                result.put("drug_code", medMap.get("drugCode"));
                result.put("drug_name", medMap.get("drugName"));
                result.put("dose", medMap.get("dose"));
                result.put("dose_unit", medMap.get("doseUnit"));
                result.put("frequency", medMap.get("frequency"));
                result.put("route", medMap.get("route"));
                result.put("max_single_dose", medMap.get("maxSingleDose"));
                result.put("max_daily_dose", medMap.get("maxDailyDose"));
            }
        }

        // 诊断信息
        if (data.containsKey("diagnoses")) {
            Object diagnoses = data.get("diagnoses");
            if (diagnoses instanceof List) {
                List<Map<String, Object>> diagList = (List<Map<String, Object>>) diagnoses;
                List<String> diagCodes = new ArrayList<>();
                List<String> diagNames = new ArrayList<>();

                for (Map<String, Object> diag : diagList) {
                    if (diag.get("code") != null) {
                        diagCodes.add(diag.get("code").toString());
                    }
                    if (diag.get("name") != null) {
                        diagNames.add(diag.get("name").toString());
                    }
                }

                result.put("diagnosis_codes", diagCodes);
                result.put("diagnosis_names", diagNames);
                result.put("diagnosis_count", diagList.size());
            }
        }

        // 直接映射的字段
        copyIfExists(data, result, "prescription_id");
        copyIfExists(data, result, "prescription_type");
        copyIfExists(data, result, "department");
        copyIfExists(data, result, "doctor");
        copyIfExists(data, result, "visit_type");

        log.debug("处方审核数据预处理完成: {}", result);
        return result;
    }

    @Override
    public Object postProcess(RuleResult ruleResult) {
        Map<String, Object> response = new HashMap<>();

        response.put("passed", !ruleResult.isMatched());  // 未命中规则表示审核通过
        response.put("matchedRules", ruleResult.getMatchedRuleCodes());

        if (ruleResult.isMatched()) {
            Map<String, Object> outputData = ruleResult.getOutputData();

            // 构建审核结果
            Map<String, Object> auditResult = new HashMap<>();
            auditResult.put("auditTime", LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            auditResult.put("auditStatus", outputData.getOrDefault("audit_status", "REJECTED"));
            auditResult.put("riskLevel", outputData.getOrDefault("risk_level", "MEDIUM"));

            // 问题列表
            List<Map<String, Object>> issues = new ArrayList<>();

            // 药物相互作用问题
            if (outputData.containsKey("interaction_warning")) {
                Map<String, Object> issue = new HashMap<>();
                issue.put("type", "DRUG_INTERACTION");
                issue.put("severity", outputData.getOrDefault("interaction_severity", "WARNING"));
                issue.put("message", outputData.get("interaction_warning"));
                issue.put("suggestion", outputData.get("interaction_suggestion"));
                issues.add(issue);
            }

            // 剂量问题
            if (outputData.containsKey("dose_warning")) {
                Map<String, Object> issue = new HashMap<>();
                issue.put("type", "DOSAGE_ISSUE");
                issue.put("severity", outputData.getOrDefault("dose_severity", "WARNING"));
                issue.put("message", outputData.get("dose_warning"));
                issue.put("suggestion", outputData.get("dose_suggestion"));
                issues.add(issue);
            }

            // 禁忌症问题
            if (outputData.containsKey("contraindication_warning")) {
                Map<String, Object> issue = new HashMap<>();
                issue.put("type", "CONTRAINDICATION");
                issue.put("severity", outputData.getOrDefault("contraindication_severity", "ERROR"));
                issue.put("message", outputData.get("contraindication_warning"));
                issue.put("suggestion", outputData.get("contraindication_suggestion"));
                issues.add(issue);
            }

            // 过敏问题
            if (outputData.containsKey("allergy_warning")) {
                Map<String, Object> issue = new HashMap<>();
                issue.put("type", "ALLERGY");
                issue.put("severity", "ERROR");
                issue.put("message", outputData.get("allergy_warning"));
                issue.put("suggestion", outputData.get("allergy_suggestion"));
                issues.add(issue);
            }

            // 通用警告
            if (outputData.containsKey("warning_message")) {
                Map<String, Object> issue = new HashMap<>();
                issue.put("type", "GENERAL_WARNING");
                issue.put("severity", "WARNING");
                issue.put("message", outputData.get("warning_message"));
                issues.add(issue);
            }

            auditResult.put("issues", issues);
            auditResult.put("issueCount", issues.size());

            // 建议调整后的剂量
            if (outputData.containsKey("adjusted_dose")) {
                auditResult.put("adjustedDose", outputData.get("adjusted_dose"));
            }

            // 替代药品建议
            if (outputData.containsKey("alternative_drugs")) {
                auditResult.put("alternativeDrugs", outputData.get("alternative_drugs"));
            }

            response.put("auditResult", auditResult);
        } else {
            // 审核通过
            Map<String, Object> auditResult = new HashMap<>();
            auditResult.put("auditTime", LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            auditResult.put("auditStatus", "APPROVED");
            auditResult.put("issues", Collections.emptyList());
            auditResult.put("issueCount", 0);
            response.put("auditResult", auditResult);
        }

        response.put("executionTime", ruleResult.getExecutionTime());

        log.debug("处方审核结果后处理完成: {}", response);
        return response;
    }

    @Override
    public Map<String, SimpleActionHandler> getActionHandlers() {
        Map<String, SimpleActionHandler> handlers = new HashMap<>();

        // 药物相互作用检查处理器
        handlers.put("CHECK_DRUG_INTERACTION", (actionType, params, context, result) -> {
            log.info("检查药物相互作用: {}", params);
            // 实际项目中这里会调用药物相互作用数据库
            result.setOutput("interaction_checked", true);
            result.setOutput("check_time", LocalDateTime.now().toString());
            return true;
        });

        // 剂量调整处理器
        handlers.put("ADJUST_DOSAGE", (actionType, params, context, result) -> {
            log.info("执行剂量调整: {}", params);
            // 实际项目中这里会根据患者参数计算合适剂量
            if (params != null && params.containsKey("adjusted_dose")) {
                result.setOutput("adjusted_dose", params.get("adjusted_dose"));
            }
            result.setOutput("dosage_adjusted", true);
            return true;
        });

        // 发送审核通知处理器
        handlers.put("SEND_AUDIT_NOTIFICATION", (actionType, params, context, result) -> {
            log.info("发送处方审核通知: {}", params);
            // 实际项目中这里会调用通知服务
            result.setOutput("notification_sent", true);
            return true;
        });

        // 记录审核日志处理器
        handlers.put("LOG_AUDIT_RESULT", (actionType, params, context, result) -> {
            log.info("记录审核日志: {}", params);
            // 实际项目中这里会调用日志服务
            result.setOutput("audit_logged", true);
            return true;
        });

        // 请求药师复核处理器
        handlers.put("REQUEST_PHARMACIST_REVIEW", (actionType, params, context, result) -> {
            log.info("请求药师复核: {}", params);
            // 实际项目中这里会发送复核请求
            result.setOutput("pharmacist_review_requested", true);
            return true;
        });

        return handlers;
    }

    @Override
    public String validate(Map<String, Object> data) {
        // 验证必要字段
        if (!data.containsKey("medications") && !data.containsKey("medication")) {
            return "药品信息不能为空";
        }

        // 验证患者信息
        if (!data.containsKey("patient") && !data.containsKey("patient_id")) {
            return "患者信息不能为空";
        }

        return null;
    }

    private void copyIfExists(Map<String, Object> source, Map<String, Object> target, String key) {
        if (source.containsKey(key)) {
            target.put(key, source.get(key));
        }
    }
}
