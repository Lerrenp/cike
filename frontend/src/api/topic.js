import request from './request'

// 话题模块 /topics
export const topicApi = {
  // 话题列表
  list() {
    return request.get('/topics')
  }
}
