# 规则引擎 API 接口设计文档

## 文档信息
- **文档版本**: v1.0
- **创建日期**: 2025-11-09
- **API版本**: v1
- **基础路径**: `/api/v1`

## 1. 接口设计原则

### 1.1 RESTful 设计规范
- 使用标准HTTP方法: GET(查询)、POST(创建)、PUT(更新)、DELETE(删除)
- URL使用名词复数形式,如 `/rules` 而非 `/rule`
- 使用HTTP状态码表示请求结果
- 统一的请求和响应格式
- 支持分页、排序、筛选

### 1.2 接口命名规范
- URL路径使用小写字母和连字符
- 资源嵌套不超过3层
- 使用查询参数进行过滤和分页

### 1.3 HTTP状态码规范
- `200 OK`: 请求成功
- `201 Created`: 资源创建成功
- `204 No Content`: 删除成功
- `400 Bad Request`: 请求参数错误
- `401 Unauthorized`: 未认证
- `403 Forbidden`: 无权限
- `404 Not Found`: 资源不存在
- `500 Internal Server Error`: 服务器内部错误

## 2. 通用约定

### 2.1 统一响应格式

#### 成功响应
```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1699520000000
}
```

#### 错误响应
```json
{
  "code": 400,
  "message": "请求参数错误",
  "errors": [
    {
      "field": "ruleName",
      "message": "规则名称不能为空"
    }
  ],
  "timestamp": 1699520000000
}
```

#### 分页响应
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [],
    "total": 100,
    "page": 1,
    "pageSize": 10,
    "totalPages": 10
  },
  "timestamp": 1699520000000
}
```

### 2.2 分页参数
- `page`: 页码,从1开始,默认1
- `pageSize`: 每页记录数,默认10,最大100
- `sortBy`: 排序字段
- `sortOrder`: 排序方式,`asc` 或 `desc`

### 2.3 请求头
```
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}
X-Request-ID: {唯一请求ID}
```

### 2.4 时间格式
- 日期时间: `yyyy-MM-dd HH:mm:ss`
- 时间戳: 毫秒级Unix时间戳

## 3. 认证授权接口

### 3.1 用户登录

**接口**: `POST /api/v1/auth/login`

**描述**: 用户登录获取JWT Token

**请求体**:
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "user": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      "roles": ["SUPER_ADMIN"]
    }
  },
  "timestamp": 1699520000000
}
```

### 3.2 用户登出

**接口**: `POST /api/v1/auth/logout`

**描述**: 用户登出,使Token失效

**请求头**: 需要JWT Token

**响应**:
```json
{
  "code": 200,
  "message": "登出成功",
  "data": null,
  "timestamp": 1699520000000
}
```

### 3.3 刷新Token

**接口**: `POST /api/v1/auth/refresh`

**描述**: 刷新JWT Token

**请求头**: 需要JWT Token

**响应**:
```json
{
  "code": 200,
  "message": "Token刷新成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 7200
  },
  "timestamp": 1699520000000
}
```

### 3.4 获取当前用户信息

**接口**: `GET /api/v1/auth/current-user`

**描述**: 获取当前登录用户信息

**请求头**: 需要JWT Token

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "系统管理员",
    "email": "admin@example.com",
    "phone": "13800138000",
    "department": "信息科",
    "roles": [
      {
        "roleCode": "SUPER_ADMIN",
        "roleName": "超级管理员"
      }
    ],
    "permissions": [
      "rule:create",
      "rule:update",
      "rule:delete",
      "rule:view"
    ]
  },
  "timestamp": 1699520000000
}
```

## 4. 业务场景接口

### 4.1 查询场景列表

**接口**: `GET /api/v1/scenes`

**描述**: 查询业务场景列表,支持分页和筛选

**查询参数**:
- `page`: 页码
- `pageSize`: 每页记录数
- `status`: 状态筛选(0-禁用,1-启用)
- `keyword`: 关键词搜索(场景名称)

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "sceneCode": "INFECTIOUS_DISEASE",
        "sceneName": "传染病上报",
        "sceneDesc": "传染病预警和强制上报规则",
        "adapterClass": "com.example.adapter.InfectiousDiseaseAdapter",
        "status": 1,
        "sortOrder": 1,
        "createTime": "2025-01-01 00:00:00"
      }
    ],
    "total": 3,
    "page": 1,
    "pageSize": 10,
    "totalPages": 1
  },
  "timestamp": 1699520000000
}
```

### 4.2 获取场景详情

**接口**: `GET /api/v1/scenes/{sceneCode}`

**描述**: 根据场景编码获取详情

**路径参数**:
- `sceneCode`: 场景编码

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "sceneCode": "INFECTIOUS_DISEASE",
    "sceneName": "传染病上报",
    "sceneDesc": "传染病预警和强制上报规则",
    "adapterClass": "com.example.adapter.InfectiousDiseaseAdapter",
    "status": 1,
    "sortOrder": 1,
    "createBy": "system",
    "createTime": "2025-01-01 00:00:00",
    "updateBy": null,
    "updateTime": null
  },
  "timestamp": 1699520000000
}
```

### 4.3 创建场景

**接口**: `POST /api/v1/scenes`

**描述**: 创建新的业务场景

**权限**: `scene:create`

**请求体**:
```json
{
  "sceneCode": "NEW_SCENE",
  "sceneName": "新场景",
  "sceneDesc": "场景描述",
  "adapterClass": "com.example.adapter.NewSceneAdapter",
  "status": 1,
  "sortOrder": 10
}
```

**响应**:
```json
{
  "code": 201,
  "message": "场景创建成功",
  "data": {
    "id": 4,
    "sceneCode": "NEW_SCENE",
    "sceneName": "新场景"
  },
  "timestamp": 1699520000000
}
```

### 4.4 更新场景

**接口**: `PUT /api/v1/scenes/{sceneCode}`

**描述**: 更新场景信息

**权限**: `scene:update`

**请求体**: 同创建场景

**响应**:
```json
{
  "code": 200,
  "message": "场景更新成功",
  "data": null,
  "timestamp": 1699520000000
}
```

### 4.5 删除场景

**接口**: `DELETE /api/v1/scenes/{sceneCode}`

**描述**: 删除场景(逻辑删除)

**权限**: `scene:delete`

**响应**:
```json
{
  "code": 204,
  "message": "场景删除成功",
  "data": null,
  "timestamp": 1699520000000
}
```

## 5. 字段元数据接口

### 5.1 查询字段列表

**接口**: `GET /api/v1/fields`

**描述**: 查询字段元数据列表

**查询参数**:
- `sceneCode`: 场景编码(必填)
- `category`: 字段分类
- `fieldType`: 字段类型
- `keyword`: 关键词搜索

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "sceneCode": "INFECTIOUS_DISEASE",
      "fieldCode": "diagnosis_code",
      "fieldName": "诊断编码",
      "fieldType": "String",
      "dataSource": "诊断数据",
      "supportedOperators": ["EQ", "IN", "NOT_IN", "LEFT_MATCH"],
      "valueRange": "ICD-10编码",
      "category": "诊断信息",
      "sortOrder": 1,
      "isRequired": 0
    }
  ],
  "timestamp": 1699520000000
}
```

### 5.2 获取字段详情

**接口**: `GET /api/v1/fields/{id}`

**描述**: 获取字段元数据详情

**响应**: 同查询字段列表单条记录

### 5.3 批量创建字段

**接口**: `POST /api/v1/fields/batch`

**描述**: 批量创建字段元数据

**权限**: `field:create`

**请求体**:
```json
{
  "sceneCode": "INFECTIOUS_DISEASE",
  "fields": [
    {
      "fieldCode": "new_field",
      "fieldName": "新字段",
      "fieldType": "String",
      "supportedOperators": ["EQ", "IN"],
      "category": "诊断信息"
    }
  ]
}
```

**响应**:
```json
{
  "code": 201,
  "message": "字段创建成功",
  "data": {
    "successCount": 1,
    "failCount": 0
  },
  "timestamp": 1699520000000
}
```

### 5.4 更新字段

**接口**: `PUT /api/v1/fields/{id}`

**描述**: 更新字段元数据

**权限**: `field:update`

### 5.5 删除字段

**接口**: `DELETE /api/v1/fields/{id}`

**描述**: 删除字段元数据

**权限**: `field:delete`

## 6. 规则组接口

### 6.1 查询规则组树

**接口**: `GET /api/v1/rule-groups/tree`

**描述**: 获取规则组树形结构

**查询参数**:
- `sceneCode`: 场景编码(必填)

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "groupCode": "HIV_RULES",
      "groupName": "HIV相关规则",
      "parentId": 0,
      "children": [
        {
          "id": 2,
          "groupCode": "HIV_DIAGNOSIS",
          "groupName": "HIV诊断规则",
          "parentId": 1,
          "children": []
        }
      ]
    }
  ],
  "timestamp": 1699520000000
}
```

### 6.2 创建规则组

**接口**: `POST /api/v1/rule-groups`

**描述**: 创建规则组

**权限**: `rule-group:create`

**请求体**:
```json
{
  "sceneCode": "INFECTIOUS_DISEASE",
  "groupCode": "NEW_GROUP",
  "groupName": "新规则组",
  "groupDesc": "描述",
  "parentId": 0,
  "sortOrder": 1
}
```

### 6.3 更新规则组

**接口**: `PUT /api/v1/rule-groups/{groupCode}`

**权限**: `rule-group:update`

### 6.4 删除规则组

**接口**: `DELETE /api/v1/rule-groups/{groupCode}`

**权限**: `rule-group:delete`

## 7. 规则定义接口

### 7.1 查询规则列表

**接口**: `GET /api/v1/rules`

**描述**: 分页查询规则列表

**查询参数**:
- `page`: 页码
- `pageSize`: 每页记录数
- `sceneCode`: 场景编码
- `groupId`: 规则组ID
- `status`: 状态
- `ruleType`: 规则类型
- `keyword`: 关键词(规则名称/编码)

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "ruleCode": "HIV_ALERT_001",
        "ruleName": "HIV阳性预警",
        "ruleDesc": "HIV抗体阳性时触发预警",
        "sceneCode": "INFECTIOUS_DISEASE",
        "groupId": 1,
        "groupName": "HIV相关规则",
        "ruleType": "ALERT",
        "priority": 100,
        "status": 1,
        "effectiveStartTime": "2025-01-01 00:00:00",
        "effectiveEndTime": null,
        "version": 1,
        "createBy": "admin",
        "createTime": "2025-01-01 00:00:00",
        "updateTime": "2025-01-05 10:30:00"
      }
    ],
    "total": 50,
    "page": 1,
    "pageSize": 10,
    "totalPages": 5
  },
  "timestamp": 1699520000000
}
```

### 7.2 获取规则详情

**接口**: `GET /api/v1/rules/{ruleCode}`

**描述**: 获取规则完整配置

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "ruleCode": "HIV_ALERT_001",
    "ruleName": "HIV阳性预警",
    "ruleDesc": "HIV抗体阳性时触发预警",
    "sceneCode": "INFECTIOUS_DISEASE",
    "groupId": 1,
    "ruleType": "ALERT",
    "priority": 100,
    "status": 1,
    "effectiveStartTime": "2025-01-01 00:00:00",
    "effectiveEndTime": null,
    "jsonConfig": {
      "conditionGroups": [
        {
          "groupLogic": "AND",
          "conditions": [
            {
              "fieldCode": "lab_hiv_antibody",
              "operator": "EQ",
              "fieldValue": "阳性",
              "valueType": "CONSTANT"
            }
          ]
        }
      ],
      "actions": [
        {
          "actionType": "ALERT",
          "actionConfig": {
            "alertLevel": "HIGH",
            "alertMessage": "患者HIV抗体检测阳性,请及时上报"
          }
        }
      ]
    },
    "drlContent": "rule \"HIV_ALERT_001\"...",
    "version": 1,
    "createBy": "admin",
    "createTime": "2025-01-01 00:00:00"
  },
  "timestamp": 1699520000000
}
```

### 7.3 创建规则

**接口**: `POST /api/v1/rules`

**描述**: 创建新规则

**权限**: `rule:create`

**请求体**:
```json
{
  "ruleCode": "NEW_RULE_001",
  "ruleName": "新规则",
  "ruleDesc": "规则描述",
  "sceneCode": "INFECTIOUS_DISEASE",
  "groupId": 1,
  "ruleType": "ALERT",
  "priority": 50,
  "status": 2,
  "effectiveStartTime": "2025-01-01 00:00:00",
  "effectiveEndTime": null,
  "jsonConfig": {
    "conditionGroups": [
      {
        "groupLogic": "AND",
        "conditions": [
          {
            "fieldCode": "diagnosis_code",
            "operator": "IN",
            "fieldValue": ["B22.100", "B22.200"],
            "valueType": "ARRAY"
          }
        ]
      }
    ],
    "actions": [
      {
        "actionType": "ALERT",
        "actionConfig": {
          "alertLevel": "MEDIUM",
          "alertMessage": "检测到传染病诊断"
        }
      }
    ]
  }
}
```

**响应**:
```json
{
  "code": 201,
  "message": "规则创建成功",
  "data": {
    "id": 100,
    "ruleCode": "NEW_RULE_001"
  },
  "timestamp": 1699520000000
}
```

### 7.4 更新规则

**接口**: `PUT /api/v1/rules/{ruleCode}`

**描述**: 更新规则配置

**权限**: `rule:update`

**请求体**: 同创建规则

### 7.5 删除规则

**接口**: `DELETE /api/v1/rules/{ruleCode}`

**描述**: 删除规则(逻辑删除)

**权限**: `rule:delete`

### 7.6 启用/禁用规则

**接口**: `PATCH /api/v1/rules/{ruleCode}/status`

**描述**: 切换规则启用状态

**权限**: `rule:update`

**请求体**:
```json
{
  "status": 1
}
```

**响应**:
```json
{
  "code": 200,
  "message": "规则状态更新成功",
  "data": null,
  "timestamp": 1699520000000
}
```

### 7.7 编译规则

**接口**: `POST /api/v1/rules/{ruleCode}/compile`

**描述**: 编译规则JSON配置为DRL

**权限**: `rule:update`

**响应**:
```json
{
  "code": 200,
  "message": "规则编译成功",
  "data": {
    "drlContent": "rule \"HIV_ALERT_001\"\nwhen\n    $data: Map(this[\"lab_hiv_antibody\"] == \"阳性\")\nthen\n    // 触发预警\nend"
  },
  "timestamp": 1699520000000
}
```

### 7.8 校验规则

**接口**: `POST /api/v1/rules/validate`

**描述**: 校验规则配置是否正确

**请求体**: 规则JSON配置

**响应**:
```json
{
  "code": 200,
  "message": "规则校验通过",
  "data": {
    "valid": true,
    "errors": []
  },
  "timestamp": 1699520000000
}
```

### 7.9 复制规则

**接口**: `POST /api/v1/rules/{ruleCode}/copy`

**描述**: 复制规则

**权限**: `rule:create`

**请求体**:
```json
{
  "newRuleCode": "COPIED_RULE_001",
  "newRuleName": "复制的规则"
}
```

## 8. 规则执行接口

### 8.1 执行规则

**接口**: `POST /api/v1/rule-engine/execute`

**描述**: 执行规则引擎

**权限**: `rule:execute`

**请求体**:
```json
{
  "sceneCode": "INFECTIOUS_DISEASE",
  "businessId": "PATIENT_12345",
  "inputData": {
    "diagnosis_code": "B22.100",
    "diagnosis_name": "HIV病(人类免疫缺陷病毒病)伴卡波西肉瘤",
    "lab_hiv_antibody": "阳性",
    "patient_age": 35,
    "admission_date": "2025-01-10"
  }
}
```

**响应**:
```json
{
  "code": 200,
  "message": "规则执行成功",
  "data": {
    "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "executionResult": "HIT",
    "executionTime": 45,
    "hitRules": [
      {
        "ruleCode": "HIV_ALERT_001",
        "ruleName": "HIV阳性预警",
        "ruleType": "ALERT",
        "priority": 100,
        "actions": [
          {
            "actionType": "ALERT",
            "actionResult": {
              "alertLevel": "HIGH",
              "alertMessage": "患者HIV抗体检测阳性,请及时上报"
            }
          }
        ]
      }
    ]
  },
  "timestamp": 1699520000000
}
```

### 8.2 批量执行规则

**接口**: `POST /api/v1/rule-engine/batch-execute`

**描述**: 批量执行规则

**权限**: `rule:execute`

**请求体**:
```json
{
  "sceneCode": "INFECTIOUS_DISEASE",
  "dataList": [
    {
      "businessId": "PATIENT_12345",
      "inputData": {}
    },
    {
      "businessId": "PATIENT_67890",
      "inputData": {}
    }
  ]
}
```

**响应**:
```json
{
  "code": 200,
  "message": "批量执行完成",
  "data": {
    "totalCount": 2,
    "successCount": 2,
    "failCount": 0,
    "results": [
      {
        "businessId": "PATIENT_12345",
        "traceId": "...",
        "executionResult": "HIT"
      }
    ]
  },
  "timestamp": 1699520000000
}
```

### 8.3 测试规则

**接口**: `POST /api/v1/rule-engine/test`

**描述**: 测试规则(不保存执行日志)

**权限**: `rule:test`

**请求体**:
```json
{
  "ruleCode": "HIV_ALERT_001",
  "inputData": {
    "lab_hiv_antibody": "阳性"
  }
}
```

**响应**: 同执行规则

## 9. 规则执行日志接口

### 9.1 查询执行日志

**接口**: `GET /api/v1/execution-logs`

**描述**: 分页查询规则执行日志

**查询参数**:
- `page`: 页码
- `pageSize`: 每页记录数
- `sceneCode`: 场景编码
- `ruleCode`: 规则编码
- `businessId`: 业务ID
- `executionResult`: 执行结果(HIT/MISS/ERROR)
- `startTime`: 开始时间
- `endTime`: 结束时间

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1000,
        "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
        "ruleId": 1,
        "ruleCode": "HIV_ALERT_001",
        "sceneCode": "INFECTIOUS_DISEASE",
        "businessId": "PATIENT_12345",
        "executionResult": "HIT",
        "executionTime": 45,
        "executeBy": "admin",
        "executeTime": "2025-01-10 14:30:00",
        "serverIp": "192.168.1.100"
      }
    ],
    "total": 1000,
    "page": 1,
    "pageSize": 10,
    "totalPages": 100
  },
  "timestamp": 1699520000000
}
```

### 9.2 获取日志详情

**接口**: `GET /api/v1/execution-logs/{id}`

**描述**: 获取执行日志详情

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1000,
    "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "ruleId": 1,
    "ruleCode": "HIV_ALERT_001",
    "sceneCode": "INFECTIOUS_DISEASE",
    "businessId": "PATIENT_12345",
    "inputData": {
      "lab_hiv_antibody": "阳性"
    },
    "executionResult": "HIT",
    "hitRules": [
      {
        "ruleCode": "HIV_ALERT_001",
        "ruleName": "HIV阳性预警"
      }
    ],
    "executionTime": 45,
    "errorMessage": null,
    "executeBy": "admin",
    "executeTime": "2025-01-10 14:30:00",
    "serverIp": "192.168.1.100"
  },
  "timestamp": 1699520000000
}
```

### 9.3 导出执行日志

**接口**: `GET /api/v1/execution-logs/export`

**描述**: 导出执行日志为Excel

**查询参数**: 同查询执行日志

**响应**: Excel文件下载

### 9.4 统计分析

**接口**: `GET /api/v1/execution-logs/statistics`

**描述**: 规则执行统计分析

**查询参数**:
- `sceneCode`: 场景编码
- `startTime`: 开始时间
- `endTime`: 结束时间
- `dimension`: 统计维度(scene/rule/day)

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalCount": 10000,
    "hitCount": 1500,
    "missCount": 8000,
    "errorCount": 500,
    "avgExecutionTime": 52,
    "maxExecutionTime": 200,
    "byDay": [
      {
        "date": "2025-01-10",
        "totalCount": 1000,
        "hitCount": 150
      }
    ],
    "byRule": [
      {
        "ruleCode": "HIV_ALERT_001",
        "ruleName": "HIV阳性预警",
        "hitCount": 50,
        "avgExecutionTime": 45
      }
    ]
  },
  "timestamp": 1699520000000
}
```

## 10. 用户管理接口

### 10.1 查询用户列表

**接口**: `GET /api/v1/users`

**权限**: `user:view`

**查询参数**:
- `page`: 页码
- `pageSize`: 每页记录数
- `status`: 状态
- `keyword`: 关键词(用户名/真实姓名)
- `department`: 部门

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "username": "admin",
        "realName": "系统管理员",
        "email": "admin@example.com",
        "phone": "13800138000",
        "department": "信息科",
        "status": 1,
        "roles": ["SUPER_ADMIN"],
        "lastLoginTime": "2025-01-10 14:30:00",
        "createTime": "2025-01-01 00:00:00"
      }
    ],
    "total": 50,
    "page": 1,
    "pageSize": 10,
    "totalPages": 5
  },
  "timestamp": 1699520000000
}
```

### 10.2 获取用户详情

**接口**: `GET /api/v1/users/{id}`

**权限**: `user:view`

### 10.3 创建用户

**接口**: `POST /api/v1/users`

**权限**: `user:create`

**请求体**:
```json
{
  "username": "newuser",
  "password": "password123",
  "realName": "新用户",
  "email": "newuser@example.com",
  "phone": "13900139000",
  "department": "医务科",
  "status": 1,
  "roleIds": [2, 3]
}
```

### 10.4 更新用户

**接口**: `PUT /api/v1/users/{id}`

**权限**: `user:update`

### 10.5 删除用户

**接口**: `DELETE /api/v1/users/{id}`

**权限**: `user:delete`

### 10.6 重置密码

**接口**: `POST /api/v1/users/{id}/reset-password`

**权限**: `user:reset-password`

**请求体**:
```json
{
  "newPassword": "newpassword123"
}
```

### 10.7 修改密码

**接口**: `POST /api/v1/users/change-password`

**描述**: 用户修改自己的密码

**请求体**:
```json
{
  "oldPassword": "oldpassword",
  "newPassword": "newpassword123"
}
```

## 11. 角色权限接口

### 11.1 查询角色列表

**接口**: `GET /api/v1/roles`

**权限**: `role:view`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "roleCode": "SUPER_ADMIN",
      "roleName": "超级管理员",
      "roleDesc": "拥有所有权限",
      "status": 1,
      "createTime": "2025-01-01 00:00:00"
    }
  ],
  "timestamp": 1699520000000
}
```

### 11.2 获取角色详情

**接口**: `GET /api/v1/roles/{id}`

**权限**: `role:view`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 2,
    "roleCode": "RULE_ADMIN",
    "roleName": "规则管理员",
    "roleDesc": "管理规则配置",
    "status": 1,
    "permissions": [
      {
        "resourceType": "API",
        "resourceCode": "/api/v1/rules",
        "permission": "CREATE"
      },
      {
        "resourceType": "API",
        "resourceCode": "/api/v1/rules",
        "permission": "UPDATE"
      }
    ],
    "createTime": "2025-01-01 00:00:00"
  },
  "timestamp": 1699520000000
}
```

### 11.3 创建角色

**接口**: `POST /api/v1/roles`

**权限**: `role:create`

**请求体**:
```json
{
  "roleCode": "CUSTOM_ROLE",
  "roleName": "自定义角色",
  "roleDesc": "角色描述",
  "status": 1
}
```

### 11.4 更新角色

**接口**: `PUT /api/v1/roles/{id}`

**权限**: `role:update`

### 11.5 删除角色

**接口**: `DELETE /api/v1/roles/{id}`

**权限**: `role:delete`

### 11.6 配置角色权限

**接口**: `POST /api/v1/roles/{id}/permissions`

**权限**: `role:assign-permission`

**请求体**:
```json
{
  "permissions": [
    {
      "resourceType": "API",
      "resourceCode": "/api/v1/rules",
      "permission": "VIEW"
    },
    {
      "resourceType": "MENU",
      "resourceCode": "rule-management",
      "permission": "VIEW"
    }
  ]
}
```

### 11.7 获取权限树

**接口**: `GET /api/v1/permissions/tree`

**描述**: 获取完整的权限树结构

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "resourceType": "MENU",
      "resourceCode": "rule-management",
      "resourceName": "规则管理",
      "children": [
        {
          "resourceType": "API",
          "resourceCode": "/api/v1/rules",
          "resourceName": "规则接口",
          "permissions": ["VIEW", "CREATE", "UPDATE", "DELETE"]
        }
      ]
    }
  ],
  "timestamp": 1699520000000
}
```

## 12. 审计日志接口

### 12.1 查询审计日志

**接口**: `GET /api/v1/audit-logs`

**权限**: `audit:view`

**查询参数**:
- `page`: 页码
- `pageSize`: 每页记录数
- `userId`: 用户ID
- `username`: 用户名
- `operationType`: 操作类型
- `operationModule`: 操作模块
- `startTime`: 开始时间
- `endTime`: 结束时间

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 10000,
        "userId": 1,
        "username": "admin",
        "operationType": "CREATE",
        "operationModule": "RULE",
        "operationDesc": "创建规则: HIV_ALERT_001",
        "requestMethod": "POST",
        "requestUrl": "/api/v1/rules",
        "responseResult": "SUCCESS",
        "ipAddress": "192.168.1.100",
        "executionTime": 120,
        "createTime": "2025-01-10 14:30:00"
      }
    ],
    "total": 5000,
    "page": 1,
    "pageSize": 10,
    "totalPages": 500
  },
  "timestamp": 1699520000000
}
```

### 12.2 获取审计日志详情

**接口**: `GET /api/v1/audit-logs/{id}`

**权限**: `audit:view`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 10000,
    "userId": 1,
    "username": "admin",
    "operationType": "CREATE",
    "operationModule": "RULE",
    "operationDesc": "创建规则: HIV_ALERT_001",
    "requestMethod": "POST",
    "requestUrl": "/api/v1/rules",
    "requestParams": "{\"ruleCode\":\"HIV_ALERT_001\"}",
    "responseResult": "SUCCESS",
    "errorMessage": null,
    "ipAddress": "192.168.1.100",
    "userAgent": "Mozilla/5.0...",
    "executionTime": 120,
    "createTime": "2025-01-10 14:30:00"
  },
  "timestamp": 1699520000000
}
```

### 12.3 导出审计日志

**接口**: `GET /api/v1/audit-logs/export`

**权限**: `audit:export`

**查询参数**: 同查询审计日志

**响应**: Excel文件下载

## 13. 系统配置接口

### 13.1 获取系统配置

**接口**: `GET /api/v1/system/config`

**描述**: 获取系统配置信息

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "systemName": "医疗规则引擎",
    "version": "1.0.0",
    "maxPageSize": 100,
    "tokenExpireTime": 7200,
    "uploadMaxSize": 10485760,
    "supportedFileTypes": ["jpg", "png", "pdf", "xlsx"]
  },
  "timestamp": 1699520000000
}
```

### 13.2 更新系统配置

**接口**: `PUT /api/v1/system/config`

**权限**: `system:config`

### 13.3 健康检查

**接口**: `GET /api/v1/system/health`

**描述**: 系统健康检查

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "status": "UP",
    "components": {
      "database": {
        "status": "UP",
        "details": {
          "database": "MySQL",
          "validationQuery": "SELECT 1"
        }
      },
      "drools": {
        "status": "UP",
        "details": {
          "kieBaseCacheSize": 3
        }
      },
      "redis": {
        "status": "UP"
      }
    }
  },
  "timestamp": 1699520000000
}
```

### 13.4 系统信息

**接口**: `GET /api/v1/system/info`

**描述**: 获取系统运行信息

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "serverTime": "2025-01-10 14:30:00",
    "uptime": 86400,
    "jvm": {
      "maxMemory": 2048,
      "totalMemory": 1024,
      "freeMemory": 512,
      "usedMemory": 512
    },
    "os": {
      "name": "Linux",
      "version": "5.4.0"
    }
  },
  "timestamp": 1699520000000
}
```

## 14. 错误码说明

| 错误码 | 说明 | 处理建议 |
|-------|------|---------|
| 200 | 成功 | - |
| 201 | 创建成功 | - |
| 204 | 删除成功 | - |
| 400 | 请求参数错误 | 检查请求参数格式和必填项 |
| 401 | 未认证 | 重新登录获取Token |
| 403 | 无权限 | 联系管理员分配权限 |
| 404 | 资源不存在 | 检查资源ID或编码是否正确 |
| 409 | 资源冲突 | 资源已存在,检查唯一性约束 |
| 500 | 服务器内部错误 | 联系技术支持 |
| 1001 | 用户名或密码错误 | 检查登录凭证 |
| 1002 | Token已过期 | 重新登录 |
| 1003 | Token无效 | 重新登录 |
| 2001 | 场景不存在 | 检查场景编码 |
| 2002 | 场景已存在 | 使用不同的场景编码 |
| 3001 | 规则不存在 | 检查规则编码 |
| 3002 | 规则编译失败 | 检查规则配置语法 |
| 3003 | 规则执行失败 | 检查输入数据格式 |
| 4001 | 用户不存在 | 检查用户ID |
| 4002 | 用户名已存在 | 使用不同的用户名 |
| 4003 | 原密码错误 | 检查原密码 |

## 15. 接口限流策略

### 15.1 限流规则
- **默认限流**: 每个用户每分钟最多100次请求
- **规则执行接口**: 每秒最多50次请求
- **批量接口**: 每分钟最多10次请求
- **导出接口**: 每小时最多5次请求

### 15.2 限流响应

当触发限流时,返回 HTTP 429 Too Many Requests:

```json
{
  "code": 429,
  "message": "请求过于频繁,请稍后再试",
  "data": {
    "retryAfter": 60
  },
  "timestamp": 1699520000000
}
```

## 16. 接口版本管理

### 16.1 版本策略
- 当前版本: v1
- URL中包含版本号: `/api/v1/...`
- 向后兼容: v1版本持续维护6个月
- 新版本发布: 提前3个月通知

### 16.2 版本废弃
- 废弃接口在响应头中添加: `X-API-Deprecated: true`
- 响应中添加废弃警告信息

## 17. 接口测试

### 17.1 Postman Collection
提供完整的Postman接口测试集合,包含:
- 所有接口的请求示例
- 环境变量配置
- 自动化测试脚本

### 17.2 Swagger文档
访问地址: `http://host:port/swagger-ui.html`

提供:
- 接口在线测试
- 请求/响应模型
- 接口参数说明

## 附录

### A. 完整权限编码列表

```
# 场景管理
scene:view        - 查看场景
scene:create      - 创建场景
scene:update      - 更新场景
scene:delete      - 删除场景

# 字段管理
field:view        - 查看字段
field:create      - 创建字段
field:update      - 更新字段
field:delete      - 删除字段

# 规则组管理
rule-group:view   - 查看规则组
rule-group:create - 创建规则组
rule-group:update - 更新规则组
rule-group:delete - 删除规则组

# 规则管理
rule:view         - 查看规则
rule:create       - 创建规则
rule:update       - 更新规则
rule:delete       - 删除规则
rule:execute      - 执行规则
rule:test         - 测试规则

# 用户管理
user:view         - 查看用户
user:create       - 创建用户
user:update       - 更新用户
user:delete       - 删除用户
user:reset-password - 重置密码

# 角色管理
role:view         - 查看角色
role:create       - 创建角色
role:update       - 更新角色
role:delete       - 删除角色
role:assign-permission - 分配权限

# 审计管理
audit:view        - 查看审计日志
audit:export      - 导出审计日志

# 系统管理
system:config     - 系统配置
system:monitor    - 系统监控
```

### B. 数据库字段映射

前端使用驼峰命名,后端数据库使用下划线命名,需要进行转换:
- `sceneCode` ↔ `scene_code`
- `ruleName` ↔ `rule_name`
- `createTime` ↔ `create_time`

## 文档变更记录

| 版本 | 日期 | 修改人 | 修改内容 |
|-----|------|-------|---------|
| v1.0 | 2025-11-09 | Claude | 初始版本,完成所有核心接口设计 |
