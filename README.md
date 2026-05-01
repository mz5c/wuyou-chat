# 友聊 AI 问答服务

基于 Spring Boot 的 AI 问答服务平台，提供用户认证、AI 对话、对话历史管理等 RESTful API。

## 技术栈

- **后端**: Spring Boot 2.7 + Spring Security + MyBatis Plus
- **前端**: React 18 + TypeScript + Vite
- **数据库**: MySQL + Redis
- **AI**: 兼容 OpenAI 格式的 API 接入（支持 SSE 流式响应）
- **文档**: SpringDoc OpenAPI (Swagger)
- **工具**: Hutool, JWT (jjwt), Lombok, react-markdown

## 项目结构

```
wuyou-chat/
├── pom.xml                         # Maven 父工程
├── wuyou-chat-api/                 # 接口定义层
│   ├── dto/                        # 数据传输对象
│   ├── enums/                      # 枚举（RoleType 等）
│   └── service/                    # 服务接口
├── wuyou-chat-service/             # 服务实现层
│   ├── config/                     # 配置（Security、CORS、JWT、Swagger）
│   ├── controller/                 # 控制器
│   ├── entity/                     # 实体类
│   ├── mapper/                     # MyBatis Mapper
│   ├── service/impl/               # 服务实现
│   ├── common/                     # 工具类
│   └── exception/                  # 异常处理
└── wuyou-chat-frontend/            # 前端（React SPA）
    ├── src/
    │   ├── components/             # UI 组件（侧栏、聊天、角色选择器、Markdown）
    │   ├── hooks/                  # React Hooks（useSSE、useSessions）
    │   ├── services/               # API 服务层（REST + SSE）
    │   ├── types/                  # TypeScript 类型定义
    │   └── styles/                 # 全局样式
    ├── vite.config.ts
    └── package.json
```

## 快速开始

### 1. 环境要求

- JDK 11+
- Maven 3.6+
- MySQL 8.0+
- Redis

### 2. 配置环境变量

复制 `.env.example` 为 `.env`，填写配置：

```bash
cp .env.example .env
```

将 `.env` 中的占位值替换为实际配置：

| 变量 | 说明 |
|------|------|
| `DB_PASSWORD` | MySQL 密码 |
| `REDIS_PASSWORD` | Redis 密码（无密码则留空） |
| `JWT_SECRET` | JWT 签名密钥（至少 32 字符） |
| `AI_API_KEY` | AI 服务的 API Key |

### 3. 初始化数据库

```bash
mysql -u root -p < wuyou-chat-service/src/main/resources/sql/schema.sql
```

### 4. 启动服务

#### 启动后端

```bash
export $(grep -v '^#' .env | xargs)
mvn clean install -DskipTests
mvn -pl wuyou-chat-service spring-boot:run
```

后端启动后访问：API 文档 http://localhost:8080/swagger-ui.html

#### 启动前端

```bash
cd wuyou-chat-frontend
npm install     # 首次运行需要安装依赖
npm run dev
```

前端启动后访问：http://localhost:5173（开发模式，自动代理 /api 到后端）

> 前端需要在 `wuyou-chat-frontend/` 目录下执行，且后端必须先启动。

## API 概览

| 路径 | 方法 | 说明 | 认证 |
|------|------|------|------|
| `/api/auth/register` | POST | 用户注册 | 否 |
| `/api/auth/login` | POST | 用户登录 | 否 |
| `/api/auth/refresh` | POST | 刷新令牌 | 否 |
| `/api/chat/ask` | POST | AI 问答 | 是 |
| `/api/chat/ask/stream` | POST | 流式 AI 问答（SSE） | 是 |
| `/api/chat/history` | GET | 对话历史 | 是 |
| `/api/chat/record/list/{sessionId}` | GET | 按会话查询记录 | 是 |
| `/api/chat/record/{id}` | GET | 单条对话记录 | 是 |
| `/api/chat/record/{id}` | DELETE | 删除对话记录 | 是 |
| `/api/session/create` | POST | 创建会话 | 是 |
| `/api/session/list` | GET | 获取会话列表 | 是 |
| `/api/session/{id}` | GET | 获取会话详情 | 是 |
| `/api/session/{id}/rename` | PUT | 重命名会话 | 是 |
| `/api/session/{id}/role` | PUT | 切换会话角色 | 是 |
| `/api/session/{id}` | DELETE | 删除会话 | 是 |
| `/api/profile/update` | PUT | 更新资料 | 是 |
| `/api/profile/change-password` | POST | 修改密码 | 是 |
| `/api/user/info` | GET | 用户信息 | 是 |

认证方式：`Authorization: Bearer <access_token>`

## API 测试方法

以下使用 `curl` 测试所有接口，完整的测试流程：

### 1. 注册

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456","email":"test@example.com"}'
```

### 2. 登录（获取 Token）

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}'
```

返回示例：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 172800,
    "userId": 1,
    "username": "testuser",
    "nickname": "testuser",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

保存返回的 `accessToken`，后续请求使用 `TOKEN` 环境变量：

```bash
export TOKEN="eyJhbGciOiJIUzI1NiJ9..."
```

### 3. 会话管理

**创建会话：**

```bash
curl -X POST http://localhost:8080/api/session/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{}'
```

支持指定标题和角色：
```bash
curl -X POST http://localhost:8080/api/session/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"我的会话","roleType":"TRANSLATOR"}'
```

可选角色类型：`GENERAL`（通用助手）、`TRANSLATOR`（翻译助手）、`CODE_REVIEW`（代码审查）、`WRITER`（写作助手）

**获取会话列表：**

```bash
curl http://localhost:8080/api/session/list \
  -H "Authorization: Bearer $TOKEN"
```

**获取会话详情：**

```bash
curl http://localhost:8080/api/session/1 \
  -H "Authorization: Bearer $TOKEN"
```

**重命名会话：**

```bash
curl -X PUT http://localhost:8080/api/session/1/rename \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"新标题"}'
```

**切换角色：**

```bash
curl -X PUT http://localhost:8080/api/session/1/role \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"CODE_REVIEW"}'
```

**删除会话：**

```bash
curl -X DELETE http://localhost:8080/api/session/1 \
  -H "Authorization: Bearer $TOKEN"
```

### 4. AI 问答

**非流式问答（普通模式）：**

```bash
curl -X POST http://localhost:8080/api/chat/ask \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"question":"你好，请介绍一下你自己"}'
```

**流式问答（SSE 模式，推荐）：**

使用 `curl` 的 `-N`（no-buffer）参数实时显示流式输出：

```bash
curl -N -X POST http://localhost:8080/api/chat/ask/stream \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"sessionId":1,"message":"你好，请介绍一下你自己"}'
```

流式响应格式（Server-Sent Events）：
```
event: message
data: {"content":"你好","type":"text"}

event: message
data: {"content":"！我是","type":"text"}

event: message
data: {"content":"AI 助手","type":"text"}

event: done
data: [DONE]

```

### 5. 聊天记录

**按会话获取聊天记录：**

```bash
curl http://localhost:8080/api/chat/record/list/1 \
  -H "Authorization: Bearer $TOKEN"
```

**获取最近对话历史：**

```bash
curl "http://localhost:8080/api/chat/history?page=1&size=20" \
  -H "Authorization: Bearer $TOKEN"
```

**获取单条记录：**

```bash
curl http://localhost:8080/api/chat/record/1 \
  -H "Authorization: Bearer $TOKEN"
```

**删除记录：**

```bash
curl -X DELETE http://localhost:8080/api/chat/record/1 \
  -H "Authorization: Bearer $TOKEN"
```

### 6. 用户与资料

**获取用户信息：**

```bash
curl http://localhost:8080/api/user/info \
  -H "Authorization: Bearer $TOKEN"
```

**更新个人资料：**

```bash
curl -X PUT http://localhost:8080/api/profile/update \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"nickname":"新昵称","email":"new@example.com"}'
```

**修改密码：**

```bash
curl -X POST http://localhost:8080/api/profile/change-password \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"oldPassword":"123456","newPassword":"654321"}'
```

**刷新令牌：**

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"token":"<refresh_token>"}'
```
