import request from './request'

// 规则管理 API

// 查询规则列表
export function getRuleList(params) {
  return request({
    url: '/api/rule-engine/rule/list',
    method: 'get',
    params
  })
}

// 查询规则详情
export function getRuleDetail(ruleId) {
  return request({
    url: `/api/rule-engine/rule/${ruleId}`,
    method: 'get'
  })
}

// 根据规则编码查询
export function getRuleByCode(ruleCode) {
  return request({
    url: `/api/rule-engine/rule/code/${ruleCode}`,
    method: 'get'
  })
}

// 创建规则
export function createRule(data) {
  return request({
    url: '/api/rule-engine/rule/create',
    method: 'post',
    data
  })
}

// 更新规则
export function updateRule(ruleId, data) {
  return request({
    url: `/api/rule-engine/rule/update/${ruleId}`,
    method: 'put',
    data
  })
}

// 删除规则
export function deleteRule(ruleId) {
  return request({
    url: `/api/rule-engine/rule/delete/${ruleId}`,
    method: 'delete'
  })
}

// 复制规则
export function copyRule(ruleId) {
  return request({
    url: `/api/rule-engine/rule/copy/${ruleId}`,
    method: 'post'
  })
}

// 更新规则状态
export function updateRuleStatus(ruleId, status) {
  return request({
    url: `/api/rule-engine/rule/status/${ruleId}`,
    method: 'put',
    params: { status }
  })
}

// 根据场景编码查询规则列表
export function getRulesByScene(sceneCode) {
  return request({
    url: `/api/rule-engine/rule/scene/${sceneCode}`,
    method: 'get'
  })
}

// 根据规则组ID查询规则列表
export function getRulesByGroup(groupId) {
  return request({
    url: `/api/rule-engine/rule/group/${groupId}`,
    method: 'get'
  })
}

// 执行规则
export function executeRule(data) {
  return request({
    url: '/rule-engine/execute',
    method: 'post',
    data
  })
}

// 测试规则
export function testRule(data) {
  return request({
    url: '/rule-engine/test',
    method: 'post',
    data
  })
}

// 验证规则
export function validateRule(data) {
  return request({
    url: '/rule-engine/validate-rule',
    method: 'post',
    data
  })
}

// 重载规则缓存
export function reloadRuleCache(sceneCode) {
  return request({
    url: `/rule-engine/reload/${sceneCode}`,
    method: 'post'
  })
}

// 导出规则
export function exportRules(ruleIds, sceneCode) {
  return request({
    url: '/api/rule-engine/rule/export',
    method: 'post',
    params: sceneCode ? { sceneCode } : {},
    data: ruleIds || []
  })
}

// 导入规则
export function importRules(data) {
  return request({
    url: '/api/rule-engine/rule/import',
    method: 'post',
    data
  })
}

// 批量删除规则
export function batchDeleteRules(ruleIds) {
  return request({
    url: '/api/rule-engine/rule/batch',
    method: 'delete',
    data: ruleIds
  })
}

// 批量更新规则状态
export function batchUpdateRuleStatus(ruleIds, status) {
  return request({
    url: '/api/rule-engine/rule/batch/status',
    method: 'put',
    params: { status },
    data: ruleIds
  })
}
