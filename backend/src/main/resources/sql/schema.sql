-- =====================================================
-- 规则引擎数据库初始化脚本
-- 数据库版本: MySQL 8.0+
-- 字符集: utf8mb4
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `rule_engine` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `rule_engine`;

-- =====================================================
-- 1. 业务场景表 (business_scene)
-- =====================================================
DROP TABLE IF EXISTS `business_scene`;
CREATE TABLE `business_scene` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `scene_code` VARCHAR(50) NOT NULL COMMENT '场景编码',
  `scene_name` VARCHAR(100) NOT NULL COMMENT '场景名称',
  `scene_desc` VARCHAR(500) DEFAULT NULL COMMENT '场景描述',
  `adapter_class` VARCHAR(200) NOT NULL COMMENT '适配器类全限定名',
  `item_pattern` VARCHAR(50) DEFAULT NULL COMMENT '评估题目字段命名模式，如 q{n} 表示 q1,q2,...qN',
  `item_count` INT DEFAULT 0 COMMENT '评估题目数量，0表示动态数量（自动识别）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  `sort_order` INT DEFAULT 0 COMMENT '排序号',
  `create_by` VARCHAR(50) DEFAULT 'system' COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-否, 1-是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_scene_code` (`scene_code`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务场景表';

-- =====================================================
-- 2. 字段元数据表 (field_metadata)
-- =====================================================
DROP TABLE IF EXISTS `field_metadata`;
CREATE TABLE `field_metadata` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `scene_code` VARCHAR(50) NOT NULL COMMENT '所属场景编码',
  `field_code` VARCHAR(100) NOT NULL COMMENT '字段编码',
  `field_name` VARCHAR(100) NOT NULL COMMENT '字段中文名称',
  `field_type` VARCHAR(20) NOT NULL COMMENT '字段类型: String, Number, Date, Boolean',
  `data_source` VARCHAR(100) DEFAULT NULL COMMENT '数据来源说明',
  `supported_operators` VARCHAR(500) NOT NULL COMMENT '支持的操作符,逗号分隔',
  `value_range` VARCHAR(500) DEFAULT NULL COMMENT '值域说明',
  `category` VARCHAR(50) DEFAULT NULL COMMENT '字段分类',
  `sort_order` INT DEFAULT 0 COMMENT '排序号',
  `is_required` TINYINT DEFAULT 0 COMMENT '是否必填: 0-否, 1-是',
  `create_by` VARCHAR(50) DEFAULT 'system' COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-否, 1-是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_scene_field` (`scene_code`, `field_code`) USING BTREE,
  KEY `idx_scene_code` (`scene_code`) USING BTREE,
  KEY `idx_field_type` (`field_type`) USING BTREE,
  KEY `idx_category` (`category`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字段元数据表';

-- =====================================================
-- 3. 规则组表 (rule_group)
-- =====================================================
DROP TABLE IF EXISTS `rule_group`;
CREATE TABLE `rule_group` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `scene_code` VARCHAR(50) NOT NULL COMMENT '所属场景编码',
  `group_code` VARCHAR(100) NOT NULL COMMENT '规则组编码',
  `group_name` VARCHAR(100) NOT NULL COMMENT '规则组名称',
  `group_desc` VARCHAR(500) DEFAULT NULL COMMENT '规则组描述',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父级ID, 0表示根节点',
  `execution_mode` VARCHAR(20) DEFAULT 'ALL' COMMENT '执行模式: ALL-全部执行, FIRST-首个匹配',
  `sort_order` INT DEFAULT 0 COMMENT '排序号',
  `create_by` VARCHAR(50) DEFAULT 'system' COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-否, 1-是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_code` (`group_code`) USING BTREE,
  KEY `idx_scene_code` (`scene_code`) USING BTREE,
  KEY `idx_parent_id` (`parent_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则组表';

-- =====================================================
-- 4. 规则定义表 (rule_definition)
-- =====================================================
DROP TABLE IF EXISTS `rule_definition`;
CREATE TABLE `rule_definition` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `rule_code` VARCHAR(100) NOT NULL COMMENT '规则编码',
  `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
  `rule_desc` VARCHAR(500) DEFAULT NULL COMMENT '规则描述',
  `scene_code` VARCHAR(50) NOT NULL COMMENT '所属场景编码',
  `group_id` BIGINT DEFAULT NULL COMMENT '所属规则组ID',
  `rule_type` VARCHAR(20) NOT NULL DEFAULT 'CONDITION' COMMENT '规则类型: CONDITION, DECISION_TABLE, SCRIPT',
  `priority` INT DEFAULT 0 COMMENT '优先级: 数值越大优先级越高',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用, 2-草稿',
  `effective_start_time` DATETIME DEFAULT NULL COMMENT '生效开始时间',
  `effective_end_time` DATETIME DEFAULT NULL COMMENT '生效结束时间',
  `drl_content` TEXT COMMENT '编译后的DRL规则内容',
  `json_config` TEXT COMMENT 'JSON格式的规则配置',
  `version` INT DEFAULT 1 COMMENT '版本号',
  `create_by` VARCHAR(50) DEFAULT 'system' COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-否, 1-是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rule_code` (`rule_code`) USING BTREE,
  KEY `idx_scene_code` (`scene_code`) USING BTREE,
  KEY `idx_group_id` (`group_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_rule_type` (`rule_type`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则定义表';

-- =====================================================
-- 5. 条件组表 (condition_group)
-- =====================================================
DROP TABLE IF EXISTS `condition_group`;
CREATE TABLE `condition_group` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `rule_id` BIGINT NOT NULL COMMENT '所属规则ID',
  `parent_group_id` BIGINT DEFAULT 0 COMMENT '父条件组ID, 0表示根节点',
  `group_logic` VARCHAR(10) NOT NULL DEFAULT 'AND' COMMENT '组内逻辑: AND, OR',
  `group_level` INT DEFAULT 1 COMMENT '嵌套层级',
  `sort_order` INT DEFAULT 0 COMMENT '排序号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_rule_id` (`rule_id`) USING BTREE,
  KEY `idx_parent_group_id` (`parent_group_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='条件组表';

-- =====================================================
-- 6. 规则条件表 (rule_condition)
-- =====================================================
DROP TABLE IF EXISTS `rule_condition`;
CREATE TABLE `rule_condition` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `rule_id` BIGINT NOT NULL COMMENT '所属规则ID',
  `group_id` BIGINT NOT NULL COMMENT '所属条件组ID',
  `field_code` VARCHAR(100) NOT NULL COMMENT '字段编码',
  `operator` VARCHAR(20) NOT NULL COMMENT '操作符: EQ, IN, GT, LT, SUM, EXPRESSION等',
  `field_value` TEXT COMMENT '字段值(JSON格式)',
  `value_type` VARCHAR(20) NOT NULL DEFAULT 'CONSTANT' COMMENT '值类型: CONSTANT, FIELD, ARRAY',
  `aggregate_function` VARCHAR(20) DEFAULT NULL COMMENT '聚合函数: SUM, AVG, MAX, MIN, COUNT',
  `aggregate_fields` TEXT DEFAULT NULL COMMENT '聚合字段列表，JSON数组格式，如 ["q1","q2","q3"]',
  `expression` TEXT DEFAULT NULL COMMENT '计算表达式，用于 EXPRESSION 操作符，支持 SpEL 语法',
  `sort_order` INT DEFAULT 0 COMMENT '排序号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_rule_id` (`rule_id`) USING BTREE,
  KEY `idx_group_id` (`group_id`) USING BTREE,
  KEY `idx_field_code` (`field_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则条件表';

-- =====================================================
-- 7. 规则动作表 (rule_action)
-- =====================================================
DROP TABLE IF EXISTS `rule_action`;
CREATE TABLE `rule_action` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `rule_id` BIGINT NOT NULL COMMENT '所属规则ID',
  `action_type` VARCHAR(50) NOT NULL COMMENT '动作类型: RETURN, CALL_SERVICE, SEND_MESSAGE',
  `action_code` VARCHAR(100) DEFAULT NULL COMMENT '动作编码',
  `action_params` TEXT COMMENT '动作参数(JSON格式)',
  `sort_order` INT DEFAULT 0 COMMENT '执行顺序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_rule_id` (`rule_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则动作表';

-- =====================================================
-- 8. 规则执行日志表 (rule_execution_log)
-- =====================================================
DROP TABLE IF EXISTS `rule_execution_log`;
CREATE TABLE `rule_execution_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `trace_id` VARCHAR(64) NOT NULL COMMENT '链路追踪ID',
  `rule_id` BIGINT DEFAULT NULL COMMENT '规则ID',
  `rule_code` VARCHAR(100) DEFAULT NULL COMMENT '规则编码',
  `scene_code` VARCHAR(50) NOT NULL COMMENT '场景编码',
  `business_id` VARCHAR(100) DEFAULT NULL COMMENT '业务ID',
  `input_data` TEXT COMMENT '输入数据(JSON格式)',
  `output_data` TEXT COMMENT '输出数据(JSON格式)',
  `execution_result` VARCHAR(20) NOT NULL COMMENT '执行结果: HIT, MISS, ERROR',
  `hit_rules` TEXT COMMENT '命中的规则列表(JSON格式)',
  `execution_time` INT DEFAULT 0 COMMENT '执行耗时(毫秒)',
  `error_message` TEXT COMMENT '错误信息',
  `execute_by` VARCHAR(50) DEFAULT NULL COMMENT '执行人',
  `execute_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
  `server_ip` VARCHAR(50) DEFAULT NULL COMMENT '服务器IP',
  PRIMARY KEY (`id`),
  KEY `idx_trace_id` (`trace_id`) USING BTREE,
  KEY `idx_rule_id` (`rule_id`) USING BTREE,
  KEY `idx_rule_code` (`rule_code`) USING BTREE,
  KEY `idx_scene_code` (`scene_code`) USING BTREE,
  KEY `idx_business_id` (`business_id`) USING BTREE,
  KEY `idx_execute_time` (`execute_time`) USING BTREE,
  KEY `idx_execution_result` (`execution_result`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则执行日志表';

-- =====================================================
-- 9. 规则版本表 (rule_version)
-- =====================================================
DROP TABLE IF EXISTS `rule_version`;
CREATE TABLE `rule_version` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `rule_id` BIGINT NOT NULL COMMENT '规则ID',
  `version_no` INT NOT NULL COMMENT '版本号',
  `rule_content` LONGTEXT COMMENT '规则内容快照(JSON格式)',
  `change_log` VARCHAR(500) DEFAULT NULL COMMENT '变更说明',
  `is_current` TINYINT DEFAULT 0 COMMENT '是否当前版本: 0-否, 1-是',
  `create_by` VARCHAR(50) DEFAULT 'system' COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_rule_id` (`rule_id`) USING BTREE,
  UNIQUE KEY `uk_rule_version` (`rule_id`, `version_no`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则版本表';

-- =====================================================
-- 10. 评估分组映射表 (assessment_group_mapping)
-- 用于多维度体质评估等分组计算场景
-- =====================================================
DROP TABLE IF EXISTS `assessment_group_mapping`;
CREATE TABLE `assessment_group_mapping` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `scene_code` VARCHAR(50) NOT NULL COMMENT '所属场景编码',
  `group_code` VARCHAR(50) NOT NULL COMMENT '分组编码，如 qixu, yangxu',
  `group_name` VARCHAR(100) NOT NULL COMMENT '分组名称，如 气虚质',
  `item_list` VARCHAR(500) NOT NULL COMMENT '题目编号列表，JSON数组格式，如 [2,3,4,14]',
  `reverse_items` VARCHAR(500) DEFAULT NULL COMMENT '需要反向计分的题目编号，JSON数组格式',
  `sort_order` INT DEFAULT 0 COMMENT '排序号',
  `create_by` VARCHAR(50) DEFAULT 'system' COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-否, 1-是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_scene_group` (`scene_code`, `group_code`) USING BTREE,
  KEY `idx_scene_code` (`scene_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评估分组映射表';

-- =====================================================
-- 11. 性能优化索引
-- =====================================================

-- 规则定义表复合索引（用于列表查询）
CREATE INDEX idx_rule_scene_status ON rule_definition(scene_code, status, is_deleted) USING BTREE;
CREATE INDEX idx_rule_group_status ON rule_definition(group_id, status, is_deleted) USING BTREE;
CREATE INDEX idx_rule_priority ON rule_definition(scene_code, priority DESC) USING BTREE;

-- 规则条件表复合索引（用于规则加载）
CREATE INDEX idx_condition_rule_group ON rule_condition(rule_id, group_id, sort_order) USING BTREE;

-- 规则动作表复合索引
CREATE INDEX idx_action_rule_order ON rule_action(rule_id, sort_order) USING BTREE;

-- 执行日志表时间范围查询索引
CREATE INDEX idx_log_scene_time ON rule_execution_log(scene_code, execute_time) USING BTREE;
CREATE INDEX idx_log_result_time ON rule_execution_log(execution_result, execute_time) USING BTREE;

-- 条件组表查询索引
CREATE INDEX idx_condgroup_rule_parent ON condition_group(rule_id, parent_group_id) USING BTREE;
