-- 测试数据初始化

-- 业务场景
INSERT INTO business_scene (scene_code, scene_name, scene_desc, adapter_class, status, sort_order, create_by)
VALUES ('INFECTIOUS_DISEASE', '传染病上报', '传染病预警和强制上报规则', 'com.example.ruleengine.adapter.InfectiousDiseaseAdapter', 1, 1, 'system');

INSERT INTO business_scene (scene_code, scene_name, scene_desc, adapter_class, status, sort_order, create_by)
VALUES ('PRESCRIPTION_AUDIT', '处方审核', '处方合理性审核规则', 'com.example.ruleengine.adapter.PrescriptionAuditAdapter', 1, 2, 'system');

INSERT INTO business_scene (scene_code, scene_name, scene_desc, adapter_class, status, sort_order, create_by)
VALUES ('MEDICAL_RECORD_QC', '病历质控', '病历质量控制规则', 'com.example.ruleengine.adapter.MedicalRecordQCAdapter', 1, 3, 'system');

-- 字段元数据 - 传染病上报场景
INSERT INTO field_metadata (scene_code, field_code, field_name, field_type, data_source, supported_operators, value_range, category, sort_order, create_by)
VALUES ('INFECTIOUS_DISEASE', 'lab_hiv_antibody', 'HIV抗体', 'String', '检验报告', 'EQ,IN', '阴性/阳性', '检验信息', 10, 'system');

INSERT INTO field_metadata (scene_code, field_code, field_name, field_type, data_source, supported_operators, value_range, category, sort_order, create_by)
VALUES ('INFECTIOUS_DISEASE', 'diagnosis_name', '诊断名称', 'String', '诊断数据', 'IN,NOT_IN,LEFT_MATCH,LIKE', '诊断名称', '诊断信息', 2, 'system');

INSERT INTO field_metadata (scene_code, field_code, field_name, field_type, data_source, supported_operators, value_range, category, sort_order, create_by)
VALUES ('INFECTIOUS_DISEASE', 'patient_age', '患者年龄', 'Number', '患者信息', 'GT,GTE,LT,LTE,BETWEEN,EQ', '0-150', '患者信息', 20, 'system');

-- 规则组
INSERT INTO rule_group (scene_code, group_code, group_name, group_desc, parent_id, execution_mode, sort_order, create_by)
VALUES ('INFECTIOUS_DISEASE', 'ID_CLASS_A', '甲类传染病', '甲类传染病规则组', 0, 'ALL', 1, 'system');

INSERT INTO rule_group (scene_code, group_code, group_name, group_desc, parent_id, execution_mode, sort_order, create_by)
VALUES ('INFECTIOUS_DISEASE', 'ID_CLASS_B', '乙类传染病', '乙类传染病规则组', 0, 'ALL', 2, 'system');

INSERT INTO rule_group (scene_code, group_code, group_name, group_desc, parent_id, execution_mode, sort_order, create_by)
VALUES ('PRESCRIPTION_AUDIT', 'PA_DOSAGE', '剂量审核', '药品剂量审核规则组', 0, 'ALL', 1, 'system');

INSERT INTO rule_group (scene_code, group_code, group_name, group_desc, parent_id, execution_mode, sort_order, create_by)
VALUES ('MEDICAL_RECORD_QC', 'MR_COMPLETENESS', '完整性检查', '病历完整性检查规则组', 0, 'ALL', 1, 'system');
