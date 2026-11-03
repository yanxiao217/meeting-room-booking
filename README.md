# 会议室预约管理平台

面向企业 / 学校内部的会议室在线预约与管理系统，包含**用户端**、**管理端**两个前台和一个统一后端服务。

覆盖从「浏览会议室 → 发起预约 → 审批 → 扫码签到 → 会后评价 / 归档」的完整闭环，并附带部门配额、候补排队、信用分、设备报修、数据看板与智能问答等扩展能力。

---

## 技术栈

| 工程 | 说明 | 技术栈 |
|---|---|---|
| `meeting-room-server` | 后端服务 | Spring Boot 3.2 · Java 17 · MyBatis-Plus 3.5 · MySQL 8 · Spring Security + JWT · Spring Mail · Knife4j |
| `meeting-room-web` | 用户端 | Vue 3 · TypeScript · Vite 5 · Element Plus · Pinia · Vue Router |
| `meeting-room-admin` | 管理端 | Vue 3 · TypeScript · Vite 5 · Element Plus · Pinia · ECharts |

---

## 目录结构

```
.
├── meeting-room-server/        # 后端（Spring Boot）
│   ├── src/main/java/com/example/meeting/
│   │   ├── controller/admin/   # 管理端接口
│   │   ├── controller/front/   # 用户端接口
│   │   ├── service/ + impl/    # 业务逻辑
│   │   ├── mapper/             # MyBatis-Plus Mapper（注解 SQL）
│   │   ├── entity/ dto/ vo/    # 数据模型
│   │   ├── config/             # Security / MyBatis-Plus / 邮件 / Web 配置
│   │   ├── interceptor/        # JWT 鉴权、操作日志
│   │   └── task/               # 定时任务
│   ├── src/main/resources/application.yml
│   └── GenHash.java            # BCrypt 密码哈希生成工具
├── meeting-room-web/           # 用户端（Vue 3）
├── meeting-room-admin/         # 管理端（Vue 3）
├── docs/screenshots/           # 界面截图（用户端 / 管理端）
├── db/                         # 建表与增量迁移脚本
└── 会议室预约管理平台PRD*.md    # 需求文档
```

---

## 功能概览

### 用户端

| 模块 | 能力 |
|---|---|
| 登录注册 | 账号密码登录 / 注册，JWT 令牌鉴权 |
| 会议室浏览 | 日历看板视图、周视图，按楼层 / 设备 / 容量筛选，查看实时占用 |
| 预约管理 | 创建、修改、取消预约；进行中 / 历史自动归档；周期预约；冲突检测并推荐可用时段 |
| 部门配额 | 部门周次数 / 月时长额度实时校验，超额拦截 |
| 候补排队 | 时段占满时排队，有人取消后按序补位，限时确认 |
| 扫码签到 | 加密二维码签到，防代签 |
| 信用分 | 按时签到加分、爽约扣分，低分限制预约，每周自动恢复 |
| 会议纪要 | 会后归档、全文检索 |
| 设备报修 | 提交工单、跟踪处理进度 |
| 通知提醒 | 站内消息 + 邮件，覆盖预约 / 审批 / 会前提醒 / 补位 |
| 会议模板 | 常用会议配置一键复用 |
| 智能问答 | 自然语言查询空闲会议室、智能推荐，DeepSeek 兼容接入 + 规则兜底 |

### 管理端

会议室 CRUD 与设备配置 · 预约审批与强制取消 · 候补队列查看与手动补位 · 部门与上下级维护 · 用户与角色管理 · 部门配额配置 · 数据统计看板（使用率 / 闲置热力图 / 预约趋势）· 信用分规则 · 评价审核 · 报修工单流转 · 系统配置（预约规则 / 候补规则 / 通知 / SMTP / AI 模型）· 操作日志。

---

## 界面预览

### 用户端

| 今日空闲 | 本周排期 | 我的预约 |
|:---:|:---:|:---:|
| <img src="docs/screenshots/user/01-available-today.png" width="360"> | <img src="docs/screenshots/user/02-week-schedule.png" width="360"> | <img src="docs/screenshots/user/03-my-bookings.png" width="360"> |

| 我的候补 | 会议纪要 | 会议模板 |
|:---:|:---:|:---:|
| <img src="docs/screenshots/user/04-my-waitlist.png" width="360"> | <img src="docs/screenshots/user/05-meeting-minutes.png" width="360"> | <img src="docs/screenshots/user/06-meeting-templates.png" width="360"> |

| 信用分 | 设备报修 | 通知中心 |
|:---:|:---:|:---:|
| <img src="docs/screenshots/user/07-credit-score.png" width="360"> | <img src="docs/screenshots/user/08-equipment-repair.png" width="360"> | <img src="docs/screenshots/user/09-notifications.png" width="360"> |

| 个人中心 | 智能问答助手 |
|:---:|:---:|
| <img src="docs/screenshots/user/10-profile.png" width="360"> | <img src="docs/screenshots/user/11-chatbot.png" width="360"> |

### 管理端

| 会议室管理 | 预约管理 | 候补管理 |
|:---:|:---:|:---:|
| <img src="docs/screenshots/admin/01-rooms.png" width="360"> | <img src="docs/screenshots/admin/02-bookings.png" width="360"> | <img src="docs/screenshots/admin/03-waitlist.png" width="360"> |

| 报修管理 | 数据看板 | 数据看板（筛选） |
|:---:|:---:|:---:|
| <img src="docs/screenshots/admin/04-repairs.png" width="360"> | <img src="docs/screenshots/admin/05-dashboard.png" width="360"> | <img src="docs/screenshots/admin/06-dashboard-2.png" width="360"> |

| 预约规则 | 候补 / 周期 / 审批规则 | 部门配额 |
|:---:|:---:|:---:|
| <img src="docs/screenshots/admin/07-booking-rules.png" width="360"> | <img src="docs/screenshots/admin/08-waitlist-rules.png" width="360"> | <img src="docs/screenshots/admin/09-dept-quota.png" width="360"> |

| 通知配置 | 部门管理 | 用户管理 |
|:---:|:---:|:---:|
| <img src="docs/screenshots/admin/10-notification-config.png" width="360"> | <img src="docs/screenshots/admin/11-departments.png" width="360"> | <img src="docs/screenshots/admin/12-users.png" width="360"> |

| 操作日志 | 问答日志 | AI 模型配置 |
|:---:|:---:|:---:|
| <img src="docs/screenshots/admin/13-operation-logs.png" width="360"> | <img src="docs/screenshots/admin/14-chatbot-logs.png" width="360"> | <img src="docs/screenshots/admin/15-ai-model-config.png" width="360"> |

---

## 快速开始

### 0. 前置环境

- JDK 17+、Maven 3.8+
- Node.js 18+
- MySQL 8.0

### 1. 初始化数据库

```bash
mysql -u root -p -e "CREATE DATABASE meeting_room_booking DEFAULT CHARSET utf8mb4;"
mysql -u root -p meeting_room_booking < db/init.sql
# 如需升级到最新结构，按版本顺序执行 db/migration_v3.sql ~ migration_v10.sql
```

### 2. 启动后端（:8080）

修改 `meeting-room-server/src/main/resources/application.yml` 中的数据库账号密码，然后：

```bash
cd meeting-room-server
mvn spring-boot:run
```

接口文档（Knife4j）：<http://localhost:8080/doc.html>

### 3. 启动前端

```bash
cd meeting-room-web && npm install && npm run dev     # 用户端
cd meeting-room-admin && npm install && npm run dev   # 管理端
```

### 4. 默认账号

`db/init.sql` 内置了演示数据，管理员账号为 `admin`（密码以 SQL 中存储的 BCrypt 哈希为准）。如需重置某个账号的密码，用 `meeting-room-server/GenHash.java` 生成哈希后更新 `sys_user.password`。

---

## 几处实现说明

- **冲突检测**：新预约与已有预约时间交叉即判定冲突（`新开始 < 已订结束 AND 新结束 > 已订开始`），只统计 `pending / confirmed / signed` 三种状态。
- **状态机**：`pending`（待审批）→ `confirmed`（已确认）→ `signed`（已签到）→ `completed`（已完成），旁支 `cancelled` / `expired`。
- **定时任务**：每分钟释放超时未签到的预约、完成已结束的预约、过期待审批与过期候补；每 5 分钟发送会前提醒；每周一恢复信用分。
- **本仓库不依赖 Redis**：数据全部落在 MySQL，无需额外中间件即可跑通（PRD 中提到的分布式锁方案未在代码中实现，并发预约目前依赖应用层查询检测）。

---

## 说明

本项目为课程设计 / 学习用途，演示数据（用户、会议室、预约记录）已随 `db/init.sql` 一并提供。
