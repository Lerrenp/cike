import request from './request'

// 互动模块（点赞/收藏/评论）
export const interactApi = {
  // 点赞
  like(noteId) {
    return request.post('/like', { noteId })
  },
  // 取消点赞
  unlike(noteId) {
    return request.delete(`/like/${noteId}`)
  },
  // 收藏
  collect(noteId) {
    return request.post('/collect', { noteId })
  },
  // 取消收藏
  uncollect(noteId) {
    return request.delete(`/collect/${noteId}`)
  },
  // 发表评论
  comment(data) {
    return request.post('/comments', data)
  },
  // 删除评论
  deleteComment(id) {
    return request.delete(`/comments/${id}`)
  }
}
