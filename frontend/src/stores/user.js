import { defineStore } from 'pinia'
import { authApi } from '@/api/auth'
import { userApi } from '@/api/user'

// 本地存储 key
const TOKEN_KEY = 'cike_token'
const USER_KEY = 'cike_user'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    userInfo: JSON.parse(localStorage.getItem(USER_KEY) || 'null')
  }),
  getters: {
    isLogin: (state) => !!state.token,
    userId: (state) => state.userInfo?.id || null,
    isSelf: (state) => (id) => state.userInfo?.id === id
  },
  actions: {
    setAuth(token, user) {
      this.token = token
      this.userInfo = user
      localStorage.setItem(TOKEN_KEY, token)
      localStorage.setItem(USER_KEY, JSON.stringify(user))
    },
    // 登录（手机号+密码）
    async login(payload) {
      const res = await authApi.login(payload)
      const { token, user } = res.data
      this.setAuth(token, user)
      return res
    },
    // 注册
    async register(payload) {
      const res = await authApi.register(payload)
      const { token, user } = res.data
      this.setAuth(token, user)
      return res
    },
    // 更新本地用户信息（修改资料后）
    updateUserInfo(patch) {
      this.userInfo = { ...this.userInfo, ...patch }
      localStorage.setItem(USER_KEY, JSON.stringify(this.userInfo))
    },
    // 刷新用户详情
    async refreshUser() {
      if (!this.userId) return
      try {
        const res = await userApi.detail(this.userId)
        this.updateUserInfo(res.data)
      } catch (e) {
        // 刷新失败不阻断页面
      }
    },
    // 退出登录
    async logout() {
      try {
        await authApi.logout()
      } catch (e) {
        // 后端退出失败也照常清理本地
      }
      this.token = ''
      this.userInfo = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    }
  }
})
