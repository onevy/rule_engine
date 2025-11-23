import { defineStore } from 'pinia'
import { ElMessage } from 'element-plus'

/**
 * 用户状态管理
 */
export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
  }),

  getters: {
    // 是否已登录
    isLoggedIn: (state) => !!state.token,

    // 获取用户名
    username: (state) => state.userInfo.username || '',

    // 获取用户角色
    roles: (state) => state.userInfo.roles || []
  },

  actions: {
    /**
     * 设置token
     */
    setToken(token) {
      this.token = token
      localStorage.setItem('token', token)
    },

    /**
     * 设置用户信息
     */
    setUserInfo(userInfo) {
      this.userInfo = userInfo
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    },

    /**
     * 登录
     */
    async login(loginForm) {
      try {
        // TODO: 调用登录API
        // const res = await loginApi(loginForm)
        // if (res.code === 200) {
        //   this.setToken(res.data.token)
        //   this.setUserInfo(res.data.userInfo)
        //   return true
        // }

        // 临时模拟登录成功
        console.log('Login with:', loginForm)
        this.setToken('mock-token-' + Date.now())
        this.setUserInfo({
          username: loginForm.username,
          roles: ['admin']
        })
        ElMessage.success('登录成功')
        return true
      } catch (error) {
        ElMessage.error('登录失败')
        return false
      }
    },

    /**
     * 登出
     */
    logout() {
      this.token = ''
      this.userInfo = {}
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      ElMessage.info('已退出登录')
    },

    /**
     * 检查权限
     */
    hasRole(role) {
      return this.roles.includes(role)
    },

    /**
     * 检查是否有任一权限
     */
    hasAnyRole(roles) {
      return roles.some((role) => this.roles.includes(role))
    },

    /**
     * 检查是否拥有所有权限
     */
    hasAllRoles(roles) {
      return roles.every((role) => this.roles.includes(role))
    }
  }
})
