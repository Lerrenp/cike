import { defineStore } from 'pinia'
import { authApi } from '@/api/auth'
import { userApi } from '@/api/user'

// 本地存储 key
const TOKEN_KEY = 'cike_token'
const USER_KEY = 'cike_user'

function readStoredUser() {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY) || 'null')
  } catch {
    localStorage.removeItem(USER_KEY)
    return null
  }
}
export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    userInfo: readStoredUser()
  }),
  getters: {
    isLogin: (state) => !!state.token || !!state.userInfo,
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
    // 应用启动时验证 cookie/Bearer，并修复协议切换后的登录态
    async restoreSession() {
      try {
        const res = await authApi.session({ skipAuthRedirect: true })
        const user = res?.data
        if (user) {
          this.userInfo = user
          localStorage.setItem(USER_KEY, JSON.stringify(user))
        }
        return !!user
      } catch {
        this.token = ''
        this.userInfo = null
        localStorage.removeItem(TOKEN_KEY)
        localStorage.removeItem(USER_KEY)
        return false
      }
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
