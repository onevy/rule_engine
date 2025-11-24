-- =====================================================
-- 规则引擎初始化数据脚本
-- =====================================================

USE `rule_engine`;

-- =====================================================
-- 1. 初始化业务场景数据
-- =====================================================
INSERT INTO `business_scene` (`scene_code`, `scene_name`, `scene_desc`, `adapter_class`, `status`, `sort_order`, `create_by`) VALUES
('INFECTIOUS_DISEASE', '传染病上报', '传染病预警和强制上报规则', 'com.example.ruleengine.adapter.InfectiousDiseaseAdapter', 1, 1, 'system'),
('PRESCRIPTION_AUDIT', '处方审核', '处方合理性审核规则', 'com.example.ruleengine.adapter.PrescriptionAuditAdapter', 1, 2, 'system'),
('MEDICAL_RECORD_QC', '病历质控', '病历质量控制规则', 'com.example.ruleengine.adapter.MedicalRecordQCAdapter', 1, 3, 'system');

-- =====================================================
-- 2. 初始化字段元数据 - 传染病上报场景
-- =====================================================
INSERT INTO `field_metadata` (`scene_code`, `field_code`, `field_name`, `field_type`, `data_source`, `supported_operators`, `value_range`, `category`, `sort_order`, `create_by`) VALUES
-- 诊断信息
('INFECTIOUS_DISEASE', 'diagnosis_code', '诊断编码', 'String', '诊断数据', 'EQ,IN,NOT_IN,LEFT_MATCH', 'ICD-10编码', '诊断信息', 1, 'system'),
('INFECTIOUS_DISEASE', 'diagnosis_name', '诊断名称', 'String', '诊断数据', 'IN,NOT_IN,LEFT_MATCH,LIKE', '诊断名称', '诊断信息', 2, 'system'),
('INFECTIOUS_DISEASE', 'diagnosis_type', '诊断类型', 'String', '诊断数据', 'EQ,IN', '主诊断/次诊断', '诊断信息', 3, 'system'),
-- 检验信息
('INFECTIOUS_DISEASE', 'lab_hiv_antibody', 'HIV抗体', 'String', '检验报告', 'EQ,IN', '阴性/阳性', '检验信息', 10, 'system'),
('INFECTIOUS_DISEASE', 'lab_hbsag', '乙肝表面抗原', 'String', '检验报告', 'EQ,IN', '阴性/阳性', '检验信息', 11, 'system'),
('INFECTIOUS_DISEASE', 'lab_syphilis', '梅毒抗体', 'String', '检验报告', 'EQ,IN', '阴性/阳性', '检验信息', 12, 'system'),
('INFECTIOUS_DISEASE', 'lab_tb', '结核菌检测', 'String', '检验报告', 'EQ,IN', '阴性/阳性', '检验信息', 13, 'system'),
('INFECTIOUS_DISEASE', 'lab_wbc', '白细胞计数', 'Number', '检验报告', 'GT,GTE,LT,LTE,BETWEEN,EQ', '0-100', '检验信息', 14, 'system'),
-- 患者信息
('INFECTIOUS_DISEASE', 'patient_age', '患者年龄', 'Number', '患者信息', 'GT,GTE,LT,LTE,BETWEEN,EQ', '0-150', '患者信息', 20, 'system'),
('INFECTIOUS_DISEASE', 'patient_gender', '患者性别', 'String', '患者信息', 'EQ,IN', '男/女', '患者信息', 21, 'system'),
('INFECTIOUS_DISEASE', 'admission_date', '入院日期', 'Date', '住院数据', 'GT,GTE,LT,LTE,BETWEEN', '日期', '住院信息', 30, 'system');

-- =====================================================
-- 3. 初始化字段元数据 - 处方审核场景
-- =====================================================
INSERT INTO `field_metadata` (`scene_code`, `field_code`, `field_name`, `field_type`, `data_source`, `supported_operators`, `value_range`, `category`, `sort_order`, `create_by`) VALUES
-- 药品信息
('PRESCRIPTION_AUDIT', 'drug_code', '药品编码', 'String', '处方数据', 'EQ,IN,NOT_IN', '药品编码', '药品信息', 1, 'system'),
('PRESCRIPTION_AUDIT', 'drug_name', '药品名称', 'String', '处方数据', 'IN,NOT_IN,LEFT_MATCH,LIKE', '药品名称', '药品信息', 2, 'system'),
('PRESCRIPTION_AUDIT', 'drug_dosage', '药品剂量', 'Number', '处方数据', 'GT,GTE,LT,LTE,EQ,BETWEEN', '数值', '药品信息', 3, 'system'),
('PRESCRIPTION_AUDIT', 'drug_frequency', '用药频次', 'String', '处方数据', 'EQ,IN', 'qd/bid/tid/qid', '药品信息', 4, 'system'),
('PRESCRIPTION_AUDIT', 'drug_days', '用药天数', 'Number', '处方数据', 'GT,GTE,LT,LTE,EQ', '数值', '药品信息', 5, 'system'),
-- 患者信息
('PRESCRIPTION_AUDIT', 'patient_age', '患者年龄', 'Number', '患者信息', 'GT,GTE,LT,LTE,BETWEEN,EQ', '0-150', '患者信息', 10, 'system'),
('PRESCRIPTION_AUDIT', 'patient_gender', '患者性别', 'String', '患者信息', 'EQ,IN', '男/女', '患者信息', 11, 'system'),
('PRESCRIPTION_AUDIT', 'patient_weight', '患者体重', 'Number', '患者信息', 'GT,GTE,LT,LTE,BETWEEN', '0-300', '患者信息', 12, 'system'),
('PRESCRIPTION_AUDIT', 'is_pregnant', '是否妊娠', 'Boolean', '患者信息', 'EQ', 'true/false', '患者信息', 13, 'system'),
('PRESCRIPTION_AUDIT', 'allergy_drugs', '过敏药物', 'String', '患者信息', 'IN,NOT_IN', '药品列表', '患者信息', 14, 'system'),
-- 诊断信息
('PRESCRIPTION_AUDIT', 'diagnosis_code', '诊断编码', 'String', '诊断数据', 'EQ,IN,NOT_IN', 'ICD-10编码', '诊断信息', 20, 'system');

-- =====================================================
-- 4. 初始化字段元数据 - 病历质控场景
-- =====================================================
INSERT INTO `field_metadata` (`scene_code`, `field_code`, `field_name`, `field_type`, `data_source`, `supported_operators`, `value_range`, `category`, `sort_order`, `create_by`) VALUES
-- 病历信息
('MEDICAL_RECORD_QC', 'main_diagnosis', '主诊断', 'String', '病历数据', 'EQ,IN,NOT_IN,IS_NULL,IS_NOT_NULL', '诊断名称', '病历信息', 1, 'system'),
('MEDICAL_RECORD_QC', 'surgery_name', '手术名称', 'String', '病历数据', 'IN,NOT_IN,IS_NULL,IS_NOT_NULL', '手术名称', '病历信息', 2, 'system'),
('MEDICAL_RECORD_QC', 'admission_days', '住院天数', 'Number', '病历数据', 'GT,GTE,LT,LTE,BETWEEN', '数值', '病历信息', 3, 'system'),
-- 费用信息
('MEDICAL_RECORD_QC', 'total_cost', '总费用', 'Number', '费用数据', 'GT,GTE,LT,LTE,BETWEEN', '数值', '费用信息', 10, 'system'),
('MEDICAL_RECORD_QC', 'drug_cost', '药品费用', 'Number', '费用数据', 'GT,GTE,LT,LTE,BETWEEN', '数值', '费用信息', 11, 'system'),
('MEDICAL_RECORD_QC', 'drug_ratio', '药占比', 'Number', '费用数据', 'GT,GTE,LT,LTE,BETWEEN', '0-1', '费用信息', 12, 'system'),
('MEDICAL_RECORD_QC', 'consumable_ratio', '耗材占比', 'Number', '费用数据', 'GT,GTE,LT,LTE,BETWEEN', '0-1', '费用信息', 13, 'system');

-- =====================================================
-- 5. 初始化示例规则组
-- =====================================================
INSERT INTO `rule_group` (`scene_code`, `group_code`, `group_name`, `group_desc`, `parent_id`, `execution_mode`, `sort_order`, `create_by`) VALUES
('INFECTIOUS_DISEASE', 'ID_CLASS_A', '甲类传染病', '甲类传染病规则组', 0, 'ALL', 1, 'system'),
('INFECTIOUS_DISEASE', 'ID_CLASS_B', '乙类传染病', '乙类传染病规则组', 0, 'ALL', 2, 'system'),
('INFECTIOUS_DISEASE', 'ID_CLASS_C', '丙类传染病', '丙类传染病规则组', 0, 'ALL', 3, 'system'),
('PRESCRIPTION_AUDIT', 'PA_DOSAGE', '剂量审核', '药品剂量审核规则组', 0, 'ALL', 1, 'system'),
('PRESCRIPTION_AUDIT', 'PA_INTERACTION', '相互作用', '药物相互作用审核规则组', 0, 'ALL', 2, 'system'),
('MEDICAL_RECORD_QC', 'MR_COMPLETENESS', '完整性检查', '病历完整性检查规则组', 0, 'ALL', 1, 'system'),
('MEDICAL_RECORD_QC', 'MR_COST', '费用检查', '费用合理性检查规则组', 0, 'ALL', 2, 'system');

-- =====================================================
-- 6. 初始化示例规则定义 - 传染病上报场景
-- =====================================================
INSERT INTO `rule_definition` (`id`, `rule_code`, `rule_name`, `rule_desc`, `scene_code`, `group_id`, `rule_type`, `priority`, `status`, `effective_start_time`, `effective_end_time`, `version`, `create_by`) VALUES
-- 甲类传染病规则 (group_id = 1)
(1, 'ID_HIV_REPORT', 'HIV/AIDS上报规则', '当检验报告HIV抗体阳性时，触发传染病上报', 'INFECTIOUS_DISEASE', 1, 'CONDITION', 100, 1, '2024-01-01 00:00:00', '2099-12-31 23:59:59', 1, 'system'),
-- 乙类传染病规则 (group_id = 2)
(2, 'ID_TB_REPORT', '肺结核上报规则', '当诊断为肺结核或结核菌检测阳性时，触发传染病上报', 'INFECTIOUS_DISEASE', 2, 'CONDITION', 90, 1, '2024-01-01 00:00:00', '2099-12-31 23:59:59', 1, 'system'),
(3, 'ID_SYPHILIS_REPORT', '梅毒上报规则', '当梅毒抗体检测阳性时，触发传染病上报', 'INFECTIOUS_DISEASE', 2, 'CONDITION', 85, 1, '2024-01-01 00:00:00', '2099-12-31 23:59:59', 1, 'system'),
(4, 'ID_HEPATITIS_B_REPORT', '乙型肝炎上报规则', '当乙肝表面抗原阳性且白细胞异常时，触发传染病上报', 'INFECTIOUS_DISEASE', 2, 'CONDITION', 80, 1, '2024-01-01 00:00:00', '2099-12-31 23:59:59', 1, 'system'),
-- 丙类传染病规则 (group_id = 3)
(5, 'ID_HFMD_REPORT', '手足口病上报规则', '当诊断为手足口病且患者年龄小于5岁时，触发传染病上报', 'INFECTIOUS_DISEASE', 3, 'CONDITION', 70, 1, '2024-01-01 00:00:00', '2099-12-31 23:59:59', 1, 'system');

-- =====================================================
-- 7. 初始化示例规则定义 - 处方审核场景
-- =====================================================
INSERT INTO `rule_definition` (`id`, `rule_code`, `rule_name`, `rule_desc`, `scene_code`, `group_id`, `rule_type`, `priority`, `status`, `effective_start_time`, `effective_end_time`, `version`, `create_by`) VALUES
-- 剂量审核规则 (group_id = 4)
(6, 'PA_OVERDOSE_CHECK', '药品超量审核', '检查药品剂量是否超过正常范围', 'PRESCRIPTION_AUDIT', 4, 'CONDITION', 100, 1, '2024-01-01 00:00:00', '2099-12-31 23:59:59', 1, 'system'),
(7, 'PA_LONG_TERM_CHECK', '长期用药审核', '检查用药天数是否超过14天', 'PRESCRIPTION_AUDIT', 4, 'CONDITION', 90, 1, '2024-01-01 00:00:00', '2099-12-31 23:59:59', 1, 'system'),
-- 相互作用规则 (group_id = 5)
(8, 'PA_PREGNANCY_CHECK', '妊娠禁忌审核', '妊娠期禁止使用特定药物', 'PRESCRIPTION_AUDIT', 5, 'CONDITION', 100, 1, '2024-01-01 00:00:00', '2099-12-31 23:59:59', 1, 'system'),
(9, 'PA_CHILD_DOSAGE_CHECK', '儿童剂量审核', '儿童用药剂量特殊限制', 'PRESCRIPTION_AUDIT', 5, 'CONDITION', 95, 1, '2024-01-01 00:00:00', '2099-12-31 23:59:59', 1, 'system'),
(10, 'PA_ALLERGY_CHECK', '过敏药物审核', '检查处方药物是否在患者过敏列表中', 'PRESCRIPTION_AUDIT', 5, 'CONDITION', 100, 1, '2024-01-01 00:00:00', '2099-12-31 23:59:59', 1, 'system');

-- =====================================================
-- 8. 初始化示例规则定义 - 病历质控场景
-- =====================================================
INSERT INTO `rule_definition` (`id`, `rule_code`, `rule_name`, `rule_desc`, `scene_code`, `group_id`, `rule_type`, `priority`, `status`, `effective_start_time`, `effective_end_time`, `version`, `create_by`) VALUES
-- 完整性检查规则 (group_id = 6)
(11, 'MR_MAIN_DIAGNOSIS_CHECK', '主诊断必填检查', '检查病历主诊断是否为空', 'MEDICAL_RECORD_QC', 6, 'CONDITION', 100, 1, '2024-01-01 00:00:00', '2099-12-31 23:59:59', 1, 'system'),
(12, 'MR_SURGERY_RECORD_CHECK', '手术记录检查', '有手术名称时检查住院天数是否合理', 'MEDICAL_RECORD_QC', 6, 'CONDITION', 90, 1, '2024-01-01 00:00:00', '2099-12-31 23:59:59', 1, 'system'),
-- 费用检查规则 (group_id = 7)
(13, 'MR_DRUG_RATIO_CHECK', '药占比检查', '检查药品费用占比是否超过30%', 'MEDICAL_RECORD_QC', 7, 'CONDITION', 100, 1, '2024-01-01 00:00:00', '2099-12-31 23:59:59', 1, 'system'),
(14, 'MR_CONSUMABLE_RATIO_CHECK', '耗材占比检查', '检查耗材费用占比是否超过20%', 'MEDICAL_RECORD_QC', 7, 'CONDITION', 90, 1, '2024-01-01 00:00:00', '2099-12-31 23:59:59', 1, 'system'),
(15, 'MR_HIGH_COST_CHECK', '高额费用检查', '检查住院总费用是否超过5万元', 'MEDICAL_RECORD_QC', 7, 'CONDITION', 80, 1, '2024-01-01 00:00:00', '2099-12-31 23:59:59', 1, 'system');

-- =====================================================
-- 9. 初始化条件组数据
-- =====================================================
INSERT INTO `condition_group` (`id`, `rule_id`, `parent_group_id`, `group_logic`, `group_level`, `sort_order`) VALUES
-- 传染病上报规则条件组
(1, 1, 0, 'AND', 1, 1),   -- HIV规则
(2, 2, 0, 'OR', 1, 1),    -- 肺结核规则 (诊断OR检验)
(3, 3, 0, 'AND', 1, 1),   -- 梅毒规则
(4, 4, 0, 'AND', 1, 1),   -- 乙肝规则
(5, 5, 0, 'AND', 1, 1),   -- 手足口病规则
-- 处方审核规则条件组
(6, 6, 0, 'AND', 1, 1),   -- 超量审核
(7, 7, 0, 'AND', 1, 1),   -- 长期用药
(8, 8, 0, 'AND', 1, 1),   -- 妊娠禁忌
(9, 9, 0, 'AND', 1, 1),   -- 儿童剂量
(10, 10, 0, 'AND', 1, 1), -- 过敏检查
-- 病历质控规则条件组
(11, 11, 0, 'AND', 1, 1), -- 主诊断检查
(12, 12, 0, 'AND', 1, 1), -- 手术记录检查
(13, 13, 0, 'AND', 1, 1), -- 药占比检查
(14, 14, 0, 'AND', 1, 1), -- 耗材占比检查
(15, 15, 0, 'AND', 1, 1); -- 高额费用检查

-- =====================================================
-- 10. 初始化规则条件数据 - 传染病上报场景
-- =====================================================
INSERT INTO `rule_condition` (`id`, `rule_id`, `group_id`, `field_code`, `operator`, `field_value`, `value_type`, `sort_order`) VALUES
-- HIV上报规则条件: HIV抗体 = 阳性
(1, 1, 1, 'lab_hiv_antibody', 'EQ', '阳性', 'CONSTANT', 1),

-- 肺结核上报规则条件: 诊断名称包含"肺结核" OR 结核菌检测 = 阳性
(2, 2, 2, 'diagnosis_name', 'LIKE', '肺结核', 'CONSTANT', 1),
(3, 2, 2, 'lab_tb', 'EQ', '阳性', 'CONSTANT', 2),

-- 梅毒上报规则条件: 梅毒抗体 = 阳性
(4, 3, 3, 'lab_syphilis', 'EQ', '阳性', 'CONSTANT', 1),

-- 乙肝上报规则条件: 乙肝表面抗原 = 阳性 AND 白细胞异常
(5, 4, 4, 'lab_hbsag', 'EQ', '阳性', 'CONSTANT', 1),
(6, 4, 4, 'lab_wbc', 'GT', '10', 'CONSTANT', 2),

-- 手足口病上报规则条件: 诊断名称包含"手足口" AND 年龄 < 5
(7, 5, 5, 'diagnosis_name', 'LIKE', '手足口', 'CONSTANT', 1),
(8, 5, 5, 'patient_age', 'LT', '5', 'CONSTANT', 2);

-- =====================================================
-- 11. 初始化规则条件数据 - 处方审核场景
-- =====================================================
INSERT INTO `rule_condition` (`id`, `rule_id`, `group_id`, `field_code`, `operator`, `field_value`, `value_type`, `sort_order`) VALUES
-- 超量审核条件: 剂量 > 100
(9, 6, 6, 'drug_dosage', 'GT', '100', 'CONSTANT', 1),

-- 长期用药审核条件: 用药天数 > 14
(10, 7, 7, 'drug_days', 'GT', '14', 'CONSTANT', 1),

-- 妊娠禁忌审核条件: 是否妊娠 = true AND 药品名称包含禁忌药
(11, 8, 8, 'is_pregnant', 'EQ', 'true', 'CONSTANT', 1),
(12, 8, 8, 'drug_name', 'IN', '["甲氨蝶呤","华法林","异维A酸","利巴韦林","沙利度胺"]', 'ARRAY', 2),

-- 儿童剂量审核条件: 年龄 < 12 AND 剂量 > 50
(13, 9, 9, 'patient_age', 'LT', '12', 'CONSTANT', 1),
(14, 9, 9, 'drug_dosage', 'GT', '50', 'CONSTANT', 2),

-- 过敏药物审核条件: 药品编码在过敏列表中
(15, 10, 10, 'drug_code', 'IN', 'allergy_drugs', 'FIELD', 1);

-- =====================================================
-- 12. 初始化规则条件数据 - 病历质控场景
-- =====================================================
INSERT INTO `rule_condition` (`id`, `rule_id`, `group_id`, `field_code`, `operator`, `field_value`, `value_type`, `sort_order`) VALUES
-- 主诊断必填检查条件: 主诊断为空
(16, 11, 11, 'main_diagnosis', 'IS_NULL', '', 'CONSTANT', 1),

-- 手术记录检查条件: 有手术名称 AND 住院天数 < 3
(17, 12, 12, 'surgery_name', 'IS_NOT_NULL', '', 'CONSTANT', 1),
(18, 12, 12, 'admission_days', 'LT', '3', 'CONSTANT', 2),

-- 药占比检查条件: 药占比 > 0.3
(19, 13, 13, 'drug_ratio', 'GT', '0.3', 'CONSTANT', 1),

-- 耗材占比检查条件: 耗材占比 > 0.2
(20, 14, 14, 'consumable_ratio', 'GT', '0.2', 'CONSTANT', 1),

-- 高额费用检查条件: 总费用 > 50000
(21, 15, 15, 'total_cost', 'GT', '50000', 'CONSTANT', 1);

-- =====================================================
-- 13. 初始化规则动作数据 - 传染病上报场景
-- =====================================================
INSERT INTO `rule_action` (`id`, `rule_id`, `action_type`, `action_code`, `action_params`, `sort_order`) VALUES
-- HIV上报动作
(1, 1, 'RETURN', 'HIV_REPORT', '{"diseaseCode":"B20","diseaseName":"HIV/AIDS","reportLevel":"甲类","reportType":"MANDATORY","message":"检测到HIV抗体阳性，需立即上报传染病报卡"}', 1),
(2, 1, 'SEND_MESSAGE', 'NOTIFY_CDC', '{"notifyType":"URGENT","department":"疾控中心","content":"发现HIV阳性病例，请及时处理"}', 2),

-- 肺结核上报动作
(3, 2, 'RETURN', 'TB_REPORT', '{"diseaseCode":"A15","diseaseName":"肺结核","reportLevel":"乙类","reportType":"MANDATORY","message":"检测到肺结核相关指标，需上报传染病报卡"}', 1),

-- 梅毒上报动作
(4, 3, 'RETURN', 'SYPHILIS_REPORT', '{"diseaseCode":"A51","diseaseName":"梅毒","reportLevel":"乙类","reportType":"MANDATORY","message":"检测到梅毒抗体阳性，需上报传染病报卡"}', 1),

-- 乙肝上报动作
(5, 4, 'RETURN', 'HEPATITIS_B_REPORT', '{"diseaseCode":"B16","diseaseName":"乙型肝炎","reportLevel":"乙类","reportType":"MANDATORY","message":"检测到乙肝表面抗原阳性且白细胞异常，需上报传染病报卡"}', 1),

-- 手足口病上报动作
(6, 5, 'RETURN', 'HFMD_REPORT', '{"diseaseCode":"B08.4","diseaseName":"手足口病","reportLevel":"丙类","reportType":"MANDATORY","message":"检测到5岁以下儿童手足口病，需上报传染病报卡"}', 1);

-- =====================================================
-- 14. 初始化规则动作数据 - 处方审核场景
-- =====================================================
INSERT INTO `rule_action` (`id`, `rule_id`, `action_type`, `action_code`, `action_params`, `sort_order`) VALUES
-- 超量审核动作
(7, 6, 'RETURN', 'OVERDOSE_ALERT', '{"alertLevel":"WARNING","alertType":"OVERDOSE","message":"药品剂量超过正常范围，请核实处方"}', 1),

-- 长期用药审核动作
(8, 7, 'RETURN', 'LONG_TERM_ALERT', '{"alertLevel":"INFO","alertType":"LONG_TERM","message":"用药天数超过14天，请确认是否为慢性病用药"}', 1),

-- 妊娠禁忌审核动作
(9, 8, 'RETURN', 'PREGNANCY_ALERT', '{"alertLevel":"ERROR","alertType":"CONTRAINDICATION","message":"该药物为妊娠禁忌药，禁止开具"}', 1),

-- 儿童剂量审核动作
(10, 9, 'RETURN', 'CHILD_DOSAGE_ALERT', '{"alertLevel":"WARNING","alertType":"CHILD_DOSAGE","message":"儿童用药剂量偏高，请核实"}', 1),

-- 过敏药物审核动作
(11, 10, 'RETURN', 'ALLERGY_ALERT', '{"alertLevel":"ERROR","alertType":"ALLERGY","message":"患者对该药物过敏，禁止开具"}', 1);

-- =====================================================
-- 15. 初始化规则动作数据 - 病历质控场景
-- =====================================================
INSERT INTO `rule_action` (`id`, `rule_id`, `action_type`, `action_code`, `action_params`, `sort_order`) VALUES
-- 主诊断检查动作
(12, 11, 'RETURN', 'DIAGNOSIS_MISSING', '{"qcLevel":"ERROR","qcType":"COMPLETENESS","deductPoints":10,"message":"病历缺少主诊断，请补充完善"}', 1),

-- 手术记录检查动作
(13, 12, 'RETURN', 'SURGERY_DAYS_ALERT', '{"qcLevel":"WARNING","qcType":"VALIDITY","deductPoints":5,"message":"手术患者住院天数少于3天，请核实病历记录"}', 1),

-- 药占比检查动作
(14, 13, 'RETURN', 'DRUG_RATIO_ALERT', '{"qcLevel":"WARNING","qcType":"COST","deductPoints":3,"message":"药占比超过30%，需要合理解释"}', 1),

-- 耗材占比检查动作
(15, 14, 'RETURN', 'CONSUMABLE_RATIO_ALERT', '{"qcLevel":"WARNING","qcType":"COST","deductPoints":3,"message":"耗材占比超过20%，需要合理解释"}', 1),

-- 高额费用检查动作
(16, 15, 'RETURN', 'HIGH_COST_ALERT', '{"qcLevel":"INFO","qcType":"COST","deductPoints":0,"message":"住院总费用超过5万元，请进行费用审核"}', 1);

-- =====================================================
-- 16. 评估量表业务场景配置
-- =====================================================

-- 自理能力评估场景
INSERT INTO `business_scene` (`scene_code`, `scene_name`, `scene_desc`, `adapter_class`, `item_pattern`, `item_count`, `status`, `sort_order`, `create_by`) VALUES
('SELF_CARE_ASSESSMENT', '老年人生活自理能力评估', '评估老年人日常生活自理能力，包括进餐、梳洗、穿衣、如厕、活动五个维度', 'com.example.ruleengine.adapter.TotalScoreAssessmentAdapter', 'q{n}', 5, 1, 4, 'system');

-- 体质评估场景
INSERT INTO `business_scene` (`scene_code`, `scene_name`, `scene_desc`, `adapter_class`, `item_pattern`, `item_count`, `status`, `sort_order`, `create_by`) VALUES
('CONSTITUTION_ASSESSMENT', '中老年体质评估', '中医药健康管理服务体质辨识，包含33道题目，评估9种体质类型', 'com.example.ruleengine.adapter.GroupScoreAssessmentAdapter', 'q{n}', 33, 1, 5, 'system');

-- =====================================================
-- 17. 自理能力评估场景字段元数据
-- =====================================================
INSERT INTO `field_metadata` (`scene_code`, `field_code`, `field_name`, `field_type`, `data_source`, `supported_operators`, `value_range`, `category`, `sort_order`, `create_by`) VALUES
-- 题目字段
('SELF_CARE_ASSESSMENT', 'q1', '进餐', 'Number', '评估量表', 'EQ', '[{"label":"独立完成","value":0},{"label":"需协助","value":3},{"label":"完全需要帮助","value":5}]', '评估题目', 1, 'system'),
('SELF_CARE_ASSESSMENT', 'q2', '梳洗', 'Number', '评估量表', 'EQ', '[{"label":"独立完成","value":0},{"label":"需协助","value":1},{"label":"部分完成","value":3},{"label":"完全需要帮助","value":7}]', '评估题目', 2, 'system'),
('SELF_CARE_ASSESSMENT', 'q3', '穿衣', 'Number', '评估量表', 'EQ', '[{"label":"独立完成","value":0},{"label":"部分完成","value":3},{"label":"完全需要帮助","value":5}]', '评估题目', 3, 'system'),
('SELF_CARE_ASSESSMENT', 'q4', '如厕', 'Number', '评估量表', 'EQ', '[{"label":"不需协助","value":0},{"label":"偶尔失禁","value":1},{"label":"经常失禁","value":5},{"label":"完全失禁","value":10}]', '评估题目', 4, 'system'),
('SELF_CARE_ASSESSMENT', 'q5', '活动', 'Number', '评估量表', 'EQ', '[{"label":"独立完成","value":0},{"label":"借助辅助","value":1},{"label":"借助较大外力","value":5},{"label":"卧床不起","value":10}]', '评估题目', 5, 'system'),
-- 计算字段
('SELF_CARE_ASSESSMENT', 'totalScore', '总分', 'Number', '适配器计算', 'GT,GTE,LT,LTE,BETWEEN,EQ', '0-37', '计算结果', 10, 'system');

-- =====================================================
-- 18. 体质评估场景字段元数据（部分示例）
-- =====================================================
INSERT INTO `field_metadata` (`scene_code`, `field_code`, `field_name`, `field_type`, `data_source`, `supported_operators`, `value_range`, `category`, `sort_order`, `create_by`) VALUES
-- 分组得分字段
('CONSTITUTION_ASSESSMENT', 'qixu_score', '气虚质得分', 'Number', '适配器计算', 'GT,GTE,LT,LTE,BETWEEN,EQ', '4-20', '体质得分', 1, 'system'),
('CONSTITUTION_ASSESSMENT', 'yangxu_score', '阳虚质得分', 'Number', '适配器计算', 'GT,GTE,LT,LTE,BETWEEN,EQ', '4-20', '体质得分', 2, 'system'),
('CONSTITUTION_ASSESSMENT', 'yinxu_score', '阴虚质得分', 'Number', '适配器计算', 'GT,GTE,LT,LTE,BETWEEN,EQ', '4-20', '体质得分', 3, 'system'),
('CONSTITUTION_ASSESSMENT', 'tanshi_score', '痰湿质得分', 'Number', '适配器计算', 'GT,GTE,LT,LTE,BETWEEN,EQ', '4-20', '体质得分', 4, 'system'),
('CONSTITUTION_ASSESSMENT', 'shire_score', '湿热质得分', 'Number', '适配器计算', 'GT,GTE,LT,LTE,BETWEEN,EQ', '4-20', '体质得分', 5, 'system'),
('CONSTITUTION_ASSESSMENT', 'xueyu_score', '血瘀质得分', 'Number', '适配器计算', 'GT,GTE,LT,LTE,BETWEEN,EQ', '4-20', '体质得分', 6, 'system'),
('CONSTITUTION_ASSESSMENT', 'qiyu_score', '气郁质得分', 'Number', '适配器计算', 'GT,GTE,LT,LTE,BETWEEN,EQ', '4-20', '体质得分', 7, 'system'),
('CONSTITUTION_ASSESSMENT', 'tebing_score', '特禀质得分', 'Number', '适配器计算', 'GT,GTE,LT,LTE,BETWEEN,EQ', '4-20', '体质得分', 8, 'system'),
('CONSTITUTION_ASSESSMENT', 'pinghe_score', '平和质得分', 'Number', '适配器计算', 'GT,GTE,LT,LTE,BETWEEN,EQ', '5-25', '体质得分', 9, 'system'),
('CONSTITUTION_ASSESSMENT', 'other_max_score', '其他体质最高分', 'Number', '适配器计算', 'GT,GTE,LT,LTE,BETWEEN,EQ', '4-20', '辅助计算', 10, 'system');

-- =====================================================
-- 19. 体质评估分组映射配置
-- =====================================================
INSERT INTO `assessment_group_mapping` (`scene_code`, `group_code`, `group_name`, `item_list`, `reverse_items`, `sort_order`, `create_by`) VALUES
('CONSTITUTION_ASSESSMENT', 'qixu', '气虚质', '[2,3,4,14]', NULL, 1, 'system'),
('CONSTITUTION_ASSESSMENT', 'yangxu', '阳虚质', '[11,12,13,29]', NULL, 2, 'system'),
('CONSTITUTION_ASSESSMENT', 'yinxu', '阴虚质', '[10,21,26,31]', NULL, 3, 'system'),
('CONSTITUTION_ASSESSMENT', 'tanshi', '痰湿质', '[9,16,28,32]', NULL, 4, 'system'),
('CONSTITUTION_ASSESSMENT', 'shire', '湿热质', '[23,25,27,30]', NULL, 5, 'system'),
('CONSTITUTION_ASSESSMENT', 'xueyu', '血瘀质', '[19,22,24,33]', NULL, 6, 'system'),
('CONSTITUTION_ASSESSMENT', 'qiyu', '气郁质', '[5,6,7,8]', NULL, 7, 'system'),
('CONSTITUTION_ASSESSMENT', 'tebing', '特禀质', '[15,17,18,20]', NULL, 8, 'system'),
('CONSTITUTION_ASSESSMENT', 'pinghe', '平和质', '[1,2,4,5,13]', '[2,4,5,13]', 9, 'system');

-- =====================================================
-- 20. 评估量表规则组
-- =====================================================
INSERT INTO `rule_group` (`scene_code`, `group_code`, `group_name`, `group_desc`, `parent_id`, `execution_mode`, `sort_order`, `create_by`) VALUES
('SELF_CARE_ASSESSMENT', 'SELF_CARE_LEVEL', '自理能力等级判定', '根据总分判定自理能力等级', 0, 'FIRST', 1, 'system'),
('CONSTITUTION_ASSESSMENT', 'CONSTITUTION_DEVIANT', '偏颇体质判定', '判定8种偏颇体质', 0, 'ALL', 1, 'system'),
('CONSTITUTION_ASSESSMENT', 'CONSTITUTION_BALANCED', '平和质判定', '判定平和质', 0, 'FIRST', 2, 'system');

-- =====================================================
-- 21. 自理能力评估规则定义
-- =====================================================
INSERT INTO `rule_definition` (`rule_code`, `rule_name`, `rule_desc`, `scene_code`, `group_id`, `rule_type`, `priority`, `status`, `effective_start_time`, `effective_end_time`, `version`, `create_by`) VALUES
('SELF_CARE_LEVEL_1', '可自理判定', '总分0-3分，判定为可自理', 'SELF_CARE_ASSESSMENT', (SELECT id FROM rule_group WHERE group_code = 'SELF_CARE_LEVEL'), 'CONDITION', 100, 1, '2024-01-01 00:00:00', '2099-12-31 23:59:59', 1, 'system'),
('SELF_CARE_LEVEL_2', '轻度依赖判定', '总分4-8分，判定为轻度依赖', 'SELF_CARE_ASSESSMENT', (SELECT id FROM rule_group WHERE group_code = 'SELF_CARE_LEVEL'), 'CONDITION', 90, 1, '2024-01-01 00:00:00', '2099-12-31 23:59:59', 1, 'system'),
('SELF_CARE_LEVEL_3', '中度依赖判定', '总分9-18分，判定为中度依赖', 'SELF_CARE_ASSESSMENT', (SELECT id FROM rule_group WHERE group_code = 'SELF_CARE_LEVEL'), 'CONDITION', 80, 1, '2024-01-01 00:00:00', '2099-12-31 23:59:59', 1, 'system'),
('SELF_CARE_LEVEL_4', '不能自理判定', '总分≥19分，判定为不能自理', 'SELF_CARE_ASSESSMENT', (SELECT id FROM rule_group WHERE group_code = 'SELF_CARE_LEVEL'), 'CONDITION', 70, 1, '2024-01-01 00:00:00', '2099-12-31 23:59:59', 1, 'system');

-- =====================================================
-- 22. 体质评估规则定义 - 偏颇体质
-- =====================================================
INSERT INTO `rule_definition` (`rule_code`, `rule_name`, `rule_desc`, `scene_code`, `group_id`, `rule_type`, `priority`, `status`, `effective_start_time`, `effective_end_time`, `version`, `create_by`) VALUES
-- 气虚质判定
('CONSTITUTION_QIXU_YES', '气虚质=是', '气虚质得分≥11分，判定为是', 'CONSTITUTION_ASSESSMENT', (SELECT id FROM rule_group WHERE group_code = 'CONSTITUTION_DEVIANT'), 'CONDITION', 100, 1, '2024-01-01 00:00:00', '2099-12-31 23:59:59', 1, 'system'),
('CONSTITUTION_QIXU_TEND', '气虚质=倾向是', '气虚质得分9-10分，判定为倾向是', 'CONSTITUTION_ASSESSMENT', (SELECT id FROM rule_group WHERE group_code = 'CONSTITUTION_DEVIANT'), 'CONDITION', 90, 1, '2024-01-01 00:00:00', '2099-12-31 23:59:59', 1, 'system'),
('CONSTITUTION_QIXU_NO', '气虚质=否', '气虚质得分≤8分，判定为否', 'CONSTITUTION_ASSESSMENT', (SELECT id FROM rule_group WHERE group_code = 'CONSTITUTION_DEVIANT'), 'CONDITION', 80, 1, '2024-01-01 00:00:00', '2099-12-31 23:59:59', 1, 'system'),
-- 阳虚质判定
('CONSTITUTION_YANGXU_YES', '阳虚质=是', '阳虚质得分≥11分，判定为是', 'CONSTITUTION_ASSESSMENT', (SELECT id FROM rule_group WHERE group_code = 'CONSTITUTION_DEVIANT'), 'CONDITION', 100, 1, '2024-01-01 00:00:00', '2099-12-31 23:59:59', 1, 'system'),
('CONSTITUTION_YANGXU_TEND', '阳虚质=倾向是', '阳虚质得分9-10分，判定为倾向是', 'CONSTITUTION_ASSESSMENT', (SELECT id FROM rule_group WHERE group_code = 'CONSTITUTION_DEVIANT'), 'CONDITION', 90, 1, '2024-01-01 00:00:00', '2099-12-31 23:59:59', 1, 'system'),
('CONSTITUTION_YANGXU_NO', '阳虚质=否', '阳虚质得分≤8分，判定为否', 'CONSTITUTION_ASSESSMENT', (SELECT id FROM rule_group WHERE group_code = 'CONSTITUTION_DEVIANT'), 'CONDITION', 80, 1, '2024-01-01 00:00:00', '2099-12-31 23:59:59', 1, 'system'),
-- 平和质判定
('CONSTITUTION_PINGHE_YES', '平和质=是', '平和质得分≥17分且其他体质最高分<8分', 'CONSTITUTION_ASSESSMENT', (SELECT id FROM rule_group WHERE group_code = 'CONSTITUTION_BALANCED'), 'CONDITION', 100, 1, '2024-01-01 00:00:00', '2099-12-31 23:59:59', 1, 'system'),
('CONSTITUTION_PINGHE_BASIC', '平和质=基本是', '平和质得分≥17分且其他体质最高分8-9分', 'CONSTITUTION_ASSESSMENT', (SELECT id FROM rule_group WHERE group_code = 'CONSTITUTION_BALANCED'), 'CONDITION', 90, 1, '2024-01-01 00:00:00', '2099-12-31 23:59:59', 1, 'system'),
('CONSTITUTION_PINGHE_NO', '平和质=否', '不满足平和质条件', 'CONSTITUTION_ASSESSMENT', (SELECT id FROM rule_group WHERE group_code = 'CONSTITUTION_BALANCED'), 'CONDITION', 80, 1, '2024-01-01 00:00:00', '2099-12-31 23:59:59', 1, 'system');

-- =====================================================
-- 23. 自理能力评估条件组
-- =====================================================
INSERT INTO `condition_group` (`rule_id`, `parent_group_id`, `group_logic`, `group_level`, `sort_order`) VALUES
((SELECT id FROM rule_definition WHERE rule_code = 'SELF_CARE_LEVEL_1'), 0, 'AND', 1, 1),
((SELECT id FROM rule_definition WHERE rule_code = 'SELF_CARE_LEVEL_2'), 0, 'AND', 1, 1),
((SELECT id FROM rule_definition WHERE rule_code = 'SELF_CARE_LEVEL_3'), 0, 'AND', 1, 1),
((SELECT id FROM rule_definition WHERE rule_code = 'SELF_CARE_LEVEL_4'), 0, 'AND', 1, 1);

-- =====================================================
-- 24. 自理能力评估规则条件
-- =====================================================
INSERT INTO `rule_condition` (`rule_id`, `group_id`, `field_code`, `operator`, `field_value`, `value_type`, `sort_order`) VALUES
-- 可自理: totalScore BETWEEN 0 AND 3
((SELECT id FROM rule_definition WHERE rule_code = 'SELF_CARE_LEVEL_1'),
 (SELECT cg.id FROM condition_group cg JOIN rule_definition rd ON cg.rule_id = rd.id WHERE rd.rule_code = 'SELF_CARE_LEVEL_1'),
 'totalScore', 'BETWEEN', '0,3', 'CONSTANT', 1),

-- 轻度依赖: totalScore BETWEEN 4 AND 8
((SELECT id FROM rule_definition WHERE rule_code = 'SELF_CARE_LEVEL_2'),
 (SELECT cg.id FROM condition_group cg JOIN rule_definition rd ON cg.rule_id = rd.id WHERE rd.rule_code = 'SELF_CARE_LEVEL_2'),
 'totalScore', 'BETWEEN', '4,8', 'CONSTANT', 1),

-- 中度依赖: totalScore BETWEEN 9 AND 18
((SELECT id FROM rule_definition WHERE rule_code = 'SELF_CARE_LEVEL_3'),
 (SELECT cg.id FROM condition_group cg JOIN rule_definition rd ON cg.rule_id = rd.id WHERE rd.rule_code = 'SELF_CARE_LEVEL_3'),
 'totalScore', 'BETWEEN', '9,18', 'CONSTANT', 1),

-- 不能自理: totalScore >= 19
((SELECT id FROM rule_definition WHERE rule_code = 'SELF_CARE_LEVEL_4'),
 (SELECT cg.id FROM condition_group cg JOIN rule_definition rd ON cg.rule_id = rd.id WHERE rd.rule_code = 'SELF_CARE_LEVEL_4'),
 'totalScore', 'GTE', '19', 'CONSTANT', 1);

-- =====================================================
-- 25. 自理能力评估规则动作
-- =====================================================
INSERT INTO `rule_action` (`rule_id`, `action_type`, `action_code`, `action_params`, `sort_order`) VALUES
((SELECT id FROM rule_definition WHERE rule_code = 'SELF_CARE_LEVEL_1'), 'RETURN', 'SELF_CARE_RESULT', '{"assessmentResult":"可自理","level":1,"suggestion":"老年人生活自理能力良好，建议保持现有生活习惯，定期进行健康检查"}', 1),
((SELECT id FROM rule_definition WHERE rule_code = 'SELF_CARE_LEVEL_2'), 'RETURN', 'SELF_CARE_RESULT', '{"assessmentResult":"轻度依赖","level":2,"suggestion":"老年人存在轻度依赖，建议关注日常起居，必要时提供适当协助"}', 1),
((SELECT id FROM rule_definition WHERE rule_code = 'SELF_CARE_LEVEL_3'), 'RETURN', 'SELF_CARE_RESULT', '{"assessmentResult":"中度依赖","level":3,"suggestion":"老年人存在中度依赖，建议加强日常照护，重点关注如厕和活动能力"}', 1),
((SELECT id FROM rule_definition WHERE rule_code = 'SELF_CARE_LEVEL_4'), 'RETURN', 'SELF_CARE_RESULT', '{"assessmentResult":"不能自理","level":4,"suggestion":"老年人不能自理，需要专人照护，建议考虑专业护理服务"}', 1);

-- =====================================================
-- 26. 体质评估条件组（部分示例）
-- =====================================================
INSERT INTO `condition_group` (`rule_id`, `parent_group_id`, `group_logic`, `group_level`, `sort_order`) VALUES
-- 气虚质
((SELECT id FROM rule_definition WHERE rule_code = 'CONSTITUTION_QIXU_YES'), 0, 'AND', 1, 1),
((SELECT id FROM rule_definition WHERE rule_code = 'CONSTITUTION_QIXU_TEND'), 0, 'AND', 1, 1),
((SELECT id FROM rule_definition WHERE rule_code = 'CONSTITUTION_QIXU_NO'), 0, 'AND', 1, 1),
-- 阳虚质
((SELECT id FROM rule_definition WHERE rule_code = 'CONSTITUTION_YANGXU_YES'), 0, 'AND', 1, 1),
((SELECT id FROM rule_definition WHERE rule_code = 'CONSTITUTION_YANGXU_TEND'), 0, 'AND', 1, 1),
((SELECT id FROM rule_definition WHERE rule_code = 'CONSTITUTION_YANGXU_NO'), 0, 'AND', 1, 1),
-- 平和质
((SELECT id FROM rule_definition WHERE rule_code = 'CONSTITUTION_PINGHE_YES'), 0, 'AND', 1, 1),
((SELECT id FROM rule_definition WHERE rule_code = 'CONSTITUTION_PINGHE_BASIC'), 0, 'AND', 1, 1),
((SELECT id FROM rule_definition WHERE rule_code = 'CONSTITUTION_PINGHE_NO'), 0, 'AND', 1, 1);

-- =====================================================
-- 27. 体质评估规则条件（部分示例）
-- =====================================================
INSERT INTO `rule_condition` (`rule_id`, `group_id`, `field_code`, `operator`, `field_value`, `value_type`, `sort_order`) VALUES
-- 气虚质=是: qixu_score >= 11
((SELECT id FROM rule_definition WHERE rule_code = 'CONSTITUTION_QIXU_YES'),
 (SELECT cg.id FROM condition_group cg JOIN rule_definition rd ON cg.rule_id = rd.id WHERE rd.rule_code = 'CONSTITUTION_QIXU_YES'),
 'qixu_score', 'GTE', '11', 'CONSTANT', 1),

-- 气虚质=倾向是: qixu_score BETWEEN 9 AND 10
((SELECT id FROM rule_definition WHERE rule_code = 'CONSTITUTION_QIXU_TEND'),
 (SELECT cg.id FROM condition_group cg JOIN rule_definition rd ON cg.rule_id = rd.id WHERE rd.rule_code = 'CONSTITUTION_QIXU_TEND'),
 'qixu_score', 'BETWEEN', '9,10', 'CONSTANT', 1),

-- 气虚质=否: qixu_score <= 8
((SELECT id FROM rule_definition WHERE rule_code = 'CONSTITUTION_QIXU_NO'),
 (SELECT cg.id FROM condition_group cg JOIN rule_definition rd ON cg.rule_id = rd.id WHERE rd.rule_code = 'CONSTITUTION_QIXU_NO'),
 'qixu_score', 'LTE', '8', 'CONSTANT', 1),

-- 阳虚质=是: yangxu_score >= 11
((SELECT id FROM rule_definition WHERE rule_code = 'CONSTITUTION_YANGXU_YES'),
 (SELECT cg.id FROM condition_group cg JOIN rule_definition rd ON cg.rule_id = rd.id WHERE rd.rule_code = 'CONSTITUTION_YANGXU_YES'),
 'yangxu_score', 'GTE', '11', 'CONSTANT', 1),

-- 阳虚质=倾向是: yangxu_score BETWEEN 9 AND 10
((SELECT id FROM rule_definition WHERE rule_code = 'CONSTITUTION_YANGXU_TEND'),
 (SELECT cg.id FROM condition_group cg JOIN rule_definition rd ON cg.rule_id = rd.id WHERE rd.rule_code = 'CONSTITUTION_YANGXU_TEND'),
 'yangxu_score', 'BETWEEN', '9,10', 'CONSTANT', 1),

-- 阳虚质=否: yangxu_score <= 8
((SELECT id FROM rule_definition WHERE rule_code = 'CONSTITUTION_YANGXU_NO'),
 (SELECT cg.id FROM condition_group cg JOIN rule_definition rd ON cg.rule_id = rd.id WHERE rd.rule_code = 'CONSTITUTION_YANGXU_NO'),
 'yangxu_score', 'LTE', '8', 'CONSTANT', 1),

-- 平和质=是: pinghe_score >= 17 AND other_max_score < 8
((SELECT id FROM rule_definition WHERE rule_code = 'CONSTITUTION_PINGHE_YES'),
 (SELECT cg.id FROM condition_group cg JOIN rule_definition rd ON cg.rule_id = rd.id WHERE rd.rule_code = 'CONSTITUTION_PINGHE_YES'),
 'pinghe_score', 'GTE', '17', 'CONSTANT', 1),
((SELECT id FROM rule_definition WHERE rule_code = 'CONSTITUTION_PINGHE_YES'),
 (SELECT cg.id FROM condition_group cg JOIN rule_definition rd ON cg.rule_id = rd.id WHERE rd.rule_code = 'CONSTITUTION_PINGHE_YES'),
 'other_max_score', 'LT', '8', 'CONSTANT', 2),

-- 平和质=基本是: pinghe_score >= 17 AND other_max_score BETWEEN 8 AND 9
((SELECT id FROM rule_definition WHERE rule_code = 'CONSTITUTION_PINGHE_BASIC'),
 (SELECT cg.id FROM condition_group cg JOIN rule_definition rd ON cg.rule_id = rd.id WHERE rd.rule_code = 'CONSTITUTION_PINGHE_BASIC'),
 'pinghe_score', 'GTE', '17', 'CONSTANT', 1),
((SELECT id FROM rule_definition WHERE rule_code = 'CONSTITUTION_PINGHE_BASIC'),
 (SELECT cg.id FROM condition_group cg JOIN rule_definition rd ON cg.rule_id = rd.id WHERE rd.rule_code = 'CONSTITUTION_PINGHE_BASIC'),
 'other_max_score', 'BETWEEN', '8,9', 'CONSTANT', 2),

-- 平和质=否: other_max_score >= 10 OR pinghe_score < 17 (简化为 other_max_score >= 10)
((SELECT id FROM rule_definition WHERE rule_code = 'CONSTITUTION_PINGHE_NO'),
 (SELECT cg.id FROM condition_group cg JOIN rule_definition rd ON cg.rule_id = rd.id WHERE rd.rule_code = 'CONSTITUTION_PINGHE_NO'),
 'other_max_score', 'GTE', '10', 'CONSTANT', 1);

-- =====================================================
-- 28. 体质评估规则动作
-- =====================================================
INSERT INTO `rule_action` (`rule_id`, `action_type`, `action_code`, `action_params`, `sort_order`) VALUES
-- 气虚质
((SELECT id FROM rule_definition WHERE rule_code = 'CONSTITUTION_QIXU_YES'), 'RETURN', 'CONSTITUTION_RESULT', '{"qixu_result":"是","guidance":["情志调摄","饮食调养","起居调摄","运动保健","穴位保健"]}', 1),
((SELECT id FROM rule_definition WHERE rule_code = 'CONSTITUTION_QIXU_TEND'), 'RETURN', 'CONSTITUTION_RESULT', '{"qixu_result":"倾向是","guidance":["情志调摄","饮食调养"]}', 1),
((SELECT id FROM rule_definition WHERE rule_code = 'CONSTITUTION_QIXU_NO'), 'RETURN', 'CONSTITUTION_RESULT', '{"qixu_result":"否"}', 1),

-- 阳虚质
((SELECT id FROM rule_definition WHERE rule_code = 'CONSTITUTION_YANGXU_YES'), 'RETURN', 'CONSTITUTION_RESULT', '{"yangxu_result":"是","guidance":["情志调摄","饮食调养","起居调摄","运动保健","穴位保健"]}', 1),
((SELECT id FROM rule_definition WHERE rule_code = 'CONSTITUTION_YANGXU_TEND'), 'RETURN', 'CONSTITUTION_RESULT', '{"yangxu_result":"倾向是","guidance":["饮食调养","起居调摄"]}', 1),
((SELECT id FROM rule_definition WHERE rule_code = 'CONSTITUTION_YANGXU_NO'), 'RETURN', 'CONSTITUTION_RESULT', '{"yangxu_result":"否"}', 1),

-- 平和质
((SELECT id FROM rule_definition WHERE rule_code = 'CONSTITUTION_PINGHE_YES'), 'RETURN', 'CONSTITUTION_RESULT', '{"pinghe_result":"是","message":"体质平和，身体健康，建议保持良好生活习惯"}', 1),
((SELECT id FROM rule_definition WHERE rule_code = 'CONSTITUTION_PINGHE_BASIC'), 'RETURN', 'CONSTITUTION_RESULT', '{"pinghe_result":"基本是","message":"体质基本平和，存在轻微偏颇倾向，建议适当调理"}', 1),
((SELECT id FROM rule_definition WHERE rule_code = 'CONSTITUTION_PINGHE_NO'), 'RETURN', 'CONSTITUTION_RESULT', '{"pinghe_result":"否","message":"体质存在明显偏颇，建议进行针对性调理"}', 1);
