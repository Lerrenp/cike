import request from './request'

// 用户模块 /users
export const userApi = {
  // 用户列表
  list() {
    return request.get('/users')
  },
  // 用户详情
  detail(id) {
    return request.get(`/users/${id}`)
  },
  // 修改用户信息
  update(id, data) {
    return request.put(`/users/${id}`, data)
  },
  // 我的笔记列表
  notes(id) {
    return request.get(`/users/${id}/notes`)
  },
  // 我赞过的笔记
  likes(id) {
    return request.get(`/users/${id}/likes`)
  },
  // 我收藏的笔记
  collects(id) {
    return request.get(`/users/${id}/collects`)
  }
}
