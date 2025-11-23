import request from './request'

// 规则版本管理 API

// 获取规则版本列表
export function getVersionList(ruleId) {
  return request({
    url: `/api/rule-engine/version/list/${ruleId}`,
    method: 'get'
  })
}

// 获取版本详情
export function getVersionDetail(versionId) {
  return request({
    url: `/api/rule-engine/version/${versionId}`,
    method: 'get'
  })
}

// 比较两个版本
export function compareVersions(versionId1, versionId2) {
  return request({
    url: '/api/rule-engine/version/compare',
    method: 'get',
    params: { versionId1, versionId2 }
  })
}

// 回滚到指定版本
export function rollbackVersion(data) {
  return request({
    url: '/api/rule-engine/version/rollback',
    method: 'post',
    data
  })
}

// 创建版本快照
export function createVersionSnapshot(ruleId, description) {
  return request({
    url: '/api/rule-engine/version/snapshot',
    method: 'post',
    data: { ruleId, description }
  })
}

// 删除版本
export function deleteVersion(versionId) {
  return request({
    url: `/api/rule-engine/version/delete/${versionId}`,
    method: 'delete'
  })
}

// 设置当前版本
export function setCurrentVersion(versionId) {
  return request({
    url: `/api/rule-engine/version/set-current/${versionId}`,
    method: 'put'
  })
}
