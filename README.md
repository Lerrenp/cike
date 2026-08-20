# 此刻 · 图文分享社区

「此刻」是一个支持图文笔记、话题、评论、点赞和收藏的全栈社区项目。前端使用 Material Design 3 风格，提供电脑端和移动端响应式界面；后端提供 JWT 与 HTTPS Cookie 会话、短信验证码注册登录以及完整的笔记互动 API。

**线上体验：** https://majiawebtest.dpdns.org/

## 功能

- 手机号短信验证码注册，手机号和密码登录，退出登录
- HTTPS 下的 JWT Bearer Token 与 HttpOnly Cookie 会话恢复
- 笔记首页、分类筛选、分页加载和笔记详情
- 发布图文笔记，选择话题和公开范围；浏览器端压缩图片后上传
- 点赞、收藏、评论和二级回复
- 个人资料编辑、作品、收藏和点赞列表
- 桌面端与移动端响应式布局，基于 Vuetify 3 的 Material Design 3 主题

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 前端 | Vue 3、Vite 5、Vue Router、Pinia、Axios、Vuetify 3、Material Design Icons |
| 后端 | Java 17、Spring Boot 3.2、MyBatis-Plus、Spring Validation、JJWT |
| 数据 | MySQL 8 / MariaDB、Redis |
| 部署 | nginx、systemd、HTTPS |

## 项目结构

```text
cike/
├── frontend/                  # Vue 3 前端
│   ├── src/api/               # API 客户端
│   ├── src/components/        # 通用组件
│   ├── src/views/             # 首页、登录、发布、详情、个人中心
│   └── src/router/            # 路由与鉴权守卫
├── backend/                   # Spring Boot 后端
│   └── src/main/java/com/cike/
│       ├── controller/        # 认证、用户、笔记、互动、话题接口
│       ├── service/           # 业务逻辑
│       └── config/            # Web、鉴权与异常处理配置
├── docs/
│   ├── init.sql               # 数据库建表脚本
│   ├── seed.sql               # 演示数据
│   ├── API接口文档.md          # 接口说明
│   └── 「此刻」图文分享社区产品需求文档（PRD）.md
└── LICENSE                    # MIT License
```

## 本地运行

### 1. 准备环境

- JDK 17+
- Maven 3.9+
- Node.js 18+
- MySQL 8.0+ 或 MariaDB
- Redis 6+

### 2. 初始化数据库

创建数据库和表：

```bash
mysql -u root -p < docs/init.sql
```

可选地写入演示数据：

```bash
mysql -u root -p cike < docs/seed.sql
```

在启动后端前，按本地环境修改 `backend/src/main/resources/application.properties` 中的数据源、Redis 和 JWT 配置。不要将生产环境密码或密钥提交到仓库。

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端默认监听 `http://localhost:8080/api/v1`。

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

Vite 开发服务器会输出本地访问地址。开发时须将 `/api/v1` 代理到后端，或让前端与 nginx 反向代理后的 API 使用同一来源；生产构建使用相对 API 地址 `/api/v1`。

构建生产静态资源：

```bash
cd frontend
npm run build
```

构建结果位于 `frontend/dist/`。

## API 与认证

完整接口、请求体和响应格式见 [API 接口文档](docs/API接口文档.md)。

- API 基础路径：`/api/v1`
- 注册时通过 `POST /auth/sms/code` 获取验证码；验证码在 Redis 中有效期为 120 秒
- 登录或注册成功后，客户端会同时保存返回的 JWT 并使用 HTTPS Cookie 恢复会话
- 公开读取接口包括首页、笔记详情和话题；发布、互动和个人中心接口需要认证

## 验证

```bash
cd backend
mvn test

cd ../frontend
npm run build
```

已部署环境可通过以下地址检查首页和 API：

```text
https://majiawebtest.dpdns.org/
https://majiawebtest.dpdns.org/api/v1/topics
```

## 许可证

本项目采用 [MIT License](LICENSE) 开源。
