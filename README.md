# 友聊 AI 问答服务

基于 Spring Boot + React 的 AI 问答服务平台，支持多轮对话、对话角色切换、多模型选择、SSE 流式响应。

## 技术栈

### 后端
- **框架**: Spring Boot 2.7 + Spring Security + MyBatis Plus
- **数据库**: MySQL 8.0 (InnoDB) + Redis
- **AI 接入**: 兼容 OpenAI 格式 API（支持 SSE 流式响应）
- **文档**: SpringDoc OpenAPI (Swagger)
- **工具**: Hutool, JWT (jjwt), Lombok

### 前端
- **框架**: React 19 + TypeScript + Vite
- **渲染**: react-markdown + rehype-katex（数学公式渲染）
- **代码高亮**: react-syntax-highlighter + prismjs
- **Lint**: ESLint + TypeScript ESLint

## 项目结构

```
wuyou-chat/
├── pom.xml                              # Maven 父工程（Java 11, Spring Boot 2.7.18）
├── .env.example                         # 环境变量模板
│
├── wuyou-chat-api/                      # 接口定义层（无 Spring 依赖）
│   └── src/main/java/com/wuyou/chat/api/
│       ├── dto/                         # DTO：ChatRequest, ChatResponse, LoginResponse 等
│       ├── enums/                       # RoleType（GENERAL/TRANSLATOR/CODE_REVIEW/WRITER）
│       └── service/                     # 服务接口：Auth, AiChat, Session, User, Profile, ModelConfig
│
├── wuyou-chat-service/                  # 服务实现层（含 Web 页面）
│   └── src/main/java/com/wuyou/chat/service/
│       ├── config/                      # SecurityConfig, CorsConfig, JwtAuthFilter, Swagger, Redis
│       ├── controller/                  # ChatController, AuthController, SessionController 等
│       ├── entity/                      # SysUser, ChatRecord, AiConfig, ChatSession 等实体
│       ├── mapper/                      # MyBatis Plus Mapper 接口
│       ├── service/impl/                # 业务实现；AI 流式响应用 WebFlux SSE 推送
│       ├── common/                      # JwtUtil（JJWT 签发/验签）
│       ├── exception/                   # BusinessException + GlobalExceptionHandler
│       └── dto/                         # 内部 DTO（ModelInfo 等）
│
└── wuyou-chat-frontend/                 # 前端 SPA
    ├── vite.config.ts                   # Vite 配置，开发时 /api 代理到 8080
    └── src/
        ├── components/
        │   ├── Chat/                    # ChatArea, ChatInput, MessageList, MessageBubble, ModelSelector, RoleSelector
        │   ├── Sidebar/                 # Sidebar, SessionItem（会话列表 + CRUD）
        │   ├── Markdown/                # MarkdownRenderer（语法高亮 + 公式）
        │   ├── LoginPage.tsx            # 登录/注册页
        │   └── Admin/                   # 模型管理后台页面
        ├── hooks/                       # useSSE（流式接收）, useSessions（会话状态管理）
        ├── services/                    # api.ts（REST 封装）, sse.ts（EventSource 封装）
        ├── types/                       # TypeScript 类型定义 + 角色枚举
        └── styles/                      # global.css
```

## 快速开始

### 1. 环境要求

- JDK 11+
- Maven 3.6+
- MySQL 8.0+
- Redis
- Node.js 18+

### 2. 配置环境变量

```bash
cp .env.example .env
```

填写 `.env` 中的配置：

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

#### 启动前端（开发模式）

```bash
cd wuyou-chat-frontend
npm install     # 首次运行需要安装依赖
npm run dev     # 启动 Vite 开发服务器
```

前端启动后：http://localhost:5173

> Vite 开发模式下，`/api/*` 请求自动代理到 `http://localhost:8080`。后端必须先启动。

### 5. 构建前端（生产模式）

```bash
cd wuyou-chat-frontend
npm run build   # TypeScript 编译 + Vite 构建
npm run preview # 预览构建产物
```

## AI 对话机制

### 流式响应（SSE）

- 前端通过 `EventSource` / `useSSE` hook 建立 SSE 连接
- 后端使用 Spring WebFlux 的 `SseEmitter` 逐字推送 AI 响应
- 支持 DeepSeek R1 风格的 `<reasoning>` / `<think>` 思考内容解析
- 未配置 API 时自动返回模拟响应

```
请求:
POST /api/chat/ask/stream
Authorization: Bearer <token>
{"sessionId":1,"message":"你好","modelId":1}

响应（SSE）:
event: message
data: {"content":"你好！","type":"text"}

event: message
data: {"content":"我是 AI 助手","type":"text"}

event: done
data: [DONE]
```

### 多模型支持

- 可在管理后台配置多个 AI 模型（不同供应商、不同模型名）
- 每个对话会话可切换使用的模型
- 模型通过 `ai_config` 表管理

### 角色预设

| 角色值 | 说明 |
|--------|------|
| `GENERAL` | 通用助手 |
| `TRANSLATOR` | 翻译助手 |
| `CODE_REVIEW` | 代码审查 |
| `WRITER` | 写作助手 |

## 认证流程

1. 用户注册/登录 → 后端生成 JWT（accessToken 2天 + refreshToken 14天）
2. 前端将 token 存入 `localStorage`，每次请求通过 `Authorization: Bearer <token>` 携带
3. `JwtAuthenticationFilter` 提取并验证令牌，认证信息写入 `SecurityContextHolder`
4. Token 过期（401）时触发 `auth:expired` 事件，页面跳转到登录页

## API 概览

| 路径 | 方法 | 说明 | 认证 |
|------|------|------|------|
| `/api/auth/register` | POST | 用户注册 | 否 |
| `/api/auth/login` | POST | 用户登录 | 否 |
| `/api/auth/refresh` | POST | 刷新令牌 | 否 |
| `/api/chat/ask` | POST | AI 问答（非流式） | 是 |
| `/api/chat/ask/stream` | POST | AI 问答（SSE 流式） | 是 |
| `/api/chat/history` | GET | 分页对话历史 | 是 |
| `/api/chat/record/list/{sessionId}` | GET | 按会话分页查询记录 | 是 |
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
| `/api/models/enabled` | GET | 可用模型列表 | 是 |
| `/api/admin/models` | GET/POST | 模型管理 | admin |

认证方式：`Authorization: Bearer <access_token>`

## 数据层

- **表**: `sys_user`（用户）、`chat_record`（聊天记录）、`chat_session`（会话）、`ai_config`（模型配置）
- ORM: MyBatis Plus `BaseMapper`，无手写 XML
- 软删除: `status = 0`（禁用/删除），非物理删除
- 分页: `chat/record/list/{sessionId}` 支持 `page` 和 `size` 参数

## 完整 API 测试

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

### 6. 模型管理（需 admin 角色）

**获取可用模型列表（用户侧）：**

```bash
curl http://localhost:8080/api/models/enabled \
  -H "Authorization: Bearer $TOKEN"
```

**获取全部模型（管理后台）：**

```bash
curl http://localhost:8080/api/admin/models \
  -H "Authorization: Bearer $TOKEN"
```

**添加模型：**

```bash
curl -X POST http://localhost:8080/api/admin/models \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"DeepSeek V3","provider":"deepseek","model":"deepseek-chat","sortOrder":1,"enabled":true}'
```

**更新模型：**

```bash
curl -X PUT http://localhost:8080/api/admin/models/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"DeepSeek V3","sortOrder":2,"enabled":true}'
```

**删除模型：**

```bash
curl -X DELETE http://localhost:8080/api/admin/models/1 \
  -H "Authorization: Bearer $TOKEN"
```

### 7. 用户与资料

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

## 开发

### 构建命令

```bash
# 完整后端构建（跳过测试）
mvn clean install -DskipTests

# 仅编译 api 模块
mvn -pl wuyou-chat-api compile

# 启动后端服务
mvn -pl wuyou-chat-service spring-boot:run

# 运行测试
mvn test

# 前端开发服务器
cd wuyou-chat-frontend && npm run dev

# 前端构建
cd wuyou-chat-frontend && npm run build

# 前端 lint
cd wuyou-chat-frontend && npm run lint
```

### 开发规范

- 控制器统一返回 `Result<T>` 封装（code/message/data）
- 业务异常抛出 `BusinessException`，由 `GlobalExceptionHandler` 统一处理
- 配置文件优先使用 `${}` 引用 `.env` 中的环境变量
- 服务接口定义在 `api` 模块，实现和配置在 `service` 模块
