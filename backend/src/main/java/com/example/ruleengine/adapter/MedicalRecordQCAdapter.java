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
 * 病案质控业务适配器
 *
 * 处理病案质控相关的规则执行，包括：
 * - 病案完整性检查
 * - 诊断编码准确性
 * - 手术操作验证
 * - 费用比例分析
 * - 时间线一致性检查
 */
@Slf4j
@Component
public class MedicalRecordQCAdapter implements BusinessAdapter {

    public static final String SCENE_CODE = "MEDICAL_RECORD_QC";

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

        // 基本信息
        copyIfExists(data, result, "record_id");
        copyIfExists(data, result, "patient_id");
        copyIfExists(data, result, "admission_number");
        copyIfExists(data, result, "department");
        copyIfExists(data, result, "ward");

        // 患者信息
        if (data.containsKey("patient")) {
            Object patient = data.get("patient");
            if (patient instanceof Map) {
                Map<String, Object> patientMap = (Map<String, Object>) patient;
                result.put("patient_name", patientMap.get("name"));
                result.put("patient_age", patientMap.get("age"));
                result.put("patient_gender", patientMap.get("gender"));
                result.put("patient_id_number", patientMap.get("idNumber"));
            }
        }

        // 入出院信息
        copyIfExists(data, result, "admission_date");
        copyIfExists(data, result, "discharge_date");
        copyIfExists(data, result, "length_of_stay");
        copyIfExists(data, result, "admission_type");
        copyIfExists(data, result, "discharge_status");

        // 诊断信息
        if (data.containsKey("diagnoses")) {
            Object diagnoses = data.get("diagnoses");
            if (diagnoses instanceof List) {
                List<Map<String, Object>> diagList = (List<Map<String, Object>>) diagnoses;
                result.put("diagnosis_count", diagList.size());

                List<String> diagCodes = new ArrayList<>();
                List<String> diagNames = new ArrayList<>();
                String mainDiagnosis = null;
                int mainDiagnosisCount = 0;

                for (Map<String, Object> diag : diagList) {
                    String code = diag.get("code") != null ? diag.get("code").toString() : null;
                    String name = diag.get("name") != null ? diag.get("name").toString() : null;

                    if (code != null) {
                        diagCodes.add(code);
                    }
                    if (name != null) {
                        diagNames.add(name);
                    }

                    // 主诊断
                    if (Boolean.TRUE.equals(diag.get("isMain"))) {
                        mainDiagnosis = code;
                        mainDiagnosisCount++;
                    }
                }

                result.put("diagnosis_codes", diagCodes);
                result.put("diagnosis_names", diagNames);
                result.put("main_diagnosis", mainDiagnosis);
                result.put("main_diagnosis_count", mainDiagnosisCount);
                result.put("has_main_diagnosis", mainDiagnosis != null);
            }
        }

        // 手术/操作信息
        if (data.containsKey("procedures")) {
            Object procedures = data.get("procedures");
            if (procedures instanceof List) {
                List<Map<String, Object>> procList = (List<Map<String, Object>>) procedures;
                result.put("procedure_count", procList.size());

                List<String> procCodes = new ArrayList<>();
                List<String> procNames = new ArrayList<>();
                String mainProcedure = null;

                for (Map<String, Object> proc : procList) {
                    String code = proc.get("code") != null ? proc.get("code").toString() : null;
                    String name = proc.get("name") != null ? proc.get("name").toString() : null;

                    if (code != null) {
                        procCodes.add(code);
                    }
                    if (name != null) {
                        procNames.add(name);
                    }

                    if (Boolean.TRUE.equals(proc.get("isMain"))) {
                        mainProcedure = code;
                    }
                }

                result.put("procedure_codes", procCodes);
                result.put("procedure_names", procNames);
                result.put("main_procedure", mainProcedure);
                result.put("has_surgery", !procList.isEmpty());
            }
        }

        // 费用信息
        if (data.containsKey("costs")) {
            Object costs = data.get("costs");
            if (costs instanceof Map) {
                Map<String, Object> costMap = (Map<String, Object>) costs;

                double totalCost = parseDouble(costMap.get("total"));
                double drugCost = parseDouble(costMap.get("drug"));
                double materialCost = parseDouble(costMap.get("material"));
                double examCost = parseDouble(costMap.get("examination"));
                double labCost = parseDouble(costMap.get("laboratory"));
                double treatmentCost = parseDouble(costMap.get("treatment"));
                double surgeryCost = parseDouble(costMap.get("surgery"));
                double nursingCost = parseDouble(costMap.get("nursing"));
                double bedCost = parseDouble(costMap.get("bed"));

                result.put("total_cost", totalCost);
                result.put("drug_cost", drugCost);
                result.put("material_cost", materialCost);
                result.put("exam_cost", examCost);
                result.put("lab_cost", labCost);
                result.put("treatment_cost", treatmentCost);
                result.put("surgery_cost", surgeryCost);
                result.put("nursing_cost", nursingCost);
                result.put("bed_cost", bedCost);

                // 计算费用比例
                if (totalCost > 0) {
                    result.put("drug_ratio", drugCost / totalCost * 100);
                    result.put("material_ratio", materialCost / totalCost * 100);
                    result.put("exam_ratio", examCost / totalCost * 100);
                    result.put("lab_ratio", labCost / totalCost * 100);
                }
            }
        }

        // 直接映射费用比例
        copyIfExists(data, result, "drug_ratio");
        copyIfExists(data, result, "material_ratio");
        copyIfExists(data, result, "total_cost");

        // 病案首页完整性字段
        copyIfExists(data, result, "has_chief_complaint");
        copyIfExists(data, result, "has_present_illness");
        copyIfExists(data, result, "has_past_history");
        copyIfExists(data, result, "has_physical_exam");
        copyIfExists(data, result, "has_discharge_summary");
        copyIfExists(data, result, "has_attending_signature");
        copyIfExists(data, result, "has_director_signature");

        // DRG/DIP相关
        copyIfExists(data, result, "drg_code");
        copyIfExists(data, result, "drg_name");
        copyIfExists(data, result, "drg_weight");
        copyIfExists(data, result, "expected_cost");

        // 质控状态
        copyIfExists(data, result, "qc_status");
        copyIfExists(data, result, "qc_score");

        log.debug("病案质控数据预处理完成: {}", result);
        return result;
    }

    @Override
    public Object postProcess(RuleResult ruleResult) {
        Map<String, Object> response = new HashMap<>();

        response.put("hasIssues", ruleResult.isMatched());
        response.put("matchedRules", ruleResult.getMatchedRuleCodes());

        // 构建质控结果
        Map<String, Object> qcResult = new HashMap<>();
        qcResult.put("qcTime", LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        if (ruleResult.isMatched()) {
            Map<String, Object> outputData = ruleResult.getOutputData();

            // 质控问题列表
            List<Map<String, Object>> issues = new ArrayList<>();

            // 完整性问题
            if (outputData.containsKey("completeness_issue")) {
                Map<String, Object> issue = new HashMap<>();
                issue.put("type", "COMPLETENESS");
                issue.put("category", "病案完整性");
                issue.put("severity", outputData.getOrDefault("completeness_severity", "WARNING"));
                issue.put("message", outputData.get("completeness_issue"));
                issue.put("field", outputData.get("missing_field"));
                issue.put("suggestion", outputData.get("completeness_suggestion"));
                issues.add(issue);
            }

            // 编码问题
            if (outputData.containsKey("coding_issue")) {
                Map<String, Object> issue = new HashMap<>();
                issue.put("type", "CODING");
                issue.put("category", "编码质量");
                issue.put("severity", outputData.getOrDefault("coding_severity", "ERROR"));
                issue.put("message", outputData.get("coding_issue"));
                issue.put("suggestion", outputData.get("coding_suggestion"));
                issues.add(issue);
            }

            // 费用问题
            if (outputData.containsKey("cost_issue")) {
                Map<String, Object> issue = new HashMap<>();
                issue.put("type", "COST");
                issue.put("category", "费用合理性");
                issue.put("severity", outputData.getOrDefault("cost_severity", "WARNING"));
                issue.put("message", outputData.get("cost_issue"));
                issue.put("actualValue", outputData.get("actual_ratio"));
                issue.put("threshold", outputData.get("ratio_threshold"));
                issue.put("suggestion", outputData.get("cost_suggestion"));
                issues.add(issue);
            }

            // 时间线问题
            if (outputData.containsKey("timeline_issue")) {
                Map<String, Object> issue = new HashMap<>();
                issue.put("type", "TIMELINE");
                issue.put("category", "时间一致性");
                issue.put("severity", outputData.getOrDefault("timeline_severity", "ERROR"));
                issue.put("message", outputData.get("timeline_issue"));
                issue.put("suggestion", outputData.get("timeline_suggestion"));
                issues.add(issue);
            }

            // 逻辑一致性问题
            if (outputData.containsKey("logic_issue")) {
                Map<String, Object> issue = new HashMap<>();
                issue.put("type", "LOGIC");
                issue.put("category", "逻辑一致性");
                issue.put("severity", outputData.getOrDefault("logic_severity", "WARNING"));
                issue.put("message", outputData.get("logic_issue"));
                issue.put("suggestion", outputData.get("logic_suggestion"));
                issues.add(issue);
            }

            // 通用问题
            if (outputData.containsKey("qc_message")) {
                Map<String, Object> issue = new HashMap<>();
                issue.put("type", "GENERAL");
                issue.put("category", "一般问题");
                issue.put("severity", "INFO");
                issue.put("message", outputData.get("qc_message"));
                issues.add(issue);
            }

            qcResult.put("issues", issues);
            qcResult.put("issueCount", issues.size());

            // 计算质控得分（每个问题扣分）
            int baseScore = 100;
            for (Map<String, Object> issue : issues) {
                String severity = issue.get("severity").toString();
                if ("ERROR".equals(severity)) {
                    baseScore -= 10;
                } else if ("WARNING".equals(severity)) {
                    baseScore -= 5;
                } else {
                    baseScore -= 2;
                }
            }
            qcResult.put("score", Math.max(0, baseScore));
            qcResult.put("passed", baseScore >= 60);

            // 建议的DRG编码
            if (outputData.containsKey("suggested_drg")) {
                qcResult.put("suggestedDrg", outputData.get("suggested_drg"));
            }

        } else {
            // 无质控问题
            qcResult.put("issues", Collections.emptyList());
            qcResult.put("issueCount", 0);
            qcResult.put("score", 100);
            qcResult.put("passed", true);
        }

        response.put("qcResult", qcResult);
        response.put("executionTime", ruleResult.getExecutionTime());

        log.debug("病案质控结果后处理完成: {}", response);
        return response;
    }

    @Override
    public Map<String, SimpleActionHandler> getActionHandlers() {
        Map<String, SimpleActionHandler> handlers = new HashMap<>();

        // 生成质控报告处理器
        handlers.put("GENERATE_QC_REPORT", (actionType, params, context, result) -> {
            log.info("生成病案质控报告: {}", params);
            // 实际项目中这里会生成PDF或其他格式的报告
            result.setOutput("report_generated", true);
            result.setOutput("report_time", LocalDateTime.now().toString());
            return true;
        });

        // 标记需要复审处理器
        handlers.put("MARK_FOR_REVIEW", (actionType, params, context, result) -> {
            log.info("标记病案需要复审: {}", params);
            // 实际项目中这里会更新病案状态
            result.setOutput("marked_for_review", true);
            return true;
        });

        // 发送质控通知处理器
        handlers.put("SEND_QC_NOTIFICATION", (actionType, params, context, result) -> {
            log.info("发送质控通知: {}", params);
            // 实际项目中这里会调用通知服务
            result.setOutput("notification_sent", true);
            return true;
        });

        // 更新DRG编码建议处理器
        handlers.put("SUGGEST_DRG_CODE", (actionType, params, context, result) -> {
            log.info("建议DRG编码: {}", params);
            if (params != null && params.containsKey("drg_code")) {
                result.setOutput("suggested_drg", params.get("drg_code"));
            }
            return true;
        });

        // 记录质控历史处理器
        handlers.put("LOG_QC_HISTORY", (actionType, params, context, result) -> {
            log.info("记录质控历史: {}", params);
            // 实际项目中这里会保存质控历史记录
            result.setOutput("history_logged", true);
            return true;
        });

        // 计算质控得分处理器
        handlers.put("CALCULATE_QC_SCORE", (actionType, params, context, result) -> {
            log.info("计算质控得分: {}", params);
            // 可以在这里实现复杂的评分逻辑
            result.setOutput("score_calculated", true);
            return true;
        });

        return handlers;
    }

    @Override
    public String validate(Map<String, Object> data) {
        // 验证必要字段
        if (!data.containsKey("record_id") && !data.containsKey("admission_number")) {
            return "病案标识不能为空";
        }

        // 验证诊断信息
        if (!data.containsKey("diagnoses")) {
            return "诊断信息不能为空";
        }

        return null;
    }

    private void copyIfExists(Map<String, Object> source, Map<String, Object> target, String key) {
        if (source.containsKey(key)) {
            target.put(key, source.get(key));
        }
    }

    private double parseDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
