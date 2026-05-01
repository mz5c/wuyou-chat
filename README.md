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
