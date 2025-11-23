-- 业务场景表
CREATE TABLE IF NOT EXISTS business_scene (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  scene_code VARCHAR(50) NOT NULL,
  scene_name VARCHAR(100) NOT NULL,
  scene_desc VARCHAR(500),
  adapter_class VARCHAR(200) NOT NULL,
  status TINYINT DEFAULT 1,
  sort_order INT DEFAULT 0,
  create_by VARCHAR(50) DEFAULT 'system',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_by VARCHAR(50),
  update_time TIMESTAMP,
  is_deleted TINYINT DEFAULT 0
);

-- 字段元数据表
CREATE TABLE IF NOT EXISTS field_metadata (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  scene_code VARCHAR(50) NOT NULL,
  field_code VARCHAR(100) NOT NULL,
  field_name VARCHAR(100) NOT NULL,
  field_type VARCHAR(20) NOT NULL,
  data_source VARCHAR(100),
  supported_operators VARCHAR(500) NOT NULL,
  value_range VARCHAR(500),
  category VARCHAR(50),
  sort_order INT DEFAULT 0,
  is_required TINYINT DEFAULT 0,
  create_by VARCHAR(50) DEFAULT 'system',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_by VARCHAR(50),
  update_time TIMESTAMP,
  is_deleted TINYINT DEFAULT 0
);

-- 规则组表
CREATE TABLE IF NOT EXISTS rule_group (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  scene_code VARCHAR(50) NOT NULL,
  group_code VARCHAR(100) NOT NULL,
  group_name VARCHAR(100) NOT NULL,
  group_desc VARCHAR(500),
  parent_id BIGINT DEFAULT 0,
  execution_mode VARCHAR(20) DEFAULT 'ALL',
  sort_order INT DEFAULT 0,
  create_by VARCHAR(50) DEFAULT 'system',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_by VARCHAR(50),
  update_time TIMESTAMP,
  is_deleted TINYINT DEFAULT 0
);

-- 规则定义表
CREATE TABLE IF NOT EXISTS rule_definition (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  rule_code VARCHAR(100) NOT NULL,
  rule_name VARCHAR(100) NOT NULL,
  rule_desc VARCHAR(500),
  scene_code VARCHAR(50) NOT NULL,
  group_id BIGINT,
  rule_type VARCHAR(20) DEFAULT 'CONDITION',
  priority INT DEFAULT 0,
  status TINYINT DEFAULT 1,
  effective_start_time TIMESTAMP,
  effective_end_time TIMESTAMP,
  drl_content CLOB,
  json_config CLOB,
  version INT DEFAULT 1,
  create_by VARCHAR(50) DEFAULT 'system',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_by VARCHAR(50),
  update_time TIMESTAMP,
  is_deleted TINYINT DEFAULT 0
);

-- 条件组表
CREATE TABLE IF NOT EXISTS condition_group (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  rule_id BIGINT NOT NULL,
  parent_group_id BIGINT DEFAULT 0,
  group_logic VARCHAR(10) DEFAULT 'AND',
  group_level INT DEFAULT 1,
  sort_order INT DEFAULT 0,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP
);

-- 规则条件表
CREATE TABLE IF NOT EXISTS rule_condition (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  rule_id BIGINT NOT NULL,
  group_id BIGINT NOT NULL,
  field_code VARCHAR(100) NOT NULL,
  operator VARCHAR(20) NOT NULL,
  field_value CLOB,
  value_type VARCHAR(20) DEFAULT 'CONSTANT',
  sort_order INT DEFAULT 0,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP
);

-- 规则动作表
CREATE TABLE IF NOT EXISTS rule_action (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  rule_id BIGINT NOT NULL,
  action_type VARCHAR(50) NOT NULL,
  action_code VARCHAR(100),
  action_params CLOB,
  sort_order INT DEFAULT 0,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP
);

-- 规则执行日志表
CREATE TABLE IF NOT EXISTS rule_execution_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  trace_id VARCHAR(64) NOT NULL,
  rule_id BIGINT,
  rule_code VARCHAR(100),
  scene_code VARCHAR(50) NOT NULL,
  business_id VARCHAR(100),
  input_data CLOB,
  output_data CLOB,
  execution_result VARCHAR(20) NOT NULL,
  hit_rules CLOB,
  execution_time INT DEFAULT 0,
  error_message CLOB,
  execute_by VARCHAR(50),
  execute_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  server_ip VARCHAR(50)
);

-- 规则版本表
CREATE TABLE IF NOT EXISTS rule_version (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  rule_id BIGINT NOT NULL,
  version_no INT NOT NULL,
  rule_content CLOB,
  change_log VARCHAR(500),
  is_current TINYINT DEFAULT 0,
  create_by VARCHAR(50) DEFAULT 'system',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
