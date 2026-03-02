# InsvnterAI

基于 [SillyTavern](https://github.com/SillyTavern/SillyTavern) 二次开发的 AI 对话平台，采用前后端分离架构。

> 🤖 **协助开发**: [Antigravity AI](https://antigravity.google) — Google DeepMind 出品的 AI 编程助手

---

## 技术栈

### 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue 3 | 3.5 | 响应式 UI 框架 |
| TypeScript | 5.9 | 类型安全 |
| Vite | 7.3 | 构建工具 & 开发服务器 |
| Naive UI | 2.43 | 企业级 UI 组件库 |
| Pinia | 3.0 | 状态管理 |
| Vue Router | 5.0 | 路由管理 |
| Axios | 1.13 | HTTP 客户端 |

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.4.3 | Web 框架 |
| Java | 17 | 运行时 |
| Spring Security | — | 认证授权 |
| Spring Data JPA | — | ORM 持久层 |
| Spring Data Redis | — | 缓存 & 会话管理 |
| Spring Data MongoDB | — | 文档存储 |
| JJWT | 0.12.6 | JWT Token 签发 & 验证 |
| Kaptcha | 2.3.3 | 图形验证码生成 |
| Lombok | — | 代码简化 |
| Maven | — | 项目构建 |

### 数据库 & 基础设施

| 服务 | 镜像 | 端口 | 用途 |
|------|------|------|------|
| MySQL | 8.0 | 3306 | 用户数据 & 业务数据 |
| Redis | 7-alpine | 6379 | 验证码缓存 & JWT 黑名单 |
| MongoDB | 7 | 27017 | 配置预设 & AI 聊天记录 |
| Docker Compose | — | — | 一键编排数据库环境 |

---

## 项目结构

```
InsvnterAI/
├── insvnter-frontend/          # 前端 (Vue 3 + Vite)
│   ├── src/
│   │   ├── api/                # API 接口封装 (Axios)
│   │   ├── components/         # 通用组件 (AuthModal, AppNavbar)
│   │   ├── layouts/            # 布局组件 (AdminLayout)
│   │   ├── views/              # 页面 (LandingPage, Admin)
│   │   ├── stores/             # Pinia 状态 (auth, theme)
│   │   ├── router/             # 路由配置
│   │   └── utils/              # 工具函数
│   └── vite.config.ts          # Vite 配置 (含 API 代理)
│
├── insvnter-backend/           # 后端 (Spring Boot 3)
│   └── src/main/java/com/insvnter/ai/
│       ├── config/             # 配置类 (Security, Kaptcha, Redis)
│       ├── controller/         # 控制器 (Auth, Captcha, Admin, Health)
│       ├── service/            # 业务逻辑 (AuthService)
│       ├── security/           # 安全组件 (JWT Provider, Filter)
│       ├── model/              # 数据模型 & DTO
│       ├── repository/         # JPA 数据访问层
│       └── exception/          # 全局异常处理
│
├── docker/                     # Docker 配置文件
│   └── mariadb/                # MySQL 自定义配置
├── docker-compose.db.yml       # 数据库 Docker Compose
├── setup-db.bat                # 一键配置数据库环境
├── config-db.bat               # 交互式数据库参数配置
├── start-backend.bat           # 启动后端 (含 UTF-8 支持)
├── start-frontend.bat          # 启动前端
├── start-all.bat               # 一键启动全部服务
└── stop-local-db.bat           # 停止本地数据库服务
```

---

## 已完成功能

### ✅ 用户认证系统

- **JWT 无状态认证** — 基于 JJWT 签发 Token，SHA-256 密钥加固
- **JWT 过滤器** — 自动拦截请求，校验 Token 有效性
- **Token 黑名单** — 退出登录后通过 Redis 使 Token 失效
- **BCrypt 密码加密** — Spring Security 标准密码编码器
- **图形验证码** — Kaptcha 生成，Redis 存储（60 秒 TTL）
- **角色权限控制** — `ADMIN` / `USER` 角色，方法级安全注解

### ✅ API 接口

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/auth/register` | POST | 用户注册 | 公开 |
| `/api/auth/login` | POST | 用户登录 | 公开 |
| `/api/auth/logout` | POST | 退出登录 | 已认证 |
| `/api/auth/me` | GET | 获取当前用户信息 | 已认证 |
| `/api/captcha` | GET | 获取图形验证码 | 公开 |
| `/api/admin/users` | GET | 管理员用户列表 | ADMIN |
| `/api/health` | GET | 健康检查 | 公开 |

### ✅ 前端界面

- **Landing Page** — 现代化着陆页，暗色主题
- **登录/注册弹窗** — 带验证码的认证对话框
- **导航栏** — 响应式顶部导航，用户菜单下拉
- **管理后台布局** — AdminLayout 管理界面框架
- **主题系统** — Pinia 管理主题状态
- **API 拦截器** — 自动附加 JWT、401 自动清除、统一错误处理

### ✅ DevOps & 脚本

- **Docker Compose** — MySQL + Redis + MongoDB 一键编排
- **一键脚本** — `setup-db.bat` / `config-db.bat` / `start-all.bat`
- **环境变量** — `.env` 文件管理所有敏感配置
- **UTF-8 终端** — 后端中文输出零乱码

---

## 快速启动

### 环境要求

- **Node.js** ≥ 20.19 / ≥ 22.12
- **Java** 17+
- **Maven** 3.9+
- **Docker Desktop** (数据库服务)

### 1. 配置数据库

```bash
# 一键启动 MySQL + Redis + MongoDB
.\setup-db.bat

# (可选) 自定义数据库连接参数
.\config-db.bat
```

### 2. 启动后端

```bash
.\start-backend.bat
# API: http://localhost:8080
```

### 3. 启动前端

```bash
.\start-frontend.bat
# 访问: http://localhost:5173
```

### 4. 一键全部启动

```bash
.\start-all.bat
```

---

## 配置说明

所有配置通过环境变量或 `.env` 文件管理：

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `DB_HOST` | localhost | 数据库地址 |
| `DB_PORT` | 3306 | 数据库端口 |
| `DB_NAME` | insvnter_ai | 数据库名 |
| `DB_USERNAME` | root | 数据库用户 |
| `DB_PASSWORD` | root | 数据库密码 |
| `REDIS_HOST` | localhost | Redis 地址 |
| `REDIS_PORT` | 6379 | Redis 端口 |
| `MONGO_URI` | mongodb://localhost:27017/insvnter_ai | MongoDB URI |
| `JWT_SECRET` | (内置默认值) | JWT 签名密钥 |
| `JWT_EXPIRATION` | 86400000 | Token 过期时间 (ms) |
| `SERVER_PORT` | 8080 | 后端端口 |

---

## License

MIT
