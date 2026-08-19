# 「此刻」图文分享社区 - API 接口文档

**基础地址**：`http://localhost:8080/api/v1`
**数据格式**：JSON，`Content-Type: application/json`，字符编码 UTF-8
**鉴权方式**：Bearer Token（登录/注册后返回 `data.token`，需鉴权的接口在请求头 `Authorization: Bearer <token>`）
**通用返回格式**：
```json
{ "code": 200, "message": "ok", "data": { } }
```
- `code`：`200` 成功；`400` 参数错误；`401` 未登录/登录失效；`404` 资源不存在；`500` 服务器错误

---

## 一、认证模块（/api/v1/auth）

### 1. 发送短信验证码
- **POST** `/api/v1/auth/sms/code`
- **请求体**：`{ "phone": "13800000001", "scene": "register" }`
  - `scene`：`register` 注册 / `login` 登录
- **响应**：`{ "code": 200, "message": "发送成功", "data": { "code": "123456" } }`
  - 开发环境验证码明文返回在 `data.code`

### 2. 注册
- **POST** `/api/v1/auth/register`
- **请求体**：`{ "phone": "13800000001", "code": "123456", "nickname": "小明", "password": "123456" }`
- **响应**：`{ "code": 200, "message": "注册成功", "data": { "token": "...", "user": { "id":1, "phone":"...", "nickname":"...", "avatar":"", "bio":"" } } }`

### 3. 登录（手机号+密码）
- **POST** `/api/v1/auth/login`
- **请求体**：`{ "phone": "13800000001", "password": "123456" }`
- **响应**：`{ "code": 200, "message": "登录成功", "data": { "token": "...", "user": { "id":1, "phone":"...", "nickname":"...", "avatar":"", "bio":"" } } }`

### 4. 退出登录
- **POST** `/api/v1/auth/logout`
- **请求头**：`Authorization: Bearer <token>`
- **响应**：`{ "code": 200, "message": "退出成功", "data": null }`

---

## 二、用户模块（/api/v1/users）

### 5. 用户列表
- **GET** `/api/v1/users`
- **响应**：`{ "code":200, "message":"ok", "data": [ { "id":1,"phone":"...","nickname":"...","avatar":"","bio":"", "note_count":3,"like_total":10 } , ...] }`

### 6. 用户详情
- **GET** `/api/v1/users/{id}`
- **路径参数**：`id`（BIGINT）
- **响应**：`{ "code":200, "message":"ok", "data": { "id":1,"phone":"...","nickname":"...","avatar":"","bio":"","note_count":3,"like_total":10 } }`

### 7. 修改用户信息
- **PUT** `/api/v1/users/{id}`
- **请求头**：`Authorization: Bearer <token>`
- **请求体**：`{ "nickname":"新昵称", "avatar":"http://...", "bio":"个人简介" }`（字段可选，传哪个改哪个）
- **响应**：`{ "code":200, "message":"修改成功", "data": { "id":1,... } }`

### 8. 我的笔记列表
- **GET** `/api/v1/users/{id}/notes`
- **请求头**：`Authorization: Bearer <token>`
- **响应**：`{ "code":200, "message":"ok", "data": [ { note对象 }, ...] }`

### 9. 我赞过的笔记
- **GET** `/api/v1/users/{id}/likes`
- **请求头**：`Authorization: Bearer <token>`
- **响应**：`{ "code":200, "message":"ok", "data": [ { note对象 }, ...] }`

### 10. 我收藏的笔记
- **GET** `/api/v1/users/{id}/collects`
- **请求头**：`Authorization: Bearer <token>`
- **响应**：`{ "code":200, "message":"ok", "data": [ { note对象 }, ...] }`

---

## 三、笔记模块（/api/v1/notes）

### 11. 首页瀑布流笔记列表（分页/分类）
- **GET** `/api/v1/notes?page=1&size=10&category=recommend|latest|hot|美食|穿搭|风景|干货`
- **响应**：`{ "code":200,"message":"ok","data": { "records":[ note对象 ], "total": 20, "page":1, "size":10 } }`
- note对象：`{ "id":..,"userId":..,"title":"..","content":"..","coverUrl":"..","viewCount":..,"likeCount":..,"collectCount":..,"commentCount":..,"createTime":"..","author":{"id":..,"nickname":"..","avatar":".."} }`

### 12. 笔记详情
- **GET** `/api/v1/notes/{id}`
- **响应**：`{ "code":200,"message":"ok","data": { "note":{...}, "images":["url1","url2"], "topics":["#美食"], "isLiked":false, "isCollected":false, "comments":[...] } }`

### 13. 发布笔记
- **POST** `/api/v1/notes`
- **请求头**：`Authorization: Bearer <token>`
- **请求体**：`{ "title":"..","content":"..","images":["url1","url2"],"topics":["#美食"],"visible":1 }`
- **响应**：`{ "code":200,"message":"发布成功","data": { "id":.. } }`

### 14. 删除笔记（作者）
- **DELETE** `/api/v1/notes/{id}`
- **请求头**：`Authorization: Bearer <token>`
- **响应**：`{ "code":200,"message":"删除成功","data":null }`

---

## 四、互动模块（/api/v1）

### 15. 点赞 / 取消点赞
- **POST** `/api/v1/like` 请求体 `{ "noteId": 1 }`
- **DELETE** `/api/v1/like/{noteId}`
- **请求头**：`Authorization: Bearer <token>`
- **响应**：`{ "code":200,"message":"ok","data": { "liked":true, "likeCount": 6 } }`

### 16. 收藏 / 取消收藏
- **POST** `/api/v1/collect` 请求体 `{ "noteId": 1 }`
- **DELETE** `/api/v1/collect/{noteId}`
- **请求头**：`Authorization: Bearer <token>`
- **响应**：`{ "code":200,"message":"ok","data": { "collected":true, "collectCount": 3 } }`

### 17. 发表评论
- **POST** `/api/v1/comments` 请求体 `{ "noteId":1, "content":"..", "parentId":0, "replyUserId":0 }`
- **请求头**：`Authorization: Bearer <token>`
- **响应**：`{ "code":200,"message":"评论成功","data": { comment对象 } }`

### 18. 删除评论
- **DELETE** `/api/v1/comments/{id}`
- **请求头**：`Authorization: Bearer <token>`
- **响应**：`{ "code":200,"message":"删除成功","data":null }`

---

## 五、话题模块（/api/v1/topics）

### 19. 话题列表
- **GET** `/api/v1/topics`
- **响应**：`{ "code":200,"message":"ok","data":[ { "id":1,"topicName":"#美食","noteCount":5 }, ...] }`
