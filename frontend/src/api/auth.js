import request from './request'

// 认证模块 /auth
export const authApi = {
  // 发送短信验证码
  smsCode(data) {
    return request.post('/auth/sms/code', data)
  },
  // 注册
  register(data) {
    return request.post('/auth/register', data)
  },
  // 登录（手机号+密码）
  login(data) {
    return request.post('/auth/login', data)
  },
  // 退出登录
  logout() {
    return request.post('/auth/logout')
  }
}
