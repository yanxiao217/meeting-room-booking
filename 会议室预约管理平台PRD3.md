# 会议室预约管理平台

## 产品需求文档 (PRD)

| 文档信息 | 内容 |
|---------|------|
| 项目名称 | 会议室预约管理平台 |
| 文档版本 | v4.0（全功能版+扩展） |
| 创建日期 | 2026-09-15 |
| 文档状态 | 初稿 |
| 选题来源 | 选题参考 · 企业管理类 · 第 78 题 |

---

## 一、项目概述

### 1.1 项目背景

企业、园区、校园和联合办公空间的会议室资源普遍存在以下痛点：

- **预约方式原始**：靠微信群喊话、纸质登记本、Excel 表格，信息不同步，谁先谁后说不清；
- **冲突频发**：两人同时订了同一间会议室的同一时段，到了现场才发现，直接影响会议；
- **资源不透明**：员工不知道哪些会议室空闲、有什么设备、能坐多少人，找会议室全靠跑楼；
- **管理无数据**：管理员不知道会议室使用率高低、哪些时段闲置、哪些设备坏了，无法优化资源配置；
- **会议全流程割裂**：预约、参会通知、会议纪要、设备维护彼此孤立，缺乏统一管理。

### 1.2 项目定位

本项目定位为 **轻量化会议室预约管理全栈项目**，采用前后端分离架构，融合日历可视化预约、时间段冲突自动检测、可配置预约规则、智能推荐、排队候补、会议纪要闭环、数据可视化看板等能力，打造**覆盖会议全生命周期**的会议室资源管理平台。

核心差异化：**时间段冲突自动检测（应用层 + 数据库层双重保障）+ 可视化日历视图 + 全规则可配置 + 会议全流程闭环（预约→通知→纪要→报修）**。

### 1.3 目标用户

| 用户角色 | 描述 | 核心诉求 |
|---------|------|---------|
| 普通员工 | 需要预约会议室开会的用户 | 快速找到空闲会议室、预约不冲突、方便管理自己的预约、会后纪要归档 |
| 管理员 | 负责会议室资源管理与预约秩序的行政/运维人员 | 会议室统一管理、预约可审可控、使用数据可查、报修闭环 |
| 部门负责人（演进） | 可查看本部门预约情况的管理者 | 部门会议资源使用概览、配额管理 |

> 一个用户可同时拥有员工和管理员身份，管理员在员工功能基础上额外拥有管理权限。

### 1.4 用户画像（示例）

**员工 · 小张，26 岁，产品经理**
- 每周要开 3-5 个会（含每周固定例会），经常临时找会议室；
- 烦透了"到了会议室发现被占了"，希望预约后有保障；
- 开会需要投影仪和白板，希望预约时能筛选设备；
- 会后要写会议纪要，希望平台能沉淀记录。

**管理员 · 李姐，38 岁，行政主管**
- 管理公司 3 层楼共 12 间会议室；
- 经常接到员工投诉"会议室被占"和"大会议室没人用却订不到小会议室"；
- 希望用数据说话，知道哪些会议室该扩容、哪些该改用途；
- 会议室投影仪坏了没人报修，希望设备维护有闭环。

### 1.5 典型场景故事

**场景一：员工快速预约会议室**

> 周三上午，小张要在下午 2 点和研发开需求评审会，需要 8 人会议室 + 投影仪。他打开平台，日历视图显示各会议室当天的占用情况。他筛选"容量≥8 + 有投影仪"，看到 3 楼"创新厅"下午 2:00-3:30 空闲，点击该时段，填写会议主题"V2.0 需求评审"，添加 3 位参会人，提交。系统自动给参会人发送邀请通知，并在会议开始前 15 分钟推送提醒。会后小张在订单详情页补充了会议纪要，归档留痕。

**场景二：冲突自动拦截 + 排队候补**

> 小王和小李几乎同时打开平台，都想订"创新厅"明天上午 10:00-11:30。小王先 2 秒提交成功。小李提交时，系统实时检测到该时段已被预约，红色提示"该时段已被预约"，展示相邻可用时段，同时提供"加入候补队列"选项。小李点了候补。第二天早上 9:30 小王临时取消，系统自动通知小李"您候补的创新厅 10:00-11:30 已释放，请在 10 分钟内确认"，小李一键确认，避免了现场冲突。

**场景三：智能推荐 + 周期预约**

> 小张每周一上午 10:00 要开组会，需要 6 人 + 白板。他选择"周期预约（每周一）"，并让系统"智能推荐"——系统根据参会人数、设备需求、历史使用习惯，推荐"2 楼协作室"（同部门过去 4 周使用 3 次、顺路最近），并标注推荐理由。小张一键确认，系统自动生成未来 4 周的预约。

**场景四：管理员查看使用率 + 设备报修闭环**

> 月底，李姐打开管理后台的数据看板，看到 1 楼"洽谈室"使用率仅 15%，而 3 楼"创新厅"使用率高达 92%、经常订不到。她决定把"洽谈室"改造成可容纳 8 人的小型会议室。同时她看到"创新厅投影仪损坏"的报修单已超 3 天未处理，立即转派给物业，处理完成后系统自动通知报修人。

### 1.6 核心价值主张

| 角色 | 核心价值 |
| --- | --- |
| 员工（预约者） | 30 秒内完成预约；一眼看清哪些会议室空闲；不再撞单；预约、参会、纪要一条龙 |
| 管理员 | 会议室/设备统一管理；预约可审可控；使用率数据驱动资源优化；报修闭环 |
| 组织 | 提升会议室利用率，减少资源浪费，沉淀会议资产，降低行政沟通成本 |

---

## 二、项目目标

### 2.1 核心目标

1. **构建一体化会议室预约平台**：整合会议室资源、预约管理、通知提醒、会议纪要、设备报修；
2. **实现日历可视化预约**：提供周视图/日视图/楼层平面视图，直观展示空闲与占用；
3. **打造零冲突预约体验**：应用层实时检测 + 数据库层并发锁 + 排队候补，三重保障资源利用；
4. **实现会议全生命周期管理**：预约 → 参会邀请 → 签到 → 纪要归档 → 设备报修闭环；
5. **提供智能推荐能力**：基于规则与历史习惯智能推荐会议室，支持自然语言预约（演进）；
6. **实现全规则可配置**：预约时长、可预约天数、时段、取消时限、部门配额等均可后台配置；
7. **建立数据可视化体系**：使用率、热门会议室、预约趋势、闲置热力图、管理驾驶舱。

### 2.2 量化指标

| 指标 | 目标值 |
|------|--------|
| 预约成功率 | ≥ 98% |
| 冲突漏检率 | 0（硬性指标） |
| 预约操作时长 | ≤ 30 秒 |
| 日历视图加载速度 | P95 ≤ 1.5 秒 |
| 预约提交响应 | P95 ≤ 1 秒 |
| 核心接口可用性 | ≥ 99.5% |
| 并发预约支持 | 100 并发同一时段仅 1 条成功 |
| 会议室整体使用率 | ≥ 50% |
| 候补补位成功率 | ≥ 40% |
| 设备报修闭环率 | ≥ 90%（报修到处理完成） |

---

## 三、系统架构设计

### 3.1 总体架构

```
┌─────────────────────────────────────────────────────────────┐
│                      用户层 (User Layer)                      │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │   PC Web    │  │  Mobile Web │  │  门牌屏/大屏 │          │
│  └─────────────┘  └─────────────┘  └─────────────┘          │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    接入层 (Access Layer)                      │
│              Nginx反向代理 + 负载均衡 + SSL终止                │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   应用层 (Application Layer)                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │  前台用户端   │  │  后台管理端   │  │  后端服务     │       │
│  │   (Vue3)     │  │   (Vue3)     │  │ (Spring Boot) │       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    服务层 (Service Layer)                     │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐    │
│  │ 用户服务  │  │ 会议室服务│  │ 预约服务  │  │ 通知服务  │    │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘    │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐    │
│  │ 冲突检测  │  │ 候补服务  │  │ 纪要服务  │  │ 报修服务  │    │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘    │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                    │
│  │ 推荐服务  │  │ 规则配置  │  │ 统计服务  │                    │
│  └──────────┘  └──────────┘  └──────────┘                    │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    数据层 (Data Layer)                        │
│         ┌──────────┐  ┌──────────┐  ┌──────────┐            │
│         │  MySQL   │  │  Redis   │  │   OSS    │            │
│         │ (关系库) │  │(缓存/锁) │  │ (文件存储)│            │
│         └──────────┘  └──────────┘  └──────────┘            │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    外部依赖 (External)                        │
│   ┌──────────────┐  ┌──────────────┐                        │
│   │ 大模型API     │  │ 邮件/企业微信  │                        │
│   │(演进:NL预约)  │  │ (通知渠道)    │                        │
│   └──────────────┘  └──────────────┘                        │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 技术架构

#### 前端技术栈

| 层级 | 技术选择 | 说明 |
|------|---------|------|
| 框架 | Vue 3 + TypeScript | 主流前端框架，类型安全 |
| UI库 | Element Plus | 企业级UI组件库 |
| 构建工具 | Vite | 快速构建工具 |
| 状态管理 | Pinia | Vue 3推荐状态管理 |
| 路由 | Vue Router 4 | 官方路由解决方案 |
| 日历组件 | FullCalendar | 周/日视图、拖拽选时段 |
| 图表 | ECharts | 数据看板、热力图、驾驶舱 |
| HTTP | Axios | 接口请求封装 |
| 富文本 | wangEditor（演进） | 会议纪要编辑 |

#### 后端技术栈

| 层级 | 技术选择 | 说明 |
|------|---------|------|
| 框架 | Spring Boot 3 | Java企业级框架 |
| JDK | Java 17 | LTS 长期支持版本 |
| ORM | MyBatis-Plus | 数据库操作，简化CRUD |
| 数据库 | MySQL 8 | 关系型数据库 |
| 缓存/锁 | Redis 7 | 缓存、分布式锁、候补队列、限流 |
| 定时任务 | Spring Task / XXL-Job（演进） | 自动释放、提醒、日报调度 |
| 认证 | JWT + Spring Security | 无状态认证 + RBAC权限 |
| API文档 | Knife4j | Swagger增强 |
| 工具库 | Hutool / EasyExcel | 工具类 / Excel 导入导出 |
| LLM（演进） | Spring AI | 自然语言预约（可选） |

### 3.3 部署架构

- 前端：Nginx 托管静态资源，反向代理后端 API；
- 后端：Spring Boot Jar 包部署，支持多实例水平扩展；
- 数据：MySQL 主从（演进）、Redis 单机/哨兵（演进）；
- 本地开发：Docker Compose 一键启动 MySQL + Redis；
- 门牌屏/大屏：独立前端页面，走同一后端 API。

---

## 四、功能需求

> 优先级标记：P0（MVP 必须）、P1（V1.1 推荐）、P2（V1.2 完整）、P3（演进/可选）。

### 4.1 前台-员工端功能

#### 4.1.1 日历视图模块

**功能描述**：提供可视化日历视图，直观展示各会议室的空闲与占用情况，支持筛选、快速预约与楼层平面视图。

| 功能点 | 描述 | 优先级 |
|-------|------|--------|
| 周视图 | 默认视图，横向为周一至周日，纵向为时间段，每个会议室一列或一组 | P0 |
| 日视图 | 单日详细视图，展示所有会议室的时间轴 | P0 |
| 楼层平面视图 | SVG 楼层平面图，会议室按位置分布并着色显示状态（空闲/占用/维护） | P2 |
| 日期切换 | 上一周/下一周/回到今天 | P0 |
| 会议室筛选 | 按楼层、容量、设备、关键词筛选 | P0 |
| 空闲时段展示 | 已预约时段显示彩色色块，空闲时段为空白可点击 | P0 |
| 预约信息浮层 | 鼠标悬停已预约色块，显示会议主题（自己的）/ "已占用"（他人的） | P0 |
| 点击快速预约 | 点击空闲时段，弹出预约表单（预填会议室和时间段） | P0 |
| 拖拽选时段 | 点击开始时间拖动到结束时间，快速选择区间 | P1 |
| 维护中标识 | 维护中/停用的会议室置灰并标注，不可点击 | P0 |
| 我的预约高亮 | 自己的预约色块深色显示，他人的浅色显示 | P0 |
| 智能推荐入口 | 日历页提供"帮我推荐会议室"入口（规则版智能推荐） | P1 |
| 列表视图 | 按时间顺序展示预约列表（演进） | P2 |

**交互说明**：
- 顶部有日期切换器和会议室筛选器；
- 他人预约仅显示"已占用"，不泄露会议主题和参会人（隐私保护）；
- 移动端周视图切换为单日视图或可横向滑动；
- 日历加载时显示骨架屏，不白屏。

#### 4.1.2 预约管理模块

**功能描述**：用户可创建、查看、修改、取消、周期预约，支持参会人管理、会议模板、排队候补，系统自动进行规则校验、冲突检测与智能推荐。

| 功能点 | 描述 | 优先级 |
|-------|------|--------|
| 创建预约 | 选择会议室、日期、时间段，填写主题、参会人数、设备需求、备注 | P0 |
| 规则校验 | 校验时长、可预约天数、可预约时段、每日上限、部门配额等规则 | P0 |
| 冲突检测 | 实时检测同一会议室同一时段是否已被预约 | P0 |
| 冲突提示与建议 | 冲突时提示原因，展示3个相邻可用时段，点击一键替换 | P0 |
| 排队候补 | 目标时段被占时加入候补队列，原预约取消后自动通知补位 | P1 |
| 我的预约列表 | 分"即将开始/已完成/已取消"三个Tab，按时间排序 | P0 |
| 预约详情 | 展示会议室、时间、主题、参会人、设备、备注、状态、纪要 | P0 |
| 取消预约 | 未开始且在取消时限内可自行取消，取消后释放时段 | P0 |
| 修改预约 | 未开始预约可修改时间/会议室/主题，重新走冲突检测 | P1 |
| 周期预约 | 支持按日/周/月重复预约（如每周例会），生成独立预约记录统一管理 | P1 |
| 会议模板 | 保存常用预约配置（会议室/时间段/设备/参会人），一键复用 | P2 |
| 参会人管理 | 添加/移除参会人，系统自动发送参会邀请通知 | P1 |
| 签到 | 会议开始后可签到（点击/扫码），未签到超时自动释放（可配置） | P1 |
| 智能推荐会议室 | 根据参会人数、设备需求、历史习惯推荐Top3会议室并说明理由 | P1 |
| 自然语言预约 | 输入"周五下午3点订8人带投影的会议室"，LLM解析并自动推荐（演进） | P3 |

**创建预约字段**：

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| 会议室 | 是 | 从可用会议室中选择，或由智能推荐填入 |
| 日期 | 是 | 不可选过去日期，不可选超过可预约天数上限的日期 |
| 开始时间 | 是 | 最小粒度30分钟（可配置），须在可预约时段内 |
| 结束时间 | 是 | 须晚于开始时间，时长在最小/最大限制内 |
| 会议主题 | 是 | 1-50字 |
| 参会人数 | 否 | 默认1，不可超过会议室容量 |
| 参会人 | 否 | 从通讯录选择，默认最多20人 |
| 设备需求 | 否 | 多选，仅作备注 |
| 备注 | 否 | 0-200字 |

**业务流程**：
```
用户选时段/自然语言 → 填写预约信息 → 规则校验 → 冲突检测 → 生成预约(已确认) → 通知参会人 → 日历刷新
       ↓不通过              ↓冲突                 ↓可选
     提示原因修改       提示+相邻时段建议    排队候补(取消自动补位)
```

#### 4.1.3 会议纪要模块

**功能描述**：把会议预约延伸为"会议全生命周期"管理，支持议题预设、纪要填写、附件上传、归档检索。

| 功能点 | 描述 | 优先级 |
|-------|------|--------|
| 议题预设 | 预约时填写会议议题列表（可选） | P2 |
| 纪要填写 | 会议结束后由预约人或参会人填写纪要（富文本） | P2 |
| 纪要附件 | 支持上传附件（文档/图片），存 OSS | P2 |
| 纪要归档 | 已完成会议的纪要自动归档，可按会议室/时间/主题检索 | P2 |
| 纪要提醒 | 会议结束后未填纪要，24小时内提醒预约人 | P2 |
| 纪要导出 | 会议纪要导出为 Word/PDF | P3 |

#### 4.1.4 设备报修模块

**功能描述**：员工发现会议室设备故障时在线报修，管理员处理并闭环，报修状态联动会议室状态。

| 功能点 | 描述 | 优先级 |
|-------|------|--------|
| 发起报修 | 选择会议室、设备类型（投影仪/白板/空调等）、故障描述、图片 | P2 |
| 报修列表 | 我的报修：待处理/处理中/已完成，处理进度可见 | P2 |
| 报修状态 | 待处理→处理中→已完成（含处理人、处理说明、完成时间） | P2 |
| 状态联动 | 报修中的设备所在会议室可标记"维护中"，自动停止接受新预约 | P2 |
| 报修通知 | 报修提交/处理完成时通知报修人 | P2 |

#### 4.1.5 通知提醒模块

| 功能点 | 描述 | 优先级 |
|-------|------|--------|
| 预约成功通知 | 预约提交成功后，站内通知确认信息 | P0 |
| 参会邀请通知 | 添加参会人后，自动通知参会人会议信息 | P1 |
| 开始前提醒 | 会议开始前15分钟（可配置）推送站内提醒 | P0 |
| 候补补位通知 | 候补成功时通知候补人，限时确认 | P1 |
| 取消通知 | 预约被取消（自己/管理员/审批拒绝）时通知相关人 | P0 |
| 报修通知 | 报修提交/处理完成时通知报修人 | P2 |
| 纪要坚持通知 | 会议结束后未填纪要的提醒 | P2 |
| 通知中心 | 查看所有历史通知，标记已读/未读 | P1 |
| 通知偏好 | 用户可自定义是否接收各类通知、提醒提前量 | P2 |
| 多渠道通知 | 邮件/企业微信Webhook推送（站内之外） | P2 |

#### 4.1.6 用户中心模块

| 功能点 | 描述 | 优先级 |
|-------|------|--------|
| 登录注册 | 支持手机号+验证码/密码登录 | P0 |
| 个人信息 | 昵称、头像、所属部门、手机号管理 | P0 |
| 修改密码 | 支持修改登录密码 | P1 |
| 常用会议室收藏 | 收藏高频会议室，日历/预约时快捷筛选 | P1 |
| 会议模板管理 | 我的模板：保存/编辑/删除/一键预约 | P2 |
| iCal 日历订阅 | 导出/订阅我的预约到 Google/Outlook（.ics） | P2 |
| 信用分查看 | 查看我的信用分、等级说明、信用变动记录 | P1 |
| 被限制提示 | 信用分低于阈值被限制预约时，明确提示原因与恢复方式 | P1 |

#### 4.1.7 信用分与爽约机制（P1，新增）

**功能描述**：通过信用分治理"预约后 no-show"（爽约）问题，与签到、自动释放形成闭环，让会议室资源不被浪费。

| 功能点 | 描述 | 优先级 |
|-------|------|--------|
| 信用分展示 | 用户中心展示当前信用分（初始100分，上限100） | P1 |
| 信用记录 | 每次变动记录：类型、变动值、变动前后分值、关联业务、时间 | P1 |
| 爽约扣分 | 预约"已过期"（未签到未取消）自动扣分 | P1 |
| 完成加分 | 正常签到并完成会议自动加分 | P1 |
| 每周恢复 | 每周定时任务自动恢复少量信用分（上限封顶） | P1 |
| 低分限制 | 低于阈值时限制可预约未来天数（如仅3天内） | P1 |
| 限制解除 | 信用分回升到阈值以上自动解除限制 | P1 |
| 管理员调整 | 管理员可手动调整用户信用分并备注原因 | P2 |

#### 4.1.8 会议室评价（P1，新增）

**功能描述**：会议结束后用户对会议室设施与环境打分，数据反哺管理（低分联动报修检查、影响使用率分析）。

| 功能点 | 描述 | 优先级 |
|-------|------|--------|
| 打分评价 | 已完成会议 24h 内可评价：1-5 星 + 标签（环境好/设备好用/隔音差/空调问题等）+ 文字 | P1 |
| 评价限制 | 同一用户对同一预约仅可评价一次 | P1 |
| 评价展示 | 会议室详情页展示平均分与评价列表 | P1 |
| 低分联动 | 3 星以下评价自动生成"待检查"提示（管理员视角），可一键发起报修 | P1 |
| 评价提醒 | 会议结束后 24h 内提醒未评价用户 | P2 |

#### 4.1.9 签到二维码（P1，新增）

**功能描述**：预约成功后生成专属签到二维码，会议开始前后扫码即签到，替代手动按钮，演示感更强。

| 功能点 | 描述 | 优先级 |
|-------|------|--------|
| 二维码生成 | 预约详情页展示签到二维码（内容为加密后的预约编号） | P1 |
| 扫码签到 | 扫码进入签到确认页，确认后即签到 | P1 |
| 签到窗口 | 仅会议开始前30分钟至开始后（自动释放窗口）内可扫码 | P1 |
| 防重复 | 已签到预约再次扫码提示"已签到" | P1 |
| 签到统计 | 签到时间、扫码方式（二维码/按钮）记录 | P2 |

### 4.2 后台管理端功能

#### 4.2.1 会议室管理模块

| 功能点 | 描述 | 优先级 |
|-------|------|--------|
| 会议室列表 | 展示所有会议室，含状态、容量、楼层、今日预约数、设备健康度 | P0 |
| 新增会议室 | 填写名称、位置、容量、类型、设备、图片、状态、备注 | P0 |
| 编辑会议室 | 修改会议室信息 | P0 |
| 删除会议室 | 逻辑删除；有未完成预约时禁止删除 | P0 |
| 状态管理 | 可用/维护中/停用，维护中和停用不可被预约 | P0 |
| 会议室筛选 | 按楼层、类型、容量、状态、设备筛选 | P0 |
| 批量导入 | Excel批量导入会议室信息 | P1 |
| 批量导出 | 会议室信息导出Excel | P1 |
| 楼层图管理 | 配置楼层平面图的会议室位置与样式 | P2 |

#### 4.2.2 预约管理模块

| 功能点 | 描述 | 优先级 |
|-------|------|--------|
| 预约总览 | 查看全部预约，按日期/会议室/状态/用户筛选 | P0 |
| 预约详情 | 查看预约完整信息（含参会人、纪要） | P0 |
| 强制取消 | 取消任何未开始的预约，需填写原因，取消后通知预约人并触发候补 | P0 |
| 候补管理 | 查看候补队列、手动补位、调整候补顺序 | P1 |
| 审批流程 | 特定会议室开启"需审批"，管理员通过/拒绝 | P2 |
| 代客预约 | 管理员替员工创建预约（如领导秘书代约） | P1 |
| 预约导出 | 预约数据导出Excel | P1 |

#### 4.2.3 数据统计模块

| 功能点 | 描述 | 优先级 |
|-------|------|--------|
| 使用率统计 | 按日/周/月统计各会议室使用率，排序展示 | P1 |
| 热门会议室 | 预约量Top5会议室，展示预约次数、使用率、平均时长 | P1 |
| 预约趋势 | 折线图展示近30天预约量趋势，可按部门/会议室筛选 | P1 |
| 闲置时段热力图 | 一周×时段占用热力图，直观发现闲置时段 | P1 |
| 部门使用统计 | 按部门统计预约次数和时长，与配额对比 | P2 |
| 取消率统计 | 各会议室预约取消率，异常高的提示关注 | P2 |
| 候补转化统计 | 候补入队数/补位成功数/转化率 | P2 |
| 报修统计 | 报修量、处理时长、未闭环工单数 | P2 |
| 管理驾驶舱 | 大屏汇总页：核心指标一屏展示（使用率/预约量/待处理报修/热门时段） | P1 |
| 使用率预测 | 基于历史数据预测未来热门时段与闲置时段 | P3 |
| 数据导出 | 统计数据导出Excel | P2 |

#### 4.2.4 系统设置模块

| 功能点 | 描述 | 优先级 |
|-------|------|--------|
| 预约规则配置 | 配置最小/最大时长、可预约天数、可预约时段、时间粒度、取消时限、每日上限 | P0 |
| 部门配额配置 | 配置各部门每周/每月预约次数或时长上限 | P2 |
| 候补规则配置 | 候补开关、补位确认时限（默认10分钟）、候补队列上限 | P1 |
| 周期预约配置 | 周期预约最大生成次数、可预约周期类型 | P1 |
| 自动释放配置 | 签到开关、超时释放时间 | P1 |
| 审批配置 | 审批开关、审批超时时间、需审批的会议室 | P2 |
| 工作日历管理 | 配置法定节假日、公司特殊休息日 | P1 |
| 通知配置 | 提醒提前量、通知开关、多渠道配置 | P1 |
| 系统参数 | 系统名称、Logo等基础配置 | P2 |

#### 4.2.5 用户与权限管理

| 功能点 | 描述 | 优先级 |
|-------|------|--------|
| 用户列表 | 查看所有用户，按角色/部门/状态筛选 | P1 |
| 用户管理 | 禁用/启用用户、重置密码、分配角色 | P1 |
| 角色管理 | 维护角色列表和权限（员工/管理员/部门负责人） | P1 |
| 部门管理 | 维护部门列表，用户归属部门 | P1 |
| 操作日志 | 管理员关键操作审计日志（谁、何时、做了什么） | P1 |
| 登录日志 | 登录行为审计 | P2 |

#### 4.2.6 报修管理模块

| 功能点 | 描述 | 优先级 |
|-------|------|--------|
| 报修工单列表 | 全部报修，按状态/会议室/时间筛选 | P2 |
| 工单处理 | 接单/处理中/完成，填写处理说明，可转派 | P2 |
| 报修导出 | 报修记录导出Excel | P3 |

#### 4.2.7 信用分与评价管理（P1，新增）

| 功能点 | 描述 | 优先级 |
|-------|------|--------|
| 信用分规则配置 | 配置：初始分、爽约扣分值、完成加分值、每周恢复值、低分阈值、低分限制天数 | P1 |
| 信用记录查询 | 按用户/时间查询信用变动记录，含业务关联 | P1 |
| 手动调整 | 调整用户信用分，必填原因，写入操作日志 | P2 |
| 低分用户列表 | 筛选低于阈值的用户，支持一键提醒 | P2 |
| 评价列表 | 全部评价，按会议室/星级/时间筛选 | P1 |
| 评价统计 | 各会议室平均分、评分分布（1-5星占比）、低分会议室Top | P1 |
| 低分工单 | 低分评价自动生成的"待检查"列表，可转报修工单 | P1 |

#### 4.2.8 多租户（演进）

| 功能点 | 描述 | 优先级 |
|-------|------|--------|
| 租户管理 | 多公司/多园区独立数据空间（组织架构、会议室、规则隔离） | P3 |
| 租户套餐 | 会议室数量、用户数配额（演进） | P3 |

### 4.3 智能服务层功能

#### 4.3.1 智能推荐会议室（规则版，P1）

**功能描述**：根据参会人数、设备需求、部门历史习惯、时段可用性，推荐 Top3 会议室并给出理由。

| 功能点 | 描述 | 优先级 |
|-------|------|--------|
| 规则推荐 | 输入人数/设备/时段 → 过滤可用会议室 → 按匹配度排序 | P1 |
| 历史习惯 | 统计该用户/部门历史使用最多的会议室，加权排序 | P1 |
| 推荐理由 | 生成简短理由（"2楼协作室：8人容量满足，含白板，本部门近4周使用3次"） | P1 |
| 一键预约 | 推荐结果可直接进入预约表单 | P1 |

**推荐排序规则**（权重可配置）：

| 维度 | 权重（默认） | 说明 |
| --- | --- | --- |
| 容量匹配度 | 30% | 容量与参会人数匹配（≥人数且最接近） |
| 设备匹配度 | 25% | 设备需求全部满足 |
| 部门使用习惯 | 20% | 该部门历史使用次数加权 |
| 时段可用性 | 15% | 目标时段是否空闲（空闲优先） |
| 楼层距离 | 10% | 距用户常用楼层更近优先 |

#### 4.3.2 自然语言预约（LLM，P3）

**功能描述**：用户用自然语言描述预约需求，LLM 解析为结构化预约请求并自动推荐会议室。

| 功能点 | 描述 | 优先级 |
|-------|------|--------|
| NL 解析 | 解析时间/人数/设备/地点偏好（"周五下午3点订8人带投影的会议室"） | P3 |
| 自动推荐 | 解析结果驱动智能推荐，返回候选会议室 | P3 |
| 多轮澄清 | 信息不足时追问（"需要哪个楼层？"） | P3 |
| 失败降级 | LLM 失败自动切换表单，不阻塞预约 | P3 |
| 调用审计 | 记录 NL 调用（输入/输出/耗时/成败） | P3 |

#### 4.3.3 冲突智能协商（LLM，P3）

**功能描述**：冲突时由 LLM 生成协调话术与替代方案建议，帮助用户快速改期。

| 功能点 | 描述 | 优先级 |
|-------|------|--------|
| 协调话术 | 冲突时生成"建议改到X时段或Y会议室"的自然语言说明 | P3 |
| 替代方案 | 基于用户特征生成 Top3 替代方案 | P3 |
| 协商消息 | 双方可发起改期协商（站内消息） | P3 |

#### 4.3.4 使用率预测（P3）

| 功能点 | 描述 | 优先级 |
|-------|------|--------|
| 趋势预测 | 基于历史预约数据预测未来7天各时段热度 | P3 |
| 闲置预测 | 预测低利用时段，辅助管理员运营（如开放自由讨论） | P3 |
| 预测展示 | 日历/看板标注预测热度 | P3 |

#### 4.3.5 智能问答助手（LLM，P1，新增）

**功能描述**：用户以自然语言提问，LLM 解析意图与参数，调用平台查询能力，返回自然语言回答。不改动预约主流程，风险低，是平台的 AI 亮点。

| 功能点 | 描述 | 优先级 |
|-------|------|--------|
| 自然语言提问 | 支持问题如"明天下午有哪些8人带投影的会议室空闲" | P1 |
| 意图解析 | LLM 结构化输出：意图（查空闲会议室/查我的预约/查会议室信息/查规则）+ 参数（日期/时段/人数/设备） | P1 |
| 查询执行 | 解析结果驱动现有查询接口，返回真实数据 | P1 |
| 回答生成 | LLM 基于查询结果生成自然语言回答（含会议室名、时段、推荐理由） | P1 |
| 入口位置 | 用户端右下角悬浮入口 + 日历页快捷按钮 | P1 |
| 失败降级 | LLM 解析失败时提示"换个说法"，查询无结果时给出建议（如扩大人数/换日期） | P1 |
| 调用审计 | 记录每次问答：问题/回答/意图/参数/耗时/成败（答辩可展示） | P1 |

**问答流程**：
```
用户提问 → LLM意图与参数解析(结构化JSON) → 参数校验 → 调用查询服务 → 结果组装 → LLM自然语言回答
   ↓失败                              ↓参数缺失                 ↓无结果
  提示换种说法                       反问补全(日期/人数)        给出放宽条件的建议
```

### 4.4 核心业务规则

#### 4.4.1 预约规则（可由管理员配置）

| 规则项 | 默认值 | 说明 |
| --- | --- | --- |
| 最小预约时长 | 30分钟 | 不可低于此时长 |
| 最大预约时长 | 4小时 | 单次预约不可超过 |
| 可预约天数范围 | 未来14天 | 只能预约今天起14天内的时段 |
| 可预约时段 | 08:00 - 22:00 | 每日可预约的时间范围 |
| 最小时间粒度 | 30分钟 | 开始/结束时间只能是整半点 |
| 提前取消时限 | 开始前15分钟 | 超过此时限不可自行取消，需联系管理员 |
| 每人每日预约上限 | 3次 | 防止恶意占用（可配置为不限） |
| 节假日是否可预约 | 否 | 对接工作日历 |
| 部门配额 | 不限 | 每周/每月预约次数或时长上限 |

#### 4.4.2 冲突检测机制（核心）

**重叠判定公式**：
新预约 `[start, end)` 与已有已确认预约 `[s, e)` 满足 `start < e AND end > s` 即视为冲突。

**检测流程**：
1. 员工提交预约时，后端先做**规则校验**（时长、天数、时段、每日上限、部门配额）；
2. 规则通过后，查询该会议室在 `[start, end)` 时段内所有状态为"已确认"的预约；
3. 若存在任意一条满足重叠判定公式，则判定冲突；
4. 冲突时返回该会议室当天剩余可用时段列表与相邻建议，引导用户改期、换会议室或**加入候补队列**。

**并发保障（两人同时提交同一时段）**：
- 应用层检测之外，**对同一会议室同一日期的预约创建加分布式锁**（Redis 锁，key: `lock:booking:room:{roomId}:{date}`）或数据库行锁；
- 确保 100 并发请求中仅 1 条成功写入，后提交者正确收到冲突提示；
- **冲突漏检率 = 0** 是本产品的硬性验收指标。

#### 4.4.3 排队候补规则

| 编号 | 规则 |
| --- | --- |
| WL-01 | 冲突时可加入候补队列（同一时段每会议室候补上限默认5人，可配置） |
| WL-02 | 原预约取消/被强制取消时，按候补顺序通知第1位候补人 |
| WL-03 | 候补人收到通知后需在确认时限内（默认10分钟）确认，超时视为放弃，顺延下一位 |
| WL-04 | 候补期间用户仍可正常预约其他会议室 |
| WL-05 | 用户可随时退出候补队列 |
| WL-06 | 候补成功生成正式预约，释放候补名额 |

#### 4.4.4 周期预约规则

| 编号 | 规则 |
| --- | --- |
| RC-01 | 支持按日/周/月重复，生成未来 N 次独立预约（默认最多4次，可配置） |
| RC-02 | 每次重复独立走冲突检测；冲突的单次跳过并提示（其余正常生成） |
| RC-03 | 周期预约统一管理：可整体查看、单次修改/取消、终止后续周期 |
| RC-04 | 周期预约计入每日预约上限与部门配额 |

#### 4.4.5 签到与自动释放规则

| 编号 | 规则 |
| --- | --- |
| SG-01 | 会议开始后可签到（点击按钮或扫码） |
| SG-02 | 若开启自动释放：会议开始后默认15分钟未签到，预约自动转为"已过期"，时段释放 |
| SG-03 | 释放前通知预约人"将自动释放"（提前5分钟） |
| SG-04 | 已签到预约不可自动释放 |
| SG-05 | 自动释放开关与超时时间可由管理员配置 |

#### 4.4.6 预约状态机

| 状态 | 触发条件 | 说明 | 可执行操作 |
| --- | --- | --- | --- |
| 待审批 | 会议室开启审批模式，员工提交 | 暂不占用时段（可配置） | 管理员审批通过/拒绝；员工取消 |
| 已确认 | 提交成功（无需审批）或审批通过 | 占用时段，冲突检测生效 | 员工取消/修改；管理员强制取消 |
| 已签到 | 会议开始后员工签到 | 占用时段 | 查看详情 |
| 已完成 | 会议结束时间到达 | 终态，释放时段 | 填写纪要、查看详情 |
| 已取消 | 员工/管理员取消，或审批拒绝 | 终态，释放时段，触发候补补位 | 查看详情 |
| 已过期 | 超过开始时间未签到且开启自动释放 | 终态，释放时段 | 查看详情 |

#### 4.4.7 信用分规则（新增）

| 编号 | 规则 |
| --- | --- |
| CR-01 | 初始信用分 100 分，上限 100 分，下限 0 分 |
| CR-02 | 预约"已过期"（爽约：未签到且未取消）扣 10 分（可配置） |
| CR-03 | 正常签到并完成会议 +2 分（可配置） |
| CR-04 | 每周一 00:00 定时任务自动恢复 +5 分（可配置），封顶 100 分 |
| CR-05 | 取消时限内正常取消不扣分；超过取消时限取消扣 5 分（可配置） |
| CR-06 | 信用分 < 60（可配置阈值）时，只能预约未来 3 天（可配置天数）内时段 |
| CR-07 | 信用分回升到阈值及以上自动解除限制 |
| CR-08 | 管理员手动调整需填写原因，记录操作日志；调整值可为负数 |
| CR-09 | 信用分变动实时写入信用记录表（前后分值快照） |

#### 4.4.8 会议室评价规则（新增）

| 编号 | 规则 |
| --- | --- |
| RR-01 | 仅状态为"已完成"且已过结束时间的预约可评价 |
| RR-02 | 评价窗口：会议结束后 24 小时内（可配置） |
| RR-03 | 同一用户对同一预约只能评价一次（唯一约束：booking_id + user_id） |
| RR-04 | 评分 1-5 星必填，标签与文字选填 |
| RR-05 | 评分 ≤ 3 星自动生成"待检查"记录，管理员可一键转报修工单 |
| RR-06 | 会议室平均分 = 全部评价星级的算术平均，实时更新 |

#### 4.4.9 签到二维码规则（新增）

| 编号 | 规则 |
| --- | --- |
| QR-01 | 预约状态为"已确认"且未签到时，详情页展示签到二维码 |
| QR-02 | 二维码内容为加密后的预约编号（AES，密钥服务端保存），有效期至会议开始后自动释放窗口 |
| QR-03 | 可扫码时段：会议开始前 30 分钟 至 会议开始后自动释放超时时间 |
| QR-04 | 扫码后需在确认页确认，确认即签到，状态变"已签到" |
| QR-05 | 已签到/已取消/已过期预约扫码提示对应状态，不可重复签到 |
| QR-06 | 签到记录同时保留签到时间与来源（button/qrcode） |

---

## 五、非功能需求

### 5.1 性能需求

| 指标 | 要求 |
|------|------|
| 页面加载时间 | < 3秒 |
| 日历视图首屏加载 | P95 ≤ 1.5秒（一周数据，≤20间会议室） |
| API响应时间 | < 500ms |
| 预约提交响应 | P95 ≤ 1秒（含冲突检测） |
| 管理端数据看板加载 | P95 ≤ 3秒 |
| 并发用户数 | > 500 |
| 并发预约 | 100并发同一时段仅1条成功 |
| 系统可用性 | > 99.5% |

### 5.2 安全需求

| 需求 | 描述 |
|------|------|
| 身份认证 | JWT Token认证 |
| 权限控制 | RBAC权限模型，普通员工只能操作自己的预约 |
| 数据加密 | 敏感数据加密存储，密码BCrypt加密 |
| 接口安全 | API限流、防刷、防重复提交 |
| 数据隐私 | 他人预约仅显示"已占用"，不泄露主题和参会人 |
| 文件安全 | 纪要/报修附件上传做类型与大小校验，防上传恶意文件 |
| 日志审计 | 管理员关键操作（强制取消、删除会议室、规则变更）全量记录 |
| 操作确认 | 管理员破坏性操作需二次确认 |

### 5.3 兼容性需求

| 需求 | 描述 |
|------|------|
| 浏览器兼容 | Chrome、Firefox、Edge、Safari最新两个版本 |
| 移动端适配 | 响应式设计，适配主流手机和平板 |
| 分辨率支持 | 1366x768 - 3840x2160 |
| 日历组件兼容 | FullCalendar在各浏览器下渲染一致 |
| 大屏适配 | 门牌屏/驾驶舱支持 1080p/4K 显示 |

### 5.4 可扩展性需求

- 模块化设计，功能可插拔；
- API版本管理，支持平滑升级；
- 预约规则全部可配置，无需改代码；
- 支持会议室数量水平扩展（≤100间无性能瓶颈）；
- 预留审批流程、周期预约、签到、多租户、LLM 能力扩展点；
- 通知渠道抽象，可插拔接入邮件/企业微信/短信。

### 5.5 数据指标与埋点

#### 核心业务指标

| 指标 | 定义 | 目标 |
| --- | --- | --- |
| 预约量 | 每日/每周创建的预约数 | — |
| 预约成功率 | 成功生成预约数 / 提交预约数 | ≥ 98% |
| 冲突率 | 因冲突被拒绝的提交数 / 总提交数 | 监控项 |
| 取消率 | 取消预约数 / 已确认预约数 | ≤ 20% |
| 候补转化率 | 补位成功数 / 候补入队数 | ≥ 40% |
| 平均预约时长 | 所有已完成预约的平均时长 | 监控项 |
| 会议室使用率 | 预约时长 / 可预约总时长（按会议室） | 整体 ≥ 50% |
| 智能推荐采纳率 | 经推荐选择的预约 / 智能推荐触发数 | ≥ 30% |
| 纪要填写率 | 已填写纪要的完成订单 / 已完成订单 | ≥ 60% |
| 报修闭环率 | 处理完成的报修 / 全部报修 | ≥ 90% |
| 日均活跃用户 | 每日登录/操作平台的用户数 | — |

#### 关键埋点事件

| 事件名 | 触发时机 | 关键参数 |
| --- | --- | --- |
| `calendar_view_load` | 日历视图加载完成 | user_id, view_type, room_count, load_time_ms |
| `room_filter` | 使用筛选器 | user_id, filters |
| `booking_slot_click` | 点击日历空闲时段 | user_id, room_id, date, start_time, end_time |
| `booking_submit` | 提交预约表单 | user_id, room_id, date, start, end, duration_min |
| `booking_success` | 预约成功 | booking_id, room_id, duration_min |
| `booking_conflict` | 因冲突被拒绝 | user_id, room_id, start, end, suggested_slots |
| `booking_rule_fail` | 因规则校验失败 | user_id, fail_reason |
| `booking_cancel` | 取消预约 | booking_id, user_id, cancel_type, reason |
| `booking_modify` | 修改预约 | booking_id, changed_fields |
| `waitlist_join` | 加入候补队列 | user_id, room_id, date, start, end |
| `waitlist_promote` | 候补补位通知 | user_id, room_id, waitlist_rank |
| `waitlist_confirm` | 候补确认成功 | booking_id, user_id |
| `recurring_create` | 创建周期预约 | user_id, frequency, count |
| `template_use` | 使用会议模板 | user_id, template_id |
| `participant_add` | 添加参会人 | booking_id, participant_count |
| `sign_in` | 签到 | booking_id, user_id |
| `auto_release` | 自动释放时段 | booking_id, release_reason |
| `recommend_show` | 智能推荐结果展示 | user_id, recommend_count, source(rule/nl) |
| `recommend_accept` | 采用推荐会议室 | user_id, room_id, rank |
| `minutes_submit` | 提交会议纪要 | booking_id, has_attachment |
| `repair_submit` | 发起报修 | user_id, room_id, device_type |
| `repair_finish` | 报修完成 | repair_id, cost_time |
| `notification_send` | 发送通知 | user_id, type, channel |
| `admin_room_create` | 管理员创建会议室 | admin_id, room_id, capacity |
| `admin_booking_force_cancel` | 管理员强制取消 | admin_id, booking_id, reason |
| `dashboard_view` | 查看数据看板 | admin_id, time_range |

---

## 六、验收标准（DoD）

### 6.1 会议室管理

- [ ] 管理员可新增、编辑、删除会议室，字段完整；
- [ ] 状态为"维护中"或"停用"的会议室不出现在可预约日历中；
- [ ] 删除有未完成预约的会议室时被禁止，并提示先处理预约；
- [ ] 会议室列表支持按楼层、容量、设备、状态筛选；
- [ ] 支持 Excel 批量导入/导出会议室（V1.1）。

### 6.2 预约与冲突检测（核心）

- [ ] 员工可在日历视图点击空闲时段创建预约，30秒内完成；
- [ ] 预约规则生效：最小/最大时长、可预约天数、可预约时段、每日上限、部门配额均正确校验；
- [ ] **冲突检测100%准确**：同一会议室同一时段不允许两条已确认预约；
- [ ] 并发测试：100个请求同时预约同一会议室同一时段，仅1条成功，其余正确收到冲突提示；
- [ ] 冲突时展示相邻可用时段建议，点击可一键替换；
- [ ] 取消预约后时段即时释放，可被他人预约；
- [ ] 加入候补队列后，原预约取消时按顺序通知，确认后生成预约（V1.1）；
- [ ] 周期预约生成独立预约记录，冲突单次跳过并提示（V1.1）；
- [ ] 参会人添加后收到邀请通知（V1.1）。

### 6.3 智能推荐（V1.1）

- [ ] 输入人数/设备/时段返回 Top3 推荐会议室及推荐理由；
- [ ] 推荐结果全部满足硬性约束（容量、设备、可用性）；
- [ ] 推荐理由与实际特征一致（抽检20条无错误）；
- [ ] 推荐结果可直接一键进入预约表单。

### 6.4 会议纪要（V1.2）

- [ ] 已完成会议可填写纪要、上传附件；
- [ ] 纪要可归档并按会议室/时间/主题检索；
- [ ] 未填纪要的完成订单 24 小时内收到提醒。

### 6.5 设备报修（V1.2）

- [ ] 员工可发起报修并跟踪进度；
- [ ] 管理员可处理工单并填写处理说明；
- [ ] 报修中设备所在会议室可标记"维护中"并停止接单；
- [ ] 报修提交/完成时通知报修人。

### 6.6 我的预约与通知

- [ ] "即将开始/已完成/已取消"三个Tab数据正确；
- [ ] 未开始且在取消时限内可自行取消；超过时限取消按钮置灰并提示；
- [ ] 预约详情信息完整准确（含参会人、纪要入口）；
- [ ] 他人预约不显示主题和参会人（隐私保护）；
- [ ] 预约成功/开始前15分钟/取消均收到通知；
- [ ] 候补补位/参会邀请/报修等通知正确触达（V1.1+）。

### 6.7 管理后台

- [ ] 管理员可查看全部预约并按条件筛选；
- [ ] 管理员可强制取消未开始预约（需填原因），取消后通知预约人并触发候补；
- [ ] 预约规则可在管理端配置并即时生效；
- [ ] 数据看板展示使用率、热门会议室、预约趋势、闲置热力图，数据准确；
- [ ] 管理驾驶舱一屏展示核心指标（V1.1）；
- [ ] 管理员关键操作有日志记录。

### 6.8 非功能

- [ ] 日历视图 P95 加载 ≤ 1.5秒；
- [ ] 预约提交 P95 ≤ 1秒；
- [ ] 核心接口可用性 ≥ 99.5%；
- [ ] 管理员破坏性操作需二次确认；
- [ ] 自动释放/提醒等定时任务执行准确、可追溯（V1.1）。

### 6.9 新增功能验收（V1.3）

**信用分与爽约**

- [ ] 初始信用分 100；爽约扣 10、完成 +2、周恢复 +5，数值与配置一致；
- [ ] 信用记录完整：类型/变动值/前后分值/关联业务/时间；
- [ ] 信用分 < 阈值时仅能预约未来 3 天，回升后自动解除；
- [ ] 管理员手动调整需填原因并记录操作日志；
- [ ] 定时任务每周一 00:00 执行恢复，幂等不重复加分。

**会议室评价**

- [ ] 已完成会议 24h 内可评价，同一预约同一用户只能评一次；
- [ ] 会议室详情页平均分与评价列表准确；
- [ ] ≤3 星自动生成"待检查"，可一键转报修工单；
- [ ] 管理端评分分布与低分会议室统计正确。

**签到二维码**

- [ ] 已确认未签到的预约展示二维码，内容为加密预约编号；
- [ ] 可扫码时段内扫码确认后状态变"已签到"；
- [ ] 已签到/已取消/已过期扫码给出对应提示，不可重复签到；
- [ ] 二维码密钥服务端保存，前端不可逆推预约数据。

**智能问答助手**

- [ ] 支持"查空闲会议室/查我的预约/查会议室信息"三类意图的自然语言提问；
- [ ] 解析参数（日期/时段/人数/设备）与实际查询条件一致（抽检 20 条）；
- [ ] 回答基于真实查询结果，无编造数据；
- [ ] 解析失败/无结果时有降级提示，不报错；
- [ ] 每次问答写入日志（问题/回答/意图/耗时/成败），管理端可查。

---

## 七、数据库设计

### 7.1 用户与权限表

```sql
-- 用户表
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码(BCrypt)',
    phone VARCHAR(20) COMMENT '手机号',
    real_name VARCHAR(50) COMMENT '真实姓名',
    avatar VARCHAR(200) COMMENT '头像',
    dept_id BIGINT COMMENT '部门ID',
    role VARCHAR(20) DEFAULT 'user' COMMENT '角色: user/admin/dept_manager',
    status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '用户表';

-- 部门表
CREATE TABLE sys_dept (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dept_name VARCHAR(50) NOT NULL COMMENT '部门名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '部门表';

-- 部门配额表
CREATE TABLE dept_quota (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dept_id BIGINT NOT NULL COMMENT '部门ID',
    quota_type VARCHAR(20) NOT NULL COMMENT '类型: weekly次数 monthly时长',
    quota_value INT NOT NULL COMMENT '配额值',
    period VARCHAR(10) DEFAULT 'week' COMMENT '周期: week/month',
    enabled TINYINT DEFAULT 0 COMMENT '是否启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_dept_type (dept_id, quota_type)
) COMMENT '部门配额表';

-- 操作日志表
CREATE TABLE sys_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT '操作人ID',
    username VARCHAR(50) COMMENT '操作人',
    module VARCHAR(50) COMMENT '操作模块',
    operation VARCHAR(100) COMMENT '操作内容',
    method VARCHAR(200) COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    ip VARCHAR(50) COMMENT 'IP地址',
    status TINYINT DEFAULT 1 COMMENT '操作状态',
    error_msg TEXT COMMENT '错误信息',
    cost_time BIGINT COMMENT '耗时(ms)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '操作日志表';
```

### 7.2 会议室表

```sql
-- 会议室表
CREATE TABLE meeting_room (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_name VARCHAR(100) NOT NULL COMMENT '会议室名称',
    floor VARCHAR(50) COMMENT '所在楼层',
    location VARCHAR(200) COMMENT '具体位置',
    floor_map_x DECIMAL(6,2) COMMENT '楼层图X坐标(%)',
    floor_map_y DECIMAL(6,2) COMMENT '楼层图Y坐标(%)',
    capacity INT NOT NULL COMMENT '容纳人数',
    room_type VARCHAR(20) DEFAULT 'meeting' COMMENT '类型',
    equipment JSON COMMENT '配备设备',
    images JSON COMMENT '图片列表',
    description TEXT COMMENT '描述/备注',
    status TINYINT DEFAULT 1 COMMENT '状态: 0维护中 1可用 2停用',
    sort INT DEFAULT 0 COMMENT '排序',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_room_floor_name (floor, room_name)
) COMMENT '会议室表';

-- 会议室收藏表
CREATE TABLE room_favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    room_id BIGINT NOT NULL COMMENT '会议室ID',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_room (user_id, room_id)
) COMMENT '会议室收藏表';
```

### 7.3 预约表（核心）

```sql
-- 预约表
CREATE TABLE booking (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_no VARCHAR(32) NOT NULL COMMENT '预约编号',
    room_id BIGINT NOT NULL COMMENT '会议室ID',
    user_id BIGINT NOT NULL COMMENT '预约人ID',
    booking_date DATE NOT NULL COMMENT '预约日期',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    duration_min INT NOT NULL COMMENT '时长(分钟)',
    topic VARCHAR(100) NOT NULL COMMENT '会议主题',
    attendee_count INT DEFAULT 1 COMMENT '参会人数',
    equipment_need JSON COMMENT '设备需求',
    remark VARCHAR(500) COMMENT '备注',
    recurring_id BIGINT COMMENT '周期预约组ID',
    source VARCHAR(20) DEFAULT 'manual' COMMENT '来源: manual表单 recommend推荐 template模板 recurring周期 nl自然语言',
    status VARCHAR(20) DEFAULT 'confirmed' COMMENT '状态: pending待审批 confirmed已确认 signed已签到 completed已完成 cancelled已取消 expired已过期',
    cancel_reason VARCHAR(500) COMMENT '取消原因',
    cancel_by BIGINT COMMENT '取消人ID',
    cancel_time DATETIME COMMENT '取消时间',
    sign_time DATETIME COMMENT '签到时间',
    release_time DATETIME COMMENT '自动释放时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_booking_no (booking_no),
    KEY idx_room_date (room_id, booking_date),
    KEY idx_user (user_id),
    KEY idx_status (status),
    KEY idx_room_date_status (room_id, booking_date, status),
    KEY idx_recurring (recurring_id)
) COMMENT '预约表';

-- 周期预约组表
CREATE TABLE recurring_booking (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '创建人',
    room_id BIGINT NOT NULL COMMENT '会议室',
    frequency VARCHAR(10) NOT NULL COMMENT '频率: daily/weekly/monthly',
    start_date DATE NOT NULL COMMENT '起始日期',
    end_date DATE NOT NULL COMMENT '结束日期',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    topic VARCHAR(100) NOT NULL COMMENT '会议主题',
    attendee_count INT DEFAULT 1,
    equipment_need JSON,
    enabled TINYINT DEFAULT 1 COMMENT '是否有效',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '周期预约组表';

-- 参会人表
CREATE TABLE booking_participant (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL COMMENT '预约ID',
    user_id BIGINT NOT NULL COMMENT '参会人ID',
    status VARCHAR(20) DEFAULT 'invited' COMMENT '状态: invited已邀请 accepted接受 declined拒绝',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_booking_user (booking_id, user_id)
) COMMENT '参会人表';

-- 候补队列表
CREATE TABLE booking_waitlist (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id BIGINT NOT NULL COMMENT '会议室ID',
    booking_date DATE NOT NULL COMMENT '日期',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    user_id BIGINT NOT NULL COMMENT '候补人ID',
    rank_no INT NOT NULL COMMENT '候补顺序',
    status VARCHAR(20) DEFAULT 'waiting' COMMENT '状态: waiting等待 notified已通知 confirmed已确认 cancelled已放弃 expired已超时',
    notify_time DATETIME COMMENT '通知时间',
    confirm_deadline DATETIME COMMENT '确认截止时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_room_slot (room_id, booking_date, start_time, status),
    KEY idx_user (user_id)
) COMMENT '候补队列表';

-- 会议模板表
CREATE TABLE booking_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '创建人',
    template_name VARCHAR(50) NOT NULL COMMENT '模板名称',
    room_id BIGINT COMMENT '会议室ID(可选)',
    start_time TIME COMMENT '开始时间(可选)',
    end_time TIME COMMENT '结束时间(可选)',
    topic VARCHAR(100) COMMENT '会议主题',
    attendee_count INT DEFAULT 1,
    equipment_need JSON,
    remark VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_user (user_id)
) COMMENT '会议模板表';
```

> 冲突检测说明：时间段重叠不是等值关系，无法直接建唯一约束。采用 **Redis 分布式锁 + 应用层查询检测 + 数据库事务** 三重保障。对同一会议室同一日期的创建操作，先获取 `lock:booking:room:{roomId}:{date}` 锁，锁内查询是否有重叠预约，无重叠则插入，完成后释放锁。周期预约的冲突单次由同一机制校验。

### 7.4 会议纪要表

```sql
-- 会议纪要表
CREATE TABLE meeting_minutes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL COMMENT '预约ID',
    author_id BIGINT NOT NULL COMMENT '填写人',
    agenda JSON COMMENT '议题列表',
    content LONGTEXT COMMENT '纪要内容(富文本)',
    attachment_list JSON COMMENT '附件列表',
    submit_time DATETIME COMMENT '提交时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_booking (booking_id),
    KEY idx_author (author_id)
) COMMENT '会议纪要表';
```

### 7.5 设备报修表

```sql
-- 报修工单表
CREATE TABLE repair_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(32) NOT NULL COMMENT '工单编号',
    room_id BIGINT NOT NULL COMMENT '会议室ID',
    reporter_id BIGINT NOT NULL COMMENT '报修人ID',
    device_type VARCHAR(30) NOT NULL COMMENT '设备类型: projector/tv/whiteboard/aircon/light/other',
    fault_desc VARCHAR(500) NOT NULL COMMENT '故障描述',
    images JSON COMMENT '故障图片',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态: pending待处理 processing处理中 finished已完成',
    assignee_id BIGINT COMMENT '处理人ID',
    handle_desc VARCHAR(500) COMMENT '处理说明',
    finished_time DATETIME COMMENT '完成时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_room (room_id),
    KEY idx_status (status),
    KEY idx_reporter (reporter_id)
) COMMENT '报修工单表';
```

### 7.6 系统配置表

```sql
-- 系统配置表（key-value，存储预约规则等）
CREATE TABLE sys_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_name VARCHAR(100) COMMENT '配置名称',
    description VARCHAR(500) COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_config_key (config_key)
) COMMENT '系统配置表';

-- 初始化预约规则配置
INSERT INTO sys_config (config_key, config_value, config_name, description) VALUES
('booking.min_duration', '30', '最小预约时长(分钟)', '单次预约不可低于此时长'),
('booking.max_duration', '240', '最大预约时长(分钟)', '单次预约不可超过此时长'),
('booking.advance_days', '14', '可预约天数', '只能预约今天起N天内的时段'),
('booking.start_time', '08:00', '可预约开始时间', '每日可预约的最早时间'),
('booking.end_time', '22:00', '可预约结束时间', '每日可预约的最晚时间'),
('booking.time_granularity', '30', '时间粒度(分钟)', '开始/结束时间的最小粒度'),
('booking.cancel_limit_min', '15', '提前取消时限(分钟)', '会议开始前N分钟内不可自行取消'),
('booking.daily_limit', '3', '每人每日预约上限', '0表示不限'),
('booking.holiday_allowed', 'false', '节假日是否可预约', 'true/false'),
('booking.remind_min', '15', '开始前提醒(分钟)', '会议开始前N分钟发送提醒'),
('booking.auto_release', 'true', '是否开启自动释放', '未签到超时自动释放时段'),
('booking.auto_release_min', '15', '自动释放超时(分钟)', '会议开始后N分钟未签到则释放'),
('waitlist.enabled', 'true', '是否开启候补', '冲突时可加入候补队列'),
('waitlist.confirm_min', '10', '候补确认时限(分钟)', '通知后N分钟内确认，超时顺延'),
('waitlist.max_rank', '5', '候补队列上限', '同一时段每会议室候补人数上限'),
('recurring.max_count', '4', '周期预约最大次数', '单次周期预约最多生成次数'),
('dept_quota.enabled', 'false', '是否启用部门配额', '启用后按部门限制预约'),
('recommend.enabled', 'true', '是否启用智能推荐', '智能推荐会议室开关'),
('approval.enabled', 'false', '是否启用审批', '特定会议室预约需审批'),
('approval.timeout_hour', '2', '审批超时(小时)', '超时未审批自动拒绝');
```

### 7.7 通知表

```sql
-- 通知表
CREATE TABLE notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    type VARCHAR(30) NOT NULL COMMENT '类型: success预约成功 remind开始提醒 cancel取消通知 invite参会邀请 waitlist候补通知 repair报修通知 minutes纪要提醒 system系统通知',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT COMMENT '内容',
    biz_type VARCHAR(30) COMMENT '业务类型: booking/waitlist/repair/minutes',
    biz_id BIGINT COMMENT '业务ID',
    channel VARCHAR(20) DEFAULT 'inapp' COMMENT '渠道: inapp站内 email邮件 wecom企业微信',
    send_status TINYINT DEFAULT 1 COMMENT '发送状态',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读: 0未读 1已读',
    read_time DATETIME COMMENT '阅读时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_user_read (user_id, is_read),
    KEY idx_user_create (user_id, create_time)
) COMMENT '通知表';
```

### 7.8 工作日历表

```sql
-- 工作日历表（节假日/特殊休息日）
CREATE TABLE work_calendar (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    calendar_date DATE NOT NULL COMMENT '日期',
    date_type TINYINT NOT NULL COMMENT '类型: 1工作日 2周末 3节假日 4调休工作日 5特殊休息日',
    holiday_name VARCHAR(50) COMMENT '节假日名称',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_calendar_date (calendar_date)
) COMMENT '工作日历表';
```

### 7.9 信用分记录表（新增）

```sql
-- 用户表增加信用分字段
ALTER TABLE sys_user ADD COLUMN credit_score INT DEFAULT 100 COMMENT '信用分' AFTER role;

-- 信用分记录表
CREATE TABLE credit_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    change_type VARCHAR(30) NOT NULL COMMENT '类型: no_show爽约 complete完成 weekly_recover周恢复 cancel_late逾期取消 admin_adjust管理员调整',
    change_value INT NOT NULL COMMENT '变动值(正加负减)',
    before_score INT NOT NULL COMMENT '变动前分值',
    after_score INT NOT NULL COMMENT '变动后分值',
    biz_type VARCHAR(30) COMMENT '关联业务: booking',
    biz_id BIGINT COMMENT '关联业务ID',
    remark VARCHAR(200) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_user (user_id),
    KEY idx_user_time (user_id, create_time)
) COMMENT '信用分记录表';
```

### 7.10 会议室评价表（新增）

```sql
CREATE TABLE room_rating (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL COMMENT '预约ID',
    room_id BIGINT NOT NULL COMMENT '会议室ID',
    user_id BIGINT NOT NULL COMMENT '评价人ID',
    rating TINYINT NOT NULL COMMENT '评分1-5星',
    tags JSON COMMENT '标签: 环境好/设备好用/隔音差/空调问题等',
    comment VARCHAR(500) COMMENT '评价内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_booking_user (booking_id, user_id),
    KEY idx_room (room_id)
) COMMENT '会议室评价表';

-- 低分检查记录（≤3星自动生成，可转报修）
CREATE TABLE room_rating_check (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rating_id BIGINT NOT NULL COMMENT '评价ID',
    room_id BIGINT NOT NULL COMMENT '会议室ID',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态: pending待检查 converted已转报修 closed已关闭',
    repair_id BIGINT COMMENT '转出的报修工单ID',
    remark VARCHAR(200),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_rating (rating_id)
) COMMENT '低分检查记录表';
```

### 7.11 智能问答日志表（新增）

```sql
CREATE TABLE chatbot_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT '提问用户ID',
    question TEXT NOT NULL COMMENT '用户问题',
    answer TEXT COMMENT '回答内容',
    intent VARCHAR(30) COMMENT '解析意图',
    params JSON COMMENT '解析参数',
    result_count INT COMMENT '查询结果条数',
    success TINYINT DEFAULT 1 COMMENT '是否成功',
    cost_ms BIGINT COMMENT '耗时(ms)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_user (user_id),
    KEY idx_time (create_time)
) COMMENT '智能问答日志表';
```

---

## 八、接口设计

### 8.1 接口规范

- 基础路径：`/api/v1`
- 认证方式：Bearer Token (JWT)
- 数据格式：JSON
- 响应格式：

```json
{
    "code": 200,
    "message": "success",
    "data": {}
}
```

- 分页参数：`pageNum` / `pageSize`，返回 `{ list, total, pageNum, pageSize }`
- 错误码：`400`参数错误、`401`未认证、`403`无权限、`409`冲突（预约冲突专用）、`500`系统错误

### 8.2 核心接口列表

#### 认证接口

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/v1/auth/register | POST | 注册 |
| /api/v1/auth/login | POST | 登录（返回token） |
| /api/v1/auth/userinfo | GET | 获取当前用户信息 |
| /api/v1/auth/logout | POST | 登出 |

#### 会议室接口（前台）

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/v1/rooms | GET | 获取会议室列表（支持筛选） |
| /api/v1/rooms/{id} | GET | 获取会议室详情 |
| /api/v1/rooms/{id}/availability | GET | 获取指定日期可用时段 |
| /api/v1/rooms/calendar | GET | 日历视图数据 |
| /api/v1/rooms/floor-map | GET | 楼层平面图数据（V1.2） |
| /api/v1/rooms/recommend | POST | 智能推荐会议室（人数/设备/时段→Top3） |
| /api/v1/rooms/{id}/favorite | POST | 收藏会议室 |
| /api/v1/rooms/{id}/favorite | DELETE | 取消收藏 |

#### 预约接口（前台）

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/v1/bookings | POST | 创建预约（含规则校验+冲突检测） |
| /api/v1/bookings/recurring | POST | 创建周期预约 |
| /api/v1/bookings/mine | GET | 我的预约列表 |
| /api/v1/bookings/{id} | GET | 预约详情（含参会人/纪要） |
| /api/v1/bookings/{id}/cancel | PUT | 取消预约（触发候补） |
| /api/v1/bookings/{id} | PUT | 修改预约 |
| /api/v1/bookings/{id}/sign | POST | 签到 |
| /api/v1/bookings/{id}/participants | POST | 添加参会人 |
| /api/v1/bookings/{id}/participants/{uid} | DELETE | 移除参会人 |
| /api/v1/bookings/nl/parse | POST | 自然语言预约解析（演进） |
| /api/v1/bookings/calendar.ics | GET | 导出我的预约 iCal 订阅 |

#### 候补接口

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/v1/waitlist | POST | 加入候补队列 |
| /api/v1/waitlist/mine | GET | 我的候补列表 |
| /api/v1/waitlist/{id} | DELETE | 退出候补 |
| /api/v1/waitlist/{id}/confirm | POST | 候补确认（生成预约） |

#### 会议纪要接口

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/v1/bookings/{id}/minutes | POST | 提交会议纪要 |
| /api/v1/bookings/{id}/minutes | GET | 获取会议纪要 |
| /api/v1/minutes/search | GET | 纪要归档检索 |
| /api/v1/minutes/{id}/export | GET | 纪要导出 |

#### 报修接口

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/v1/repairs | POST | 发起报修 |
| /api/v1/repairs/mine | GET | 我的报修列表 |
| /api/v1/repairs/{id} | GET | 报修详情 |

#### 通知接口

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/v1/notifications | GET | 通知列表 |
| /api/v1/notifications/{id}/read | PUT | 标记已读 |
| /api/v1/notifications/read-all | PUT | 全部已读 |
| /api/v1/notifications/unread-count | GET | 未读数量 |

#### 会议模板接口

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/v1/templates | GET | 我的模板列表 |
| /api/v1/templates | POST | 创建模板 |
| /api/v1/templates/{id} | PUT | 编辑模板 |
| /api/v1/templates/{id} | DELETE | 删除模板 |

#### 后台-会议室管理

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/v1/admin/rooms | GET | 会议室列表（含今日预约数） |
| /api/v1/admin/rooms | POST | 新增会议室 |
| /api/v1/admin/rooms/{id} | PUT | 编辑会议室 |
| /api/v1/admin/rooms/{id} | DELETE | 删除会议室 |
| /api/v1/admin/rooms/{id}/status | PUT | 修改状态 |
| /api/v1/admin/rooms/import | POST | Excel批量导入 |
| /api/v1/admin/rooms/export | GET | 导出会议室 |

#### 后台-预约管理

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/v1/admin/bookings | GET | 全部预约列表（多条件筛选） |
| /api/v1/admin/bookings/{id}/force-cancel | PUT | 强制取消（触发候补） |
| /api/v1/admin/bookings/create-by-admin | POST | 代客预约 |
| /api/v1/admin/bookings/export | GET | 导出预约数据 |
| /api/v1/admin/waitlist | GET | 候补队列管理 |
| /api/v1/admin/waitlist/{id}/promote | POST | 手动补位 |

#### 后台-报修管理

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/v1/admin/repairs | GET | 报修工单列表 |
| /api/v1/admin/repairs/{id}/assign | PUT | 转派/接单 |
| /api/v1/admin/repairs/{id}/finish | PUT | 完成工单 |

#### 后台-数据统计

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/v1/admin/stats/usage | GET | 会议室使用率统计 |
| /api/v1/admin/stats/hot-rooms | GET | 热门会议室Top5 |
| /api/v1/admin/stats/trend | GET | 预约量趋势 |
| /api/v1/admin/stats/idle-heatmap | GET | 闲置时段热力图 |
| /api/v1/admin/stats/dept | GET | 部门使用统计 |
| /api/v1/admin/stats/waitlist | GET | 候补转化统计 |
| /api/v1/admin/stats/repair | GET | 报修统计 |
| /api/v1/admin/stats/dashboard | GET | 管理驾驶舱汇总数据 |
| /api/v1/admin/stats/predict | GET | 使用率预测（演进） |

#### 后台-系统设置

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/v1/admin/config/booking-rules | GET/PUT | 预约规则配置 |
| /api/v1/admin/config/waitlist | GET/PUT | 候补规则配置 |
| /api/v1/admin/config/recurring | GET/PUT | 周期预约配置 |
| /api/v1/admin/config/approval | GET/PUT | 审批配置 |
| /api/v1/admin/config/work-calendar | GET/POST/DELETE | 工作日历管理 |
| /api/v1/admin/config/notification | GET/PUT | 通知配置 |
| /api/v1/admin/dept-quotas | GET/POST/PUT | 部门配额管理 |

#### 后台-用户与权限

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/v1/admin/users | GET | 用户列表 |
| /api/v1/admin/users/{id}/status | PUT | 禁用/启用 |
| /api/v1/admin/users/{id}/reset-password | PUT | 重置密码 |
| /api/v1/admin/users/{id}/role | PUT | 分配角色 |
| /api/v1/admin/depts | CRUD | 部门管理 |
| /api/v1/admin/operation-logs | GET | 操作日志 |
| /api/v1/admin/users/{id}/credit-records | GET | 用户信用记录 |
| /api/v1/admin/users/{id}/credit | PUT | 手动调整信用分 |
| /api/v1/admin/config/credit | GET/PUT | 信用分规则配置 |
| /api/v1/admin/ratings | GET | 评价列表（筛选/统计） |
| /api/v1/admin/ratings/check | GET | 低分检查记录 |
| /api/v1/admin/ratings/check/{id}/convert | PUT | 低分转报修工单 |
| /api/v1/admin/chatbot-logs | GET | 问答日志（审计） |

#### 前台-信用分与评价（新增）

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/v1/credit/score | GET | 我的信用分与等级 |
| /api/v1/credit/records | GET | 我的信用分记录（分页） |
| /api/v1/bookings/{id}/rating | POST | 提交评价（1-5星+标签+文字） |
| /api/v1/rooms/{id}/ratings | GET | 会议室评价列表与平均分 |
| /api/v1/bookings/{id}/qrcode | GET | 获取签到二维码（加密预约编号） |
| /api/v1/bookings/scan/{token} | GET | 扫码校验并进入签到确认页 |
| /api/v1/bookings/{id}/sign-by-qrcode | POST | 二维码确认签到 |

#### 前台-智能问答（新增）

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/v1/chatbot/ask | POST | 智能问答（问题→回答） |

---

## 九、项目结构

### 9.1 仓库目录结构

```
meeting-room-booking/
├── meeting-room-web/             # 前台用户端 (Vue3)
│   ├── src/
│   │   ├── api/                  # 接口定义
│   │   ├── assets/
│   │   ├── components/
│   │   │   ├── BookingCalendar.vue   # 日历组件封装
│   │   │   ├── BookingForm.vue       # 预约表单弹窗
│   │   │   ├── RoomFilter.vue        # 会议室筛选器
│   │   │   ├── RoomRecommend.vue     # 智能推荐组件
│   │   │   ├── WaitlistPanel.vue     # 候补队列组件
│   │   │   ├── MinutesEditor.vue     # 会议纪要编辑器
│   │   │   ├── RepairForm.vue        # 报修表单
│   │   │   ├── CreditBadge.vue       # 信用分徽标组件
│   │   │   ├── RatingForm.vue        # 会议室评价表单
│   │   │   ├── QrCodeCard.vue        # 签到二维码组件
│   │   │   └── ChatBot.vue           # 智能问答悬浮助手
│   │   ├── composables/
│   │   ├── router/
│   │   ├── stores/               # Pinia (auth/booking/room/waitlist)
│   │   ├── styles/
│   │   ├── types/
│   │   ├── utils/
│   │   └── views/
│   │       ├── CalendarView.vue      # 日历视图(首页)
│   │       ├── FloorMapView.vue      # 楼层平面视图
│   │       ├── MyBookings.vue        # 我的预约
│   │       ├── BookingDetail.vue     # 预约详情(含纪要/参会人)
│   │       ├── WaitlistView.vue      # 我的候补
│   │       ├── MinutesView.vue       # 纪要归档
│   │       ├── RepairsView.vue       # 我的报修
│   │       ├── TemplatesView.vue     # 会议模板
│   │       ├── Notifications.vue     # 通知中心
│   │       ├── Profile.vue           # 个人中心
│   │       ├── CreditView.vue        # 我的信用分与记录
│   │       └── RatingView.vue        # 会议室评价与详情
│   ├── package.json
│   └── vite.config.ts
│
├── meeting-room-admin/           # 后台管理端 (Vue3)
│   ├── src/
│   │   ├── api/
│   │   ├── components/
│   │   ├── layout/
│   │   ├── router/
│   │   ├── stores/
│   │   └── views/
│   │       ├── room/RoomManage.vue       # 会议室管理
│   │       ├── booking/BookingManage.vue # 预约管理
│   │       ├── booking/WaitlistManage.vue# 候补管理
│   │       ├── repair/RepairManage.vue   # 报修管理
│   │       ├── stats/StatsDashboard.vue  # 数据看板
│   │       ├── stats/DashboardScreen.vue # 管理驾驶舱(大屏)
│   │       ├── config/BookingRules.vue   # 预约规则配置
│   │       ├── config/WaitlistRules.vue  # 候补规则配置
│   │       ├── config/QuotaConfig.vue    # 部门配额
│   │       ├── config/WorkCalendar.vue   # 工作日历
│   │       ├── user/UserManage.vue       # 用户管理
│   │       ├── user/UserManage.vue       # 用户管理
│   │       ├── credit/CreditRuleConfig.vue # 信用分规则配置
│   │       ├── credit/CreditRecords.vue  # 信用记录查询
│   │       ├── rating/RatingManage.vue   # 评价管理(统计/低分检查)
│   │       ├── chatbot/ChatbotLogs.vue   # 问答日志
│   │       └── log/OperationLog.vue      # 操作日志
│   ├── package.json
│   └── vite.config.ts
│
├── meeting-room-signage/         # 门牌屏/大屏 (Vue3, 独立页面)
│   ├── src/views/RoomScreen.vue      # 会议室门牌屏
│   └── src/views/DashboardScreen.vue # 管理驾驶舱大屏
│
├── meeting-room-server/          # 后端服务 (Spring Boot)
│   ├── src/main/java/com/example/meeting/
│   │   ├── config/               # 配置类 (Security/Redis/Knife4j/定时任务)
│   │   ├── controller/           # 控制器 (前台/后台)
│   │   │   ├── front/            # 前台接口
│   │   │   └── admin/            # 后台接口
│   │   ├── service/              # 服务层
│   │   │   ├── BookingService.java       # 预约服务(冲突检测/周期/候补联动)
│   │   │   ├── WaitlistService.java      # 候补服务
│   │   │   ├── RecommendService.java     # 智能推荐服务
│   │   │   ├── MinutesService.java       # 会议纪要服务
│   │   │   ├── RepairService.java        # 报修服务
│   │   │   ├── RoomService.java          # 会议室服务
│   │   │   ├── NotificationService.java  # 通知服务(多渠道)
│   │   │   ├── StatsService.java         # 统计服务
│   │   │   ├── ScheduleTaskService.java  # 定时任务(自动释放/提醒/纪要提醒)
│   │   │   └── ConfigService.java        # 配置服务
│   │   ├── mapper/               # Mapper接口
│   │   ├── entity/               # 实体类
│   │   ├── dto/ / vo/            # 数据传输/视图对象
│   │   ├── common/               # 公共模块
│   │   ├── interceptor/          # 拦截器
│   │   └── MeetingApplication.java
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── mapper/
│   │   └── db/init.sql           # 初始化SQL(含示例数据)
│   └── pom.xml
│
├── docs/                         # 项目文档
│   ├── PRD.md
│   ├── API.md
│   └── DEPLOY.md
│
├── deploy/                       # 部署配置
│   ├── docker-compose.yml        # MySQL + Redis
│   └── nginx/
│
└── README.md
```

---

## 十、开发计划

### 10.1 阶段划分

| 阶段 | 周期 | 主要任务 |
|------|------|---------|
| 第一阶段：基础搭建 | 1周 | 项目初始化、前后端框架搭建、数据库设计（全量表）、Docker环境 |
| 第二阶段：用户与会议室 | 2周 | 登录注册、权限、部门、会议室CRUD、收藏、前台会议室列表 |
| 第三阶段：预约核心 | 3周 | 日历视图、创建预约、规则校验、冲突检测(含并发锁)、我的预约、取消修改、通知 |
| 第四阶段：增强功能 | 2周 | 周期预约、参会人邀请、会议模板、签到+自动释放(定时任务)、排队候补 |
| 第五阶段：智能与管理 | 2周 | 智能推荐(规则版)、预约管理、候补管理、审批、数据看板+热力图、驾驶舱 |
| 第六阶段：会议闭环 | 2周 | 会议纪要(归档/检索/提醒)、设备报修(工单/状态联动)、iCal订阅、多渠道通知 |
| 第七阶段：测试上线 | 2周 | 功能测试、并发压测(冲突检测)、LLM评估(如做NL预约)、性能优化、部署上线 |
| 第八阶段：扩展功能 | 2周 | 信用分与爽约机制、会议室评价、签到二维码、智能问答助手（ChatBot） |

**总计：16周**

### 10.2 里程碑

| 里程碑 | 时间 | 交付物 |
|-------|------|--------|
| M1：框架就绪 | 第1周末 | 前后端框架、数据库全量表、Docker环境 |
| M2：MVP版本 | 第6周末 | 预约核心闭环（日历+预约+冲突检测+我的预约+通知） |
| M3：增强版 | 第8周末 | 周期预约、参会人、候补、签到释放全部可用 |
| M4：全功能版 | 第10周末 | 智能推荐、审批、看板、纪要、报修、驾驶舱 |
| M5：内测版 | 第12周末 | 全功能联调通过，压测达标 |
| M6：正式上线 | 第14周末 | 生产环境部署、示例数据、答辩准备 |
| M7：扩展版 | 第16周末 | 信用分、评价、二维码、智能问答全部可用 |

### 10.3 答辩演示建议

- **核心演示**：冲突拦截（两人抢同一时段）+ 并发压测结果（100并发仅1条成功）；
- **亮点演示**：排队候补自动补位、周期预约一键生成、闲置热力图、智能推荐会议室、管理驾驶舱；
- **扩展亮点（V1.3）**：信用分爽约治理（演示爽约扣分→低分限制→周恢复闭环）、会议室评价→低分转报修、二维码扫码签到、智能问答助手（"明天下午有哪些8人带投影的会议室空闲"）；
- **演进演示（可选）**：自然语言预约（"周五下午3点订8人带投影的会议室"）。

---

## 十一、风险评估

| 风险 | 影响 | 概率 | 应对措施 |
|------|------|------|---------|
| 并发预约产生重复 | 高 | 中 | Redis分布式锁 + 数据库行锁 + 应用层检测三重保障；100并发压测验证 |
| 功能范围膨胀导致延期 | 高 | 高 | 已按优先级分四期（MVP→V1.1→V1.2→演进）；每期可独立交付演示 |
| 周期预约与冲突规则复杂 | 中 | 中 | 冲突单次跳过机制；周期统一管理；专项测试用例覆盖 |
| 候补补位时序问题 | 中 | 中 | 候补确认时限 + 事务保证"取消→通知→确认"链路；并发取消时用分布式锁 |
| 定时任务可靠性（自动释放/提醒） | 中 | 中 | 任务执行记录表；失败重试；任务幂等设计 |
| 管理员误操作(强制取消/删除) | 中 | 低 | 二次确认 + 操作日志 + 通知受影响人 + 触发候补兜底 |
| 自然语言预约(LLM)成本与幻觉 | 高 | 低 | 放演进期；结构化输出 + 后端校验 + 表单兜底；不阻塞主流程 |
| 会议纪要/报修附件安全 | 中 | 低 | 上传类型/大小校验；存OSS私有桶；访问鉴权 |
| 冷启动无会议室数据 | 低 | 高 | 初始化示例数据脚本；Excel批量导入；管理员快速录入 |
| 多租户改造困难 | 中 | 低 | 放演进期；表结构预留 tenant_id 字段设计 |
| 信用分规则误伤正常用户 | 中 | 低 | 规则全部可配置；变动记录留痕可追溯；管理员可手动修正并附原因 |
| 智能问答LLM返回错误信息 | 中 | 中 | 结构化输出 + 参数白名单校验 + 查询结果兜底（LLM只负责解析和润色，不直接生成数据）；抽检日志 |
| 签到二维码被冒用 | 中 | 低 | 二维码加密签名+限时有效+仅本人可操作；扫码需登录态 |

---

## 十二、附录

### 12.1 术语表

| 术语 | 解释 |
|------|------|
| PRD | Product Requirements Document，产品需求文档 |
| RBAC | Role-Based Access Control，基于角色的访问控制 |
| JWT | JSON Web Token，JSON网络令牌 |
| DoD | Definition of Done，完成定义/验收标准 |
| MVP | Minimum Viable Product，最小可行产品 |
| 冲突检测 | 校验同一会议室的预约时间段不重叠的机制 |
| 排队候补 | 目标时段被占时排队，取消后自动补位的机制 |
| 周期预约 | 按日/周/月重复生成的预约组 |
| 自动释放 | 未签到超时自动释放时段的机制（no-show） |
| 智能推荐 | 基于规则与历史习惯推荐会议室的机制 |
| 管理驾驶舱 | 一屏汇总核心指标的大屏看板 |
| iCal | 通用日历交换格式（.ics），用于日历订阅 |
| 分布式锁 | Redis实现的跨进程锁，用于并发预约保障 |
| 信用分 | 治理爽约行为的用户信用评分机制，影响预约权限 |
| 智能问答助手 | 基于LLM解析自然语言问题并调用平台查询能力的问答服务 |

### 12.2 参考文档

- Vue 3 官方文档：https://vuejs.org/
- Spring Boot 官方文档：https://spring.io/projects/spring-boot
- MyBatis-Plus 官方文档：https://baomidou.com/
- FullCalendar 官方文档：https://fullcalendar.io/
- ECharts 官方文档：https://echarts.apache.org/
- Redis 官方文档：https://redis.io/
- EasyExcel 官方文档：https://easyexcel.opensource.alibaba.com/
- Spring AI（演进，自然语言预约）：https://spring.io/projects/spring-ai

---

*本文档为会议室预约管理平台全功能版 PRD，覆盖产品需求与技术实现全貌，适用于毕业设计答辩与研发实施。功能按优先级分四期交付，每期可独立演示；各功能优先级与验收标准在研发排期前由产品与研发共同确认。*
