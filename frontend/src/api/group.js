import request from './request'

// 规则组管理 API

// 获取规则组列表
export function getGroupList(sceneCode) {
  return request({
    url: '/api/rule-engine/group/list',
    method: 'get',
    params: { sceneCode }
  })
}

// 获取规则组树形结构
export function getGroupTree(sceneCode) {
  return request({
    url: '/api/rule-engine/group/tree',
    method: 'get',
    params: { sceneCode }
  })
}

// 分页查询规则组
export function getGroupPage(params) {
  return request({
    url: '/api/rule-engine/group/page',
    method: 'get',
    params
  })
}

// 获取规则组详情
export function getGroupDetail(id) {
  return request({
    url: `/api/rule-engine/group/${id}`,
    method: 'get'
  })
}

// 根据编码获取规则组
export function getGroupByCode(groupCode) {
  return request({
    url: `/api/rule-engine/group/code/${groupCode}`,
    method: 'get'
  })
}

// 创建规则组
export function createGroup(data) {
  return request({
    url: '/api/rule-engine/group/create',
    method: 'post',
    data
  })
}

// 更新规则组
export function updateGroup(id, data) {
  return request({
    url: `/api/rule-engine/group/update/${id}`,
    method: 'put',
    data
  })
}

// 删除规则组
export function deleteGroup(id) {
  return request({
    url: `/api/rule-engine/group/delete/${id}`,
    method: 'delete'
  })
}

// 更新规则组状态
export function updateGroupStatus(id, status) {
  return request({
    url: `/api/rule-engine/group/status/${id}`,
    method: 'put',
    params: { status }
  })
}
