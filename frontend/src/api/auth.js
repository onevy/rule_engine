import request from './request'

// 认证相关 API

/**
 * 用户登录
 * @param {Object} data - 登录信息
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @returns {Promise} 返回token和用户信息
 */
export function login(data) {
  return request({
    url: '/api/auth/login',
    method: 'post',
    data
  })
}

/**
 * 用户登出
 * @returns {Promise}
 */
export function logout() {
  return request({
    url: '/api/auth/logout',
    method: 'post'
  })
}

/**
 * 获取当前用户信息
 * @returns {Promise}
 */
export function getUserInfo() {
  return request({
    url: '/api/auth/user-info',
    method: 'get'
  })
}

/**
 * 刷新token
 * @param {string} refreshToken - 刷新令牌
 * @returns {Promise}
 */
export function refreshToken(refreshToken) {
  return request({
    url: '/api/auth/refresh-token',
    method: 'post',
    data: { refreshToken }
  })
}

/**
 * 修改密码
 * @param {Object} data - 密码信息
 * @param {string} data.oldPassword - 旧密码
 * @param {string} data.newPassword - 新密码
 * @returns {Promise}
 */
export function changePassword(data) {
  return request({
    url: '/api/auth/change-password',
    method: 'put',
    data
  })
}
