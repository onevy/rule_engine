import request from './request'

// 业务场景管理 API

// 获取场景列表
export function getSceneList() {
  return request({
    url: '/api/rule-engine/scene/list',
    method: 'get'
  })
}

// 分页查询场景
export function getScenePage(params) {
  return request({
    url: '/api/rule-engine/scene/page',
    method: 'get',
    params
  })
}

// 获取场景详情
export function getSceneDetail(id) {
  return request({
    url: `/api/rule-engine/scene/${id}`,
    method: 'get'
  })
}

// 根据场景编码获取
export function getSceneByCode(sceneCode) {
  return request({
    url: `/api/rule-engine/scene/code/${sceneCode}`,
    method: 'get'
  })
}

// 创建场景
export function createScene(data) {
  return request({
    url: '/api/rule-engine/scene/create',
    method: 'post',
    data
  })
}

// 更新场景
export function updateScene(id, data) {
  return request({
    url: `/api/rule-engine/scene/update/${id}`,
    method: 'put',
    data
  })
}

// 删除场景
export function deleteScene(id) {
  return request({
    url: `/api/rule-engine/scene/delete/${id}`,
    method: 'delete'
  })
}

// 更新场景状态
export function updateSceneStatus(id, status) {
  return request({
    url: `/api/rule-engine/scene/status/${id}`,
    method: 'put',
    params: { status }
  })
}

// 获取场景元数据
export function getSceneMetadata(sceneCode) {
  return request({
    url: `/api/rule-engine/metadata/${sceneCode}`,
    method: 'get'
  })
}

// 获取场景元数据（别名）
export const getMetadataByScene = getSceneMetadata

// 根据场景和分类获取元数据
export function getMetadataByCategory(sceneCode, category) {
  return request({
    url: `/api/rule-engine/metadata/${sceneCode}/category/${category}`,
    method: 'get'
  })
}

// 创建元数据
export function createMetadata(data) {
  return request({
    url: '/api/rule-engine/metadata/create',
    method: 'post',
    data
  })
}

// 更新元数据
export function updateMetadata(id, data) {
  return request({
    url: `/api/rule-engine/metadata/update/${id}`,
    method: 'put',
    data
  })
}

// 删除元数据
export function deleteMetadata(id) {
  return request({
    url: `/api/rule-engine/metadata/delete/${id}`,
    method: 'delete'
  })
}
