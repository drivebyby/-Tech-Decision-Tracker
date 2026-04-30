# Fullstack Basic 项目总结文档

## 项目概述

fullstack-basic 是一个可直接运行的前后端分离基础项目，集成了登录、注册、JWT 认证、图形验证码等核心功能，适合作为新项目的启动脚手架。

---

## 技术栈

| 层级 | 技术 | 版本 |
|---|---|---|
| 后端框架 | Spring Boot | 3.3.5 |
| ORM | MyBatis-Plus | 3.5.9 |
| 数据库 | MySQL | 8+ |
| 认证 | JWT (jjwt) + BCrypt | 0.12.6 |
| 验证码 | Hutool Captcha | 5.8.36 |
| 前端框架 | Vue 3 (Composition API) | — |
| 构建工具 | Vite | 5 |
| UI 组件库 | Element Plus | — |
| 图表 | ECharts | 5 |
| 状态管理 | Pinia | — |
| 路由 | Vue Router 4 (Hash 模式) | — |
| HTTP 客户端 | Axios | — |

---

## 项目结构

```
.
├── backend/                          # 后端项目
│   ├── pom.xml                       # Maven 依赖管理
│   └── src/main/java/com/example/fullstack/
│       ├── FullstackBasicApplication.java    # 启动入口
│       ├── common/
│       │   ├── ApiResponse.java              # 统一响应体 {code, message, data}
│       │   ├── AuthContext.java              # ThreadLocal 用户上下文
│       │   └── BusinessException.java        # 业务异常
│       ├── config/
│       │   ├── MybatisPlusConfig.java        # MyBatis-Plus 分页插件
│       │   ├── WebMvcConfig.java             # 拦截器注册
│       │   ├── AuthProperties.java           # JWT 配置属性
│       │   └── CaptchaProperties.java        # 验证码配置属性
│       ├── controller/
│       │   ├── HealthController.java         # 健康检查接口
│       │   ├── AuthController.java           # 认证接口（登录/注册/验证码/个人信息）
│       │   └── UserController.java           # 用户 CRUD 接口
│       ├── dto/
│       │   ├── LoginRequest.java             # 登录请求体
│       │   ├── LoginResponse.java            # 登录响应体（token + 用户信息）
│       │   ├── RegisterRequest.java          # 注册请求体
│       │   ├── CaptchaResponse.java          # 验证码响应体
│       │   ├── UserProfileResponse.java      # 用户信息响应体
│       │   └── UserCreateRequest.java        # 创建用户请求体
│       ├── entity/
│       │   └── User.java                     # 用户实体 → sys_user 表
│       ├── exception/
│       │   └── GlobalExceptionHandler.java   # 全局异常处理
│       ├── interceptor/
│       │   └── AuthInterceptor.java          # JWT 认证拦截器
│       ├── mapper/
│       │   └── UserMapper.java               # MyBatis-Plus BaseMapper
│       ├── service/
│       │   ├── AuthService.java              # 认证服务接口
│       │   ├── UserService.java              # 用户服务接口
│       │   └── impl/
│       │       ├── AuthServiceImpl.java      # 认证服务实现
│       │       └── UserServiceImpl.java      # 用户服务实现
│       └── util/
│           └── JwtTokenUtil.java             # JWT 令牌工具类
│
├── frontend/                         # 前端项目
│   ├── index.html                    # HTML 入口
│   ├── vite.config.ts                # Vite 配置（含 /api 代理）
│   └── src/
│       ├── main.ts                   # 应用入口（挂载 Pinia/ElementPlus/Router）
│       ├── App.vue                   # 根组件
│       ├── api/
│       │   ├── http.ts               # Axios 实例 + 请求/响应拦截器
│       │   ├── auth.ts               # 认证 API 封装
│       │   ├── users.ts              # 用户 API 封装
│       │   └── health.ts             # 健康检查 API 封装
│       ├── stores/
│       │   ├── auth.ts               # 认证状态（token + user，localStorage 持久化）
│       │   └── app.ts                # 应用状态（loading + health）
│       ├── router/
│       │   └── index.ts              # 路由配置 + 导航守卫
│       ├── views/
│       │   ├── AuthView.vue          # 登录/注册页面（含验证码）
│       │   └── DashboardView.vue     # 仪表盘页面
│       ├── components/
│       │   ├── UserTable.vue         # 用户列表表格
│       │   └── DashboardChart.vue    # ECharts 趋势图表
│       └── styles/
│           └── main.scss             # 全局样式 + 响应式布局
│
├── sql/
│   ├── init.sql                      # 数据库初始化脚本
│   └── upgrade_auth.sql              # 增量升级脚本
│
├── scripts/
│   └── use-dev-env.ps1               # 开发环境变量加载脚本
│
└── README.md                         # 项目说明
```

---

## 后端 API 设计

### 公开接口（无需认证）

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/health` | 健康检查，返回服务状态和时间 |
| GET | `/api/auth/captcha` | 获取 SVG 图形验证码，返回 captchaKey + captchaSvg |
| POST | `/api/auth/register` | 注册新用户（需验证码） |
| POST | `/api/auth/login` | 登录（需验证码），返回 JWT Token + 用户信息 |

### 需认证接口（Bearer Token）

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/auth/me` | 获取当前登录用户信息 |
| GET | `/api/users` | 分页查询用户列表（支持 page/size 参数） |
| POST | `/api/users` | 创建用户（仅 username + email） |

### 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

---

## 安全设计

### 认证流程

```
用户 → 获取验证码 → 输入凭证 + 验证码 → 后端校验 → 返回 JWT → 前端存储 → 后续请求携带 Bearer Token
```

### 关键安全措施

| 措施 | 实现方式 |
|---|---|
| 密码存储 | BCrypt 哈希加密，数据库不存明文 |
| 令牌签发 | JWT（jjwt 0.12.6），HMAC-SHA 签名，24 小时有效期 |
| 令牌校验 | AuthInterceptor 拦截器从 Authorization 头提取 Bearer Token 并解析 |
| 用户上下文 | ThreadLocal（AuthContext）传递当前用户 ID，请求结束自动清理 |
| 验证码防刷 | SVG 图形验证码，一次性消费，5 分钟过期，内存 ConcurrentHashMap 存储 |
| 参数校验 | Jakarta Bean Validation（@NotBlank, @Size, @Email, @Pattern） |
| 统一异常处理 | GlobalExceptionHandler 捕获校验异常、业务异常、系统异常 |

### 配置属性

```yaml
# JWT 配置
app.jwt.secret: ${JWT_SECRET:ReplaceThisJwtSecretWithAtLeast32Chars}  # 生产必须覆盖
app.jwt.expire-hours: 24

# 验证码配置
app.captcha.expire-minutes: 5

# 数据库配置（均支持环境变量覆盖）
DB_HOST / DB_PORT / DB_NAME / DB_USERNAME / DB_PASSWORD
```

---

## 前端架构设计

### 路由设计

| 路径 | 组件 | 权限 |
|---|---|---|
| `/` | — | 重定向到 `/dashboard` |
| `/login` | AuthView | 游客模式（已登录自动跳转仪表盘） |
| `/dashboard` | DashboardView | 需认证（未登录跳转登录页） |

### 状态管理

**AuthStore（Pinia）**：
- `token`：JWT 令牌，持久化到 `localStorage`
- `user`：用户信息 `{id, username, email}`，持久化到 `localStorage`
- `setAuth()`：登录/注册成功后保存
- `logout()`：清除所有状态
- `fetchProfile()`：从后端获取最新用户信息

**AppStore（Pinia）**：
- `loading`：全局加载状态
- `health`：后端健康状态信息

### HTTP 拦截器

- **请求拦截器**：自动从 `localStorage` 读取 token 附加到 `Authorization` 头
- **响应拦截器**：
  - 业务状态码非 200 → 抛出异常
  - HTTP 401 → 清除本地状态并跳转登录页

### 页面功能

**登录/注册页（AuthView）**：
- 登录/注册 Tab 切换
- 用户名 + 密码 + 邮箱（注册）+ 图形验证码
- 页面加载自动获取验证码，失败后自动刷新
- 登录/注册成功自动跳转仪表盘

**仪表盘页（DashboardView）**：
- 后端健康状态卡片（API 状态 + 服务时间 + 登录邮箱）
- ECharts 访问趋势图（折线图 + 柱状图，静态演示数据）
- 用户列表表格（ID、用户名、邮箱、状态标签）
- 刷新状态按钮 + 退出登录按钮

---

## 数据库设计

### sys_user 表

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT | 主键，自增 |
| username | VARCHAR(50) | 用户名，唯一索引 |
| email | VARCHAR(100) | 邮箱，唯一索引 |
| password | VARCHAR(100) | BCrypt 密码哈希 |
| status | TINYINT | 状态：1 启用，0 禁用 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 默认测试账号

| 用户名 | 邮箱 | 密码 |
|---|---|---|
| admin | admin@example.com | 123456 |
| demo_user | demo@example.com | 123456 |

---

## 启动方式

### 环境要求

- JDK 17+
- Maven 3.6.3+
- Node.js 18+
- MySQL 8+

### 数据库初始化

```sql
source sql/init.sql;
```

### 启动后端

```bash
cd backend
mvn spring-boot:run
# 运行在 http://localhost:8080/api
```

### 启动前端

```bash
cd frontend
npm install
npm run dev
# 运行在 http://localhost:5173
```

### 验证

```bash
# 后端测试
cd backend && mvn test

# 前端构建
cd frontend && npm run build
```

---

## 已知问题与改进建议

| 优先级 | 问题 | 建议 |
|---|---|---|
| 高 | `application.yml` 配置了逻辑删除字段 `deleted`，但 `sys_user` 表没有该字段 | 移除 `logic-delete-field` 配置，或在表中添加 `deleted` 字段 |
| 高 | 默认 JWT 密钥为占位符 | 生产环境通过 `JWT_SECRET` 环境变量设置强密钥 |
| 中 | 验证码存储在内存中，多实例部署时会失效 | 迁移到 Redis 集中存储 |
| 中 | 无 CORS 配置，生产环境跨域需额外处理 | 补充 CORS 配置或使用 Nginx 反向代理 |
| 低 | `POST /api/users` 创建用户时密码为空字符串 | 明确接口用途：是纯 CRUD Demo 还是应补充密码字段 |
| 低 | ECharts 图表数据为静态演示数据 | 可接入后端统计数据接口 |
