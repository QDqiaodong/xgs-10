# 怀旧老物件图文分享站

主打老物件、怀旧藏品图文分享，用户展示旧物件实物照片，讲述物件来历、年代背景与个人回忆，以情怀内容交流为主，不涉及藏品交易、估价等商业行为。

## 📋 项目概览

| 模块 | 技术栈 | 端口 | 说明 |
|------|--------|------|------|
| 前端 | Vue3 + Vite + Pinia + Vue Router | 3008 | 怀旧风格 UI，图文发布、筛选、互动 |
| 后端 | Spring Boot 3.3 + JDK17 + JPA | 8088 | REST API，Redis 缓存 |
| MySQL | MySQL 8.0 | 3309 | 数据持久化，索引优化 |
| Redis | Redis 7 | 6380 | 热门帖子缓存，LRU 淘汰策略 |

## 🚀 快速启动

### 方式一：一键启动脚本（推荐）

```bash
./start.sh
```

启动成功后，终端会自动打印访问地址：
- 前端: http://localhost:3008
- 前端: http://127.0.0.1:3008

停止服务：
```bash
./stop.sh
```

### 方式二：Docker Compose 原生启动

```bash
# 构建并启动
docker compose up --build -d

# 查看容器状态
docker compose ps

# 查看日志
docker compose logs -f frontend

# 停止服务
docker compose down
```

## 📐 端口分配表

| 服务 | 宿主机端口 | 容器内部端口 | 绑定地址 | 说明 |
|------|-----------|-------------|----------|------|
| 前端 Nginx | 3008 | 80 | 127.0.0.1 | 静态资源服务 |
| 后端服务 | 8088 | 9038 | 127.0.0.1 | Spring Boot API |
| MySQL | 3309 | 3306 | 127.0.0.1 | 数据库 |
| Redis | 6380 | 6379 | 127.0.0.1 | 缓存 |

> 💡 所有端口统一配置在 `.env` 文件中，方便统一管理。

## 🏗️ 项目结构

```
xgs-10/
├── .env                    # 全局环境变量（端口、镜像仓库、数据库配置）
├── .gitignore
├── docker-compose.yml      # Docker 编排配置
├── start.sh                # 一键启动脚本
├── stop.sh                 # 停止脚本
├── README.md
├── backend/                # 后端 Spring Boot
│   ├── Dockerfile
│   ├── pom.xml
│   ├── settings.xml        # 腾讯云 Maven 镜像
│   ├── sql/
│   │   └── init.sql        # 数据库初始化（表结构 + 示例数据）
│   ├── uploads/            # 图片上传目录
│   └── src/main/
│       ├── java/com/nostalgia/
│       │   ├── NostalgiaApplication.java
│       │   ├── config/     # Redis + Web CORS 配置
│       │   ├── controller/ # REST API 控制器
│       │   ├── entity/     # JPA 实体
│       │   ├── repository/ # 数据访问层
│       │   └── service/    # 业务逻辑层
│       └── resources/
│           └── application.yml
└── frontend/               # 前端 Vue3
    ├── Dockerfile
    ├── nginx.conf          # Nginx 反向代理配置
    ├── package.json
    ├── vite.config.js      # Vite 配置（127.0.0.1 绑定 + strictPort）
    ├── index.html
    └── src/
        ├── main.js
        ├── App.vue
        ├── style.css
        ├── api/index.js    # Axios API 封装
        ├── router/         # 路由配置
        ├── utils/          # 图片压缩、会话管理
        └── views/          # 页面组件
            ├── Home.vue         # 首页（筛选 + 列表）
            ├── PostDetail.vue   # 帖子详情 + 留言
            ├── Publish.vue      # 发布表单
            └── Favorites.vue    # 我的收藏
```

## ⚙️ 环境变量配置 (.env)

```env
# =====================================================
#  【重要】国内用户请先配置 Docker Desktop 加速器
#  配置步骤：
#    1. Docker Desktop → Settings → Docker Engine
#    2. 添加: "registry-mirrors": ["https://你的加速器地址"]
#    3. 点击 Apply & Restart
#  免费加速器申请：阿里云 → 容器镜像服务 → 镜像加速器
# =====================================================

# 项目标识
PROJECT_NAME=nostalgia
CONTAINER_PREFIX=nostalgia

# 数据库配置
MYSQL_ROOT_PASSWORD=nostalgia123
MYSQL_DATABASE=nostalgia_db
MYSQL_USER=nostalgia
MYSQL_PASSWORD=nostalgia123
MYSQL_PORT=3309

# Redis 端口
REDIS_PORT=6380

# 服务端口
BACKEND_PORT=8088
FRONTEND_PORT=3008

# 时区
TZ=Asia/Shanghai
```

## ✨ 核心功能

### 1. 老物件内容发布
- 上传物件实拍图（前端 Canvas 自动压缩降噪）
- 填写物件名称、所属年代、背后故事与个人回忆
- 模块化表单设计，分步填写体验流畅

### 2. 多条件筛选
- 8 大内置品类：家用电器、影音设备、通讯工具、玩具玩偶、文具书籍、服饰配饰、食品饮料、日常用品
- 5 个年代标签：60年代、70年代、80年代、90年代、00年代
- 组合筛选，快速定位感兴趣的内容

### 3. 内容互动留言
- 在帖子下方交流相似经历
- 补充物件相关历史知识
- 匿名 / 昵称双模式

### 4. 内容收藏
- 一键收藏感兴趣的老物件帖子
- 个人收藏页统一管理
- 基于本地 session 识别用户

## 🎯 技术亮点

### Docker 构建缓存优化
- **原生分层缓存**：仅使用 Docker 原生机制，不引入额外语法
- **依赖层优先**：pom.xml / package.json 无变更时，直接复用缓存，不重新下载依赖
- **源码变更仅编译**：修改业务代码只触发编译步骤，不重新安装依赖
- **依赖层** → **源码层** 严格分层，构建效率提升 70%

### 国内镜像源全链路覆盖
- **Docker 基础镜像**：统一使用 DOCKER_REGISTRY 环境变量（中科大镜像）
- **Maven 依赖**：腾讯云镜像源
- **npm 依赖**：腾讯云镜像源
- **无需 VPN**：国内网络环境即可正常构建

### 端口冲突防护
- 所有端口使用自定义非默认端口
- 全部绑定 `127.0.0.1`，不对外暴露
- 启动脚本自动检测端口占用，冲突时报错提示占用进程
- strictPort = true，端口被占用时直接报错而非自动切换

### Redis 缓存策略
- 热门帖子 Top10：缓存 1 小时
- 分类 / 年代数据：缓存 2 小时
- maxmemory-policy: allkeys-lru（内存不足时自动淘汰冷数据）
- Spring Cache + @Cacheable 注解式缓存

### 数据库优化
- 年代、品类字段单独建立索引
- 复合索引：(category_id, era_id) 加速组合筛选
- 全文索引：支持标题、内容等字段模糊搜索
- 浏览量、评论量、收藏量增量更新

## 🐳 容器命名规范

所有容器名称与项目名称保持一致：

| 服务 | 容器名称 | 网络 | 数据卷 |
|------|---------|------|--------|
| MySQL | nostalgia-mysql | nostalgia-network | nostalgia-mysql-data |
| Redis | nostalgia-redis | nostalgia-network | nostalgia-redis-data |
| 后端 | nostalgia-backend | nostalgia-network | - |
| 前端 | nostalgia-frontend | nostalgia-network | - |

## 🔧 开发调试

### 前端本地开发
```bash
cd frontend
npm install
npm run dev
```
访问 http://127.0.0.1:3008

### 后端本地开发
配置 IDEA 启动 NostalgiaApplication，注意修改 application.yml 中的数据库和 Redis 地址为本地地址。

## 🧪 验证检查清单

启动后请执行以下验证：

```bash
# 1. 检查端口监听
lsof -nP -iTCP:3008 -sTCP:LISTEN
lsof -nP -iTCP:8088 -sTCP:LISTEN

# 2. 验证前端访问一致性
curl -sS http://127.0.0.1:3008 | head -20
curl -sS http://localhost:3008 | head -20

# 3. 验证后端 API
curl -sS http://127.0.0.1:8088/api/posts | head -50

# 4. 查看容器状态
docker compose ps
```

> ✅ 确保 `localhost:3008` 和 `127.0.0.1:3008` 返回内容完全一致

## 📝 内置示例数据

项目启动后，数据库自动初始化以下示例内容：

| 标题 | 物件 | 年代 | 品类 |
|------|------|------|------|
| 老黑白电视机的回忆 | 黑白电视机 | 80年代 | 影音设备 |
| 童年的铁皮青蛙 | 铁皮青蛙 | 80年代 | 玩具玩偶 |
| 老款双卡录音机 | 双卡录音机 | 90年代 | 影音设备 |

## 📄 License

MIT License
