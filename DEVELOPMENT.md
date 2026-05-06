# 开发文档

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Java 17、Spring Boot 3.3.5、MyBatis-Plus 3.5.9、MySQL 8 |
| 认证 | JWT (jjwt 0.12.6) + BCrypt + SVG 图形验证码 (Hutool) |
| 前端 | Vue 3 (Composition API)、TypeScript、Vite 5、Element Plus |
| 图表 | ECharts 5（力导向关系图 + 时间线趋势图） |
| 状态管理 | Pinia |
| 路由 | Vue Router 4 (Hash 模式) |

---

## 目录结构

```text
.
├── backend        # Spring Boot 后端
├── frontend       # Vue 3 前端
├── sql            # 建库脚本
└── scripts        # 开发环境工具
```

---

## 环境要求

- JDK 17+
- Maven 3.6.3+
- Node.js 18+
- MySQL 8+

已验证通过的环境：

- JDK 17.0.12 / Maven 3.6.3 / Node.js 24.15.0 / npm 11.12.1

---

## 数据库初始化

```sql
-- 首次建库
source sql/init.sql;

-- 增量决策表
source sql/upgrade_decision.sql;
```

默认数据库连接配置 `backend/src/main/resources/application.yml`：

```yaml
url: jdbc:mysql://localhost:3306/fullstack_basic
username: root
password: root
```

支持环境变量覆盖：`DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USERNAME`、`DB_PASSWORD`、`JWT_SECRET`

---

## 默认测试账号

| 用户名 | 邮箱 | 密码 |
|---|---|---|
| admin | admin@example.com | 123456 |
| demo_user | demo@example.com | 123456 |

---

## API 接口

公开接口：

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/health` | 健康检查 |
| GET | `/api/auth/captcha` | 获取 SVG 图形验证码 |
| POST | `/api/auth/register` | 注册 |
| POST | `/api/auth/login` | 登录 |

需登录接口（Bearer Token）：

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/auth/me` | 当前用户信息 |
| GET | `/api/users` | 用户分页列表 |
| GET | `/api/decisions` | 决策分页列表 |
| GET | `/api/decisions/{id}` | 决策详情（含决策链 + 关联 commit） |
| POST | `/api/decisions` | 创建决策 |
| PUT | `/api/decisions/{id}` | 更新决策 |
| POST | `/api/decisions/{id}/supersede` | 推翻决策（自动建立链） |
| GET | `/api/decisions/timeline` | 时间线数据 |
| GET | `/api/decisions/graph` | 关系图数据 |
| POST | `/api/decisions/{id}/commits` | 关联 Git commit |

---

## 启动方式

```bash
# 后端
cd backend
mvn spring-boot:run

# 前端
cd frontend
npm install
npm run dev
```

前端默认地址：`http://localhost:5173`

---

## 验证

```bash
cd backend && mvn test
cd frontend && npm run build
```
