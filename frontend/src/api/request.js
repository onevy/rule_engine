import axios from 'axios'
import { ElMessage, ElLoading } from 'element-plus'

// 全局loading实例
let loadingInstance = null
let loadingCount = 0

// 显示loading
const showLoading = () => {
  if (loadingCount === 0) {
    loadingInstance = ElLoading.service({
      lock: true,
      text: '加载中...',
      background: 'rgba(255, 255, 255, 0.7)'
    })
  }
  loadingCount++
}

// 隐藏loading
const hideLoading = () => {
  loadingCount--
  if (loadingCount <= 0) {
    loadingCount = 0
    loadingInstance?.close()
  }
}

// 错误码映射
const ERROR_MESSAGES = {
  400: '请求参数错误',
  401: '登录已过期，请重新登录',
  403: '权限不足，无法访问该资源',
  404: '请求的资源不存在',
  405: '请求方法不允许',
  408: '请求超时',
  500: '服务器内部错误',
  502: '网关错误',
  503: '服务暂不可用',
  504: '网关超时'
}

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 30000
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 显示loading（可通过config.showLoading控制）
    if (config.showLoading !== false) {
      showLoading()
    }

    // 从localStorage获取token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    hideLoading()
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    hideLoading()
    const { code, message } = response.data

    if (code === 200) {
      return response.data
    } else {
      ElMessage.error(message || '请求失败')
      return Promise.reject(new Error(message))
    }
  },
  (error) => {
    hideLoading()

    // 网络错误或请求超时
    if (!error.response) {
      if (error.code === 'ECONNABORTED') {
        ElMessage.error('请求超时，请检查网络连接')
      } else {
        ElMessage.error('网络连接失败，请检查网络')
      }
      return Promise.reject(error)
    }

    const status = error.response.status
    const serverMessage = error.response?.data?.message

    // 处理401未授权错误
    if (status === 401) {
      ElMessage.error(ERROR_MESSAGES[401])
      localStorage.removeItem('token')
      window.location.href = '/login'
      return Promise.reject(error)
    }

    // 使用错误码映射或服务器返回的消息
    const errorMessage = serverMessage || ERROR_MESSAGES[status] || `请求失败(${status})`
    ElMessage.error(errorMessage)

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
