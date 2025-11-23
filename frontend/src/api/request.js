import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 30000
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const { code, message } = response.data

    if (code === 200) {
      return response.data
    } else {
      ElMessage.error(message || '请求失败')
      return Promise.reject(new Error(message))
    }
  },
  (error) => {
    // 处理401未授权错误
    if (error.response && error.response.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      // 跳转到登录页
      window.location.href = '/login'
      return Promise.reject(error)
    }

    // 处理403权限不足错误
    if (error.response && error.response.status === 403) {
      ElMessage.error('权限不足，无法访问该资源')
      return Promise.reject(error)
    }

    // 其他错误
    ElMessage.error(error.response?.data?.message || error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default service

/**
 * 通用请求方法
 */
export const request = (config) => {
  return service(config)
}
