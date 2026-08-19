import request from './request'

// 笔记模块 /notes
export const noteApi = {
  // 首页瀑布流列表（分页/分类）
  list(params) {
    return request.get('/notes', { params })
  },
  // 笔记详情
  detail(id) {
    return request.get(`/notes/${id}`)
  },
  // 发布笔记
  publish(data) {
    return request.post('/notes', data)
  },
  // 删除笔记（作者）
  remove(id) {
    return request.delete(`/notes/${id}`)
  }
}
