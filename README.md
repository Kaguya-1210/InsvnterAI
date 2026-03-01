# InsvnterAI

基于 [SillyTavern](https://github.com/SillyTavern/SillyTavern) 二次开发的 AI 对话平台，采用前后端分离架构。

## 技术栈

| 模块 | 技术 |
|------|------|
| 前端 | Vue 3 + TypeScript + Vite |
| 后端 | Spring Boot 3 + Java 17 + Maven |
| 状态管理 | Pinia |
| 路由 | Vue Router |

## 项目结构

```
InsvnterAI/
├── insvnter-frontend/    # 前端项目
├── insvnter-backend/     # 后端项目
└── README.md
```

## 快速启动

### 前端

```bash
cd insvnter-frontend
npm install
npm run dev
# 访问 http://localhost:5173
```

### 后端

```bash
cd insvnter-backend
./mvnw spring-boot:run
# API 地址 http://localhost:8080
```

## License

MIT
