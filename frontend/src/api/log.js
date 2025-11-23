import request from './request'

// 执行日志 API

// 查询执行日志列表
export function getLogList(params) {
  return request({
    url: '/api/rule-engine/log/list',
    method: 'get',
    params
  })
}

// 查询日志详情
export function getLogByTraceId(traceId) {
  return request({
    url: `/api/rule-engine/log/${traceId}`,
    method: 'get'
  })
}

// 根据业务ID查询日志
export function getLogByBusinessId(businessId) {
  return request({
    url: `/api/rule-engine/log/business/${businessId}`,
    method: 'get'
  })
}

// 根据场景编码查询日志
export function getLogBySceneCode(sceneCode) {
  return request({
    url: `/api/rule-engine/log/scene/${sceneCode}`,
    method: 'get'
  })
}

// 获取执行统计信息
export function getLogStatistics(params) {
  return request({
    url: '/api/rule-engine/log/statistics',
    method: 'get',
    params
  })
}

// 删除过期日志
export function deleteExpiredLogs(expireTime) {
  return request({
    url: '/api/rule-engine/log/expired',
    method: 'delete',
    params: { expireTime }
  })
}
