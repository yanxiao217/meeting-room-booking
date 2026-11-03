# 会议室预约管理平台

## 产品需求文档 (PRD)



| 文档信息 | 内容                |
| ---- | ----------------- |
| 项目名称 | 会议室预约管理平台         |
| 文档版本 | v2.0（融合版）         |
| 创建日期 | 2026-09-14        |
| 文档状态 | 初稿                |
| 选题来源 | 选题参考・企业管理类・第 78 题 |



***

## 一、项目概述

### 1.1 项目背景

企业、园区、校园和联合办公空间的会议室资源普遍存在以下痛点：



* **预约方式原始**：靠微信群喊话、纸质登记本、Excel 表格，信息不同步，谁先谁后说不清；

* **冲突频发**：两人同时订了同一间会议室的同一时段，到了现场才发现，直接影响会议；

* **资源不透明**：员工不知道哪些会议室空闲、有什么设备、能坐多少人，找会议室全靠跑楼；

* **管理无数据**：管理员不知道会议室使用率高低、哪些时段闲置、哪些设备坏了，无法优化资源配置。

### 1.2 项目定位

本项目定位为 **轻量化会议室预约管理全栈项目**，采用前后端分离架构，融合日历可视化预约、时间段冲突自动检测、可配置预约规则、使用数据统计看板等能力，打造新一代会议室资源管理平台。

核心差异化：**时间段冲突自动检测（应用层 + 数据库层双重保障）+ 可视化日历视图 + 全规则可配置**。

### 1.3 目标用户



| 用户角色      | 描述                       | 核心诉求                      |
| --------- | ------------------------ | ------------------------- |
| 普通员工      | 需要预约会议室开会的用户             | 快速找到空闲会议室、预约不冲突、方便管理自己的预约 |
| 管理员       | 负责会议室资源管理与预约秩序的行政 / 运维人员 | 会议室统一管理、预约可审可控、使用数据可查     |
| 部门负责人（演进） | 可查看本部门预约情况的管理者           | 部门会议资源使用概览                |

> 一个用户可同时拥有员工和管理员身份，管理员在员工功能基础上额外拥有管理权限。

### 1.4 用户画像（示例）

**员工・小张，26 岁，产品经理**



* 每周要开 3-5 个会，经常临时找会议室；

* 烦透了 "到了会议室发现被占了"，希望预约后有保障；

* 开会需要投影仪和白板，希望预约时能筛选设备。

**管理员・李姐，38 岁，行政主管**



* 管理公司 3 层楼共 12 间会议室；

* 经常接到员工投诉 "会议室被占" 和 "大会议室没人用却订不到小会议室"；

* 希望用数据说话，知道哪些会议室该扩容、哪些该改用途。

### 1.5 典型场景故事

**场景一：员工快速预约会议室**

> 周三上午，小张要在下午 2 点和研发开需求评审会，需要 8 人会议室 + 投影仪。他打开平台，日历视图显示各会议室当天的占用情况。他筛选 "容量≥8 + 有投影仪"，看到 3 楼 "创新厅" 下午 2:00-3:30 空闲，点击该时段，填写会议主题 "V2.0 需求评审"，提交。系统提示 "预约成功"，并在会议开始前 15 分钟推送提醒。

**场景二：冲突自动拦截**

> 小王和小李几乎同时打开平台，都想订 "创新厅" 明天上午 10:00-11:30。小王先 2 秒提交成功。小李提交时，系统实时检测到该时段已被预约，红色提示 "该时段已被预约，建议选择 11:30-13:00 或更换会议室"，并展示相邻可用时段，小李一键改到 11:30，避免了现场冲突。

**场景三：管理员查看使用率并优化资源**

> 月底，李姐打开管理后台的数据看板，看到 1 楼 "洽谈室" 使用率仅 15%，而 3 楼 "创新厅" 使用率高达 92%、经常订不到。她决定把 "洽谈室" 改造成可容纳 8 人的小型会议室，并在平台上更新了会议室信息。同时她发现周五下午所有会议室使用率都低于 30%，考虑在周五下午开放为自由讨论区。

### 1.6 核心价值主张



| 角色      | 核心价值                            |
| ------- | ------------------------------- |
| 员工（预约者） | 30 秒内完成预约；一眼看清哪些会议室空闲；不再撞单      |
| 管理员     | 会议室 / 设备统一管理；预约可审可控；使用率数据驱动资源优化 |
| 组织      | 提升会议室利用率，减少资源浪费，降低行政沟通成本        |



***

## 二、项目目标

### 2.1 核心目标



1. **构建一体化会议室预约平台**：整合会议室资源、预约管理、通知提醒；

2. **实现日历可视化预约**：提供周视图 / 日视图，直观展示空闲与占用；

3. **打造零冲突预约体验**：应用层实时检测 + 数据库层并发锁，双重保障不重复预约；

4. **实现全规则可配置**：预约时长、可预约天数、时段、取消时限等均可后台配置；

5. **建立数据可视化体系**：为管理员提供使用率、热门会议室、预约趋势等数据支撑。

### 2.2 量化指标



| 指标       | 目标值               |
| -------- | ----------------- |
| 预约成功率    | ≥ 98%             |
| 冲突漏检率    | 0（硬性指标）           |
| 预约操作时长   | ≤ 30 秒            |
| 日历视图加载速度 | P95 ≤ 1.5 秒       |
| 预约提交响应   | P95 ≤ 1 秒         |
| 核心接口可用性  | ≥ 99.5%           |
| 并发预约支持   | 100 并发同一时段仅 1 条成功 |
| 会议室整体使用率 | ≥ 50%             |



***

## 三、系统架构设计

### 3.1 总体架构



```
┌─────────────────────────────────────────────────────────────┐

│                      用户层 (User Layer)                      │

│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │

│  │   PC Web    │  │  Mobile Web │  │  平板适配    │          │

│  └─────────────┘  └─────────────┘  └─────────────┘          │

└─────────────────────────────────────────────────────────────┘

&#x20;                             │

&#x20;                             ▼

┌─────────────────────────────────────────────────────────────┐

│                    接入层 (Access Layer)                      │

│              Nginx反向代理 + 负载均衡 + SSL终止                │

└─────────────────────────────────────────────────────────────┘

&#x20;                             │

&#x20;                             ▼

┌─────────────────────────────────────────────────────────────┐

│                   应用层 (Application Layer)                  │

│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │

│  │  前台用户端   │  │  后台管理端   │  │  后端服务     │       │

│  │   (Vue3)     │  │   (Vue3)     │  │ (Spring Boot) │       │

│  └──────────────┘  └──────────────┘  └──────────────┘       │

└─────────────────────────────────────────────────────────────┘

&#x20;                             │

&#x20;                             ▼

┌─────────────────────────────────────────────────────────────┐

│                    服务层 (Service Layer)                     │

│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐    │

│  │ 用户服务  │  │ 会议室服务│  │ 预约服务  │  │ 通知服务  │    │

│  └──────────┘  └──────────┘  └──────────┘  └──────────┘    │

│  ┌──────────┐  ┌──────────┐  ┌──────────┐                    │

│  │ 冲突检测  │  │ 规则配置  │  │ 统计服务  │                    │

│  └──────────┘  └──────────┘  └──────────┘                    │

└─────────────────────────────────────────────────────────────┘

&#x20;                             │

&#x20;                             ▼

┌─────────────────────────────────────────────────────────────┐

│                    数据层 (Data Layer)                        │

│              ┌──────────┐  ┌──────────┐                     │

│              │  MySQL   │  │   OSS    │                     │

│              │ (关系库) │  │ (文件存储)│                     │

│              └──────────┘  └──────────┘                     │

└─────────────────────────────────────────────────────────────┘
```

### 3.2 技术架构

#### 前端技术栈



| 层级   | 技术选择               | 说明                     |
| ---- | ------------------ | ---------------------- |
| 框架   | Vue 3 + TypeScript | 主流前端框架，类型安全            |
| UI 库 | Element Plus       | 企业级 UI 组件库             |
| 构建工具 | Vite               | 快速构建工具                 |
| 状态管理 | Pinia              | Vue 3 推荐状态管理           |
| 路由   | Vue Router 4       | 官方路由解决方案               |
| 日历组件 | FullCalendar       | 成熟的日历调度组件，支持周 / 日视图、拖拽 |
| 图表   | ECharts            | 管理端数据看板可视化             |
| HTTP | Axios              | 接口请求封装                 |

#### 后端技术栈



| 层级     | 技术选择                  | 说明                 |
| ------ | --------------------- | ------------------ |
| 框架     | Spring Boot 3         | Java 企业级框架         |
| JDK    | Java 17               | LTS 长期支持版本         |
| ORM    | MyBatis-Plus          | 数据库操作，简化 CRUD      |
| 数据库    | MySQL 8               | 关系型数据库             |
| API 文档 | Knife4j               | Swagger 增强，接口在线调试  |
| 认证     | JWT + Spring Security | 无状态认证 + RBAC 权限    |
| 工具库    | Hutool                | Java 工具类库          |

### 3.3 部署架构



* 前端：Nginx 托管静态资源，反向代理后端 API；

* 后端：Spring Boot Jar 包部署，支持多实例水平扩展；

* 数据：MySQL 主从（演进）；

* 本地开发：Docker Compose 一键启动 MySQL。



***

## 四、功能需求

### 4.1 前台用户端功能

#### 4.1.1 日历视图模块

**功能描述**：提供可视化日历视图，直观展示各会议室的空闲与占用情况，支持筛选和快速预约。



| 功能点    | 描述                                | 优先级 |
| ------ | --------------------------------- | --- |
| 周视图    | 默认视图，横向为周一至周日，纵向为时间段，每个会议室一列或一组   | P0  |
| 日视图    | 单日详细视图，展示所有会议室的时间轴                | P0  |
| 日期切换   | 上一周 / 下一周 / 回到今天                  | P0  |
| 会议室筛选  | 按楼层、容量、设备、关键词筛选                   | P0  |
| 空闲时段展示 | 已预约时段显示彩色色块，空闲时段为空白可点击            | P0  |
| 预约信息浮层 | 鼠标悬停已预约色块，显示会议主题（自己的）/ "已占用"（他人的） | P0  |
| 点击快速预约 | 点击空闲时段，弹出预约表单（预填会议室和时间段）          | P0  |
| 拖拽选时段  | 点击开始时间拖动到结束时间，快速选择区间              | P1  |
| 维护中标识  | 维护中 / 停用的会议室置灰并标注，不可点击            | P0  |
| 我的预约高亮 | 自己的预约色块深色显示，他人的浅色显示               | P0  |
| 列表视图   | 按时间顺序展示预约列表（演进）                   | P2  |

**交互说明**：



* 顶部有日期切换器和会议室筛选器；

* 他人预约仅显示 "已占用"，不泄露会议主题和参会人（隐私保护）；

* 移动端周视图切换为单日视图或可横向滑动；

* 日历加载时显示骨架屏，不白屏。

#### 4.1.2 预约管理模块

**功能描述**：用户可创建、查看、修改、取消预约，系统自动进行规则校验和冲突检测。



| 功能点     | 描述                                | 优先级 |
| ------- | --------------------------------- | --- |
| 创建预约    | 选择会议室、日期、时间段，填写主题、参会人数、设备需求、备注    | P0  |
| 规则校验    | 校验时长、可预约天数、可预约时段、每日上限等规则          | P0  |
| 冲突检测    | 实时检测同一会议室同一时段是否已被预约               | P0  |
| 冲突提示与建议 | 冲突时提示原因，并展示 3 个相邻可用时段，点击一键替换      | P0  |
| 我的预约列表  | 分 "即将开始 / 已完成 / 已取消" 三个 Tab，按时间排序 | P0  |
| 预约详情    | 展示会议室、时间、主题、参会人数、设备、备注、状态         | P0  |
| 取消预约    | 未开始且在取消时限内可自行取消，取消后释放时段           | P0  |
| 修改预约    | 未开始预约可修改时间 / 会议室 / 主题，修改时重新走冲突检测  | P1  |
| 预约签到    | 会议开始后可签到，未签到超时自动释放（可配置开关）         | P2  |
| 周期预约    | 支持按日 / 周 / 月重复预约（如每周例会）           | P2  |

**创建预约字段**：



| 字段   | 必填 | 说明                       |
| ---- | -- | ------------------------ |
| 会议室  | 是  | 从可用会议室中选择                |
| 日期   | 是  | 不可选过去日期，不可选超过可预约天数上限的日期  |
| 开始时间 | 是  | 最小粒度 30 分钟（可配置），须在可预约时段内 |
| 结束时间 | 是  | 须晚于开始时间，时长在最小 / 最大限制内    |
| 会议主题 | 是  | 1-50 字                   |
| 参会人数 | 否  | 默认 1，不可超过会议室容量           |
| 设备需求 | 否  | 多选，仅作备注                  |
| 备注   | 否  | 0-200 字                  |

**业务流程**：



```
用户选时段 → 填写预约信息 → 规则校验 → 冲突检测 → 生成预约(已确认) → 通知 → 日历刷新

&#x20;               ↓不通过          ↓冲突

&#x20;             提示原因修改     提示+建议相邻时段
```

#### 4.1.3 通知提醒模块



| 功能点         | 描述                        | 优先级 |
| ----------- | ------------------------- | --- |
| 预约成功通知      | 预约提交成功后，站内通知确认信息          | P0  |
| 开始前提醒       | 会议开始前 15 分钟（可配置）推送站内提醒    | P0  |
| 取消通知        | 预约被取消（自己 / 管理员 / 审批拒绝）时通知 | P0  |
| 通知中心        | 查看所有历史通知，标记已读 / 未读        | P1  |
| 通知偏好        | 用户可自定义是否接收各类通知、提醒提前量      | P2  |
| 邮件 / 企业微信通知 | 演进支持多渠道通知                 | P2  |

#### 4.1.4 用户中心模块



| 功能点      | 描述                 | 优先级 |
| -------- | ------------------ | --- |
| 登录注册     | 支持手机号 + 验证码 / 密码登录 | P0  |
| 个人信息     | 昵称、头像、所属部门、手机号管理   | P0  |
| 我的预约快捷入口 | 从个人中心快速进入我的预约      | P1  |
| 修改密码     | 支持修改登录密码           | P1  |

### 4.2 后台管理端功能

#### 4.2.1 会议室管理模块



| 功能点   | 描述                        | 优先级 |
| ----- | ------------------------- | --- |
| 会议室列表 | 展示所有会议室，含状态、容量、楼层、今日预约数   | P0  |
| 新增会议室 | 填写名称、位置、容量、类型、设备、图片、状态、备注 | P0  |
| 编辑会议室 | 修改会议室信息                   | P0  |
| 删除会议室 | 逻辑删除；有未完成预约时禁止删除          | P0  |
| 状态管理  | 可用 / 维护中 / 停用，维护中和停用不可被预约 | P0  |
| 会议室筛选 | 按楼层、类型、容量、状态、设备筛选         | P0  |
| 批量导入  | Excel 批量导入会议室信息           | P2  |

**会议室信息字段**：



| 字段        | 必填 | 说明                        |
| --------- | -- | ------------------------- |
| 会议室名称     | 是  | 同一楼层内唯一                   |
| 所在楼层 / 位置 | 是  | 如 "3 楼东侧"                 |
| 容纳人数      | 是  | 整数，≥1                     |
| 会议室类型     | 否  | 会议室 / 洽谈室 / 培训室 / 电话亭     |
| 配备设备      | 否  | 多选：投影仪、电视 / 大屏、视频会议、白板、音响 |
| 会议室图片     | 否  | 最多 3 张                    |
| 状态        | 是  | 可用 / 维护中 / 停用             |
| 备注        | 否  | 其他说明                      |

#### 4.2.2 预约管理模块



| 功能点  | 描述                           | 优先级 |
| ---- | ---------------------------- | --- |
| 预约总览 | 查看全部预约，按日期 / 会议室 / 状态 / 用户筛选 | P0  |
| 预约详情 | 查看预约完整信息                     | P0  |
| 强制取消 | 取消任何未开始的预约，需填写原因，取消后通知预约人    | P0  |
| 代客预约 | 管理员替员工创建预约（如领导秘书代约）          | P1  |
| 预约导出 | 预约数据导出 Excel                 | P2  |

#### 4.2.3 数据统计模块



| 功能点    | 描述                                     | 优先级 |
| ------ | -------------------------------------- | --- |
| 使用率统计  | 按日 / 周 / 月统计各会议室使用率（预约时长 / 可预约时长），排序展示 | P1  |
| 热门会议室  | 预约量 Top 5 会议室，展示预约次数、使用率、平均时长          | P1  |
| 预约趋势   | 折线图展示近 30 天预约量趋势，可按部门 / 会议室筛选          | P1  |
| 闲置时段分析 | 热力图展示各会议室在一周各时段的闲置情况                   | P2  |
| 部门使用统计 | 按部门统计预约次数和时长                           | P2  |
| 取消率统计  | 各会议室预约取消率，异常高的提示关注                     | P2  |
| 数据导出   | 统计数据导出 Excel                           | P2  |

#### 4.2.4 系统设置模块



| 功能点    | 描述                                     | 优先级 |
| ------ | -------------------------------------- | --- |
| 预约规则配置 | 配置最小 / 最大时长、可预约天数、可预约时段、时间粒度、取消时限、每日上限 | P0  |
| 工作日历管理 | 配置法定节假日、公司特殊休息日（这些日期不可预约）              | P1  |
| 通知配置   | 配置提醒提前量、通知开关                           | P1  |
| 签到规则配置 | 配置是否开启自动释放、释放超时时间                      | P2  |
| 系统参数   | 系统名称、Logo 等基础配置                        | P2  |

#### 4.2.5 用户与权限管理



| 功能点  | 描述                     | 优先级 |
| ---- | ---------------------- | --- |
| 用户列表 | 查看所有用户，按角色 / 部门 / 状态筛选 | P1  |
| 用户管理 | 禁用 / 启用用户、重置密码、分配角色    | P1  |
| 角色管理 | 维护角色列表和权限              | P1  |
| 部门管理 | 维护部门列表，用户归属部门          | P1  |
| 操作日志 | 管理员关键操作审计日志（谁、何时、做了什么） | P1  |
| 登录日志 | 登录行为审计                 | P2  |

### 4.3 核心业务规则

#### 4.3.1 预约规则（可由管理员配置）



| 规则项      | 默认值           | 说明                 |
| -------- | ------------- | ------------------ |
| 最小预约时长   | 30 分钟         | 不可低于此时长            |
| 最大预约时长   | 4 小时          | 单次预约不可超过           |
| 可预约天数范围  | 未来 14 天       | 只能预约今天起 14 天内的时段   |
| 可预约时段    | 08:00 - 22:00 | 每日可预约的时间范围         |
| 最小时间粒度   | 30 分钟         | 开始 / 结束时间只能是整半点    |
| 提前取消时限   | 开始前 15 分钟     | 超过此时限不可自行取消，需联系管理员 |
| 每人每日预约上限 | 3 次           | 防止恶意占用（可配置为不限）     |
| 节假日是否可预约 | 否             | 对接工作日历，节假日默认不可预约   |

#### 4.3.2 冲突检测机制（核心）

**重叠判定公式**：

新预约 `[start, end)` 与已有已确认预约 `[s, e)` 满足 `start < e AND end > s` 即视为冲突。

**检测流程**：



1. 员工提交预约时，后端先做**规则校验**（时长、天数、时段、每日上限）；

2. 规则通过后，查询该会议室在 `[start, end)` 时段内所有状态为 "已确认" 的预约；

3. 若存在任意一条满足重叠判定公式，则判定冲突；

4. 冲突时返回该会议室当天剩余可用时段列表，引导用户改期或换会议室。

**并发保障（两人同时提交同一时段）**：



* 应用层检测之外，**对同一会议室同一日期的预约创建加数据库行锁**（`SELECT ... FOR UPDATE` 锁定会议室行）；

* 确保 100 并发请求中仅 1 条成功写入，后提交者正确收到冲突提示；

* **冲突漏检率 = 0** 是本产品的硬性验收指标。

#### 4.3.3 预约状态机



| 状态  | 触发条件                 | 说明          | 可执行操作             |
| --- | -------------------- | ----------- | ----------------- |
| 待审批 | 会议室开启审批模式，员工提交       | 暂不占用时段（可配置） | 管理员审批通过 / 拒绝；员工取消 |
| 已确认 | 提交成功（无需审批）或审批通过      | 占用时段，冲突检测生效 | 员工取消 / 修改；管理员强制取消 |
| 已签到 | 会议开始后员工签到            | 演进功能        | —                 |
| 已完成 | 会议结束时间到达             | 终态，释放时段     | 查看详情              |
| 已取消 | 员工 / 管理员取消，或审批拒绝     | 终态，释放时段     | 查看详情              |
| 已过期 | 超过开始时间未签到且开启自动释放（演进） | 终态，释放时段     | 查看详情              |



***

## 五、非功能需求

### 5.1 性能需求



| 指标        | 要求                         |
| --------- | -------------------------- |
| 页面加载时间    | < 3 秒                      |
| 日历视图首屏加载  | P95 ≤ 1.5 秒（一周数据，≤20 间会议室） |
| API 响应时间  | < 500ms                    |
| 预约提交响应    | P95 ≤ 1 秒（含冲突检测）           |
| 管理端数据看板加载 | P95 ≤ 3 秒                  |
| 并发用户数     | > 500                      |
| 并发预约      | 100 并发同一时段仅 1 条成功          |
| 系统可用性     | > 99.5%                    |

### 5.2 安全需求



| 需求   | 描述                           |
| ---- | ---------------------------- |
| 身份认证 | JWT Token 认证                 |
| 权限控制 | RBAC 权限模型，普通员工只能操作自己的预约      |
| 数据加密 | 敏感数据加密存储，密码 BCrypt 加密        |
| 接口安全 | API 限流、防刷、防重复提交              |
| 数据隐私 | 他人预约仅显示 "已占用"，不泄露主题和参会人      |
| 日志审计 | 管理员关键操作（强制取消、删除会议室、规则变更）全量记录 |
| 操作确认 | 管理员破坏性操作需二次确认                |

### 5.3 兼容性需求



| 需求     | 描述                                |
| ------ | --------------------------------- |
| 浏览器兼容  | Chrome、Firefox、Edge、Safari 最新两个版本 |
| 移动端适配  | 响应式设计，适配主流手机和平板                   |
| 分辨率支持  | 1366x768 - 3840x2160              |
| 日历组件兼容 | FullCalendar 在各浏览器下渲染一致           |

### 5.4 可扩展性需求



* 模块化设计，功能可插拔；

* API 版本管理，支持平滑升级；

* 预约规则全部可配置，无需改代码；

* 支持会议室数量水平扩展（≤100 间无性能瓶颈）；

* 预留审批流程、周期预约、签到等扩展点。

### 5.5 数据指标与埋点

#### 核心业务指标



| 指标     | 定义                  | 目标       |
| ------ | ------------------- | -------- |
| 预约量    | 每日 / 每周创建的预约数       | —        |
| 预约成功率  | 成功生成预约数 / 提交预约数     | ≥ 98%    |
| 冲突率    | 因冲突被拒绝的提交数 / 总提交数   | 监控项      |
| 取消率    | 取消预约数 / 已确认预约数      | ≤ 20%    |
| 平均预约时长 | 所有已完成预约的平均时长        | 监控项      |
| 会议室使用率 | 预约时长 / 可预约总时长（按会议室） | 整体 ≥ 50% |
| 日均活跃用户 | 每日登录 / 操作平台的用户数     | —        |

#### 关键埋点事件



| 事件名                          | 触发时机     | 关键参数                                                |
| ---------------------------- | -------- | --------------------------------------------------- |
| `calendar_view_load`         | 日历视图加载完成 | user\_id, view\_type, room\_count, load\_time\_ms   |
| `room_filter`                | 使用筛选器    | user\_id, filters                                   |
| `booking_slot_click`         | 点击日历空闲时段 | user\_id, room\_id, date, start\_time, end\_time    |
| `booking_submit`             | 提交预约表单   | user\_id, room\_id, date, start, end, duration\_min |
| `booking_success`            | 预约成功     | booking\_id, room\_id, duration\_min                |
| `booking_conflict`           | 因冲突被拒绝   | user\_id, room\_id, start, end, suggested\_slots    |
| `booking_rule_fail`          | 因规则校验失败  | user\_id, fail\_reason                              |
| `booking_cancel`             | 取消预约     | booking\_id, user\_id, cancel\_type, reason         |
| `booking_modify`             | 修改预约     | booking\_id, changed\_fields                        |
| `notification_send`          | 发送通知     | user\_id, type, channel                             |
| `admin_room_create`          | 管理员创建会议室 | admin\_id, room\_id, capacity                       |
| `admin_booking_force_cancel` | 管理员强制取消  | admin\_id, booking\_id, reason                      |
| `dashboard_view`             | 查看数据看板   | admin\_id, time\_range                              |



***

## 六、验收标准（DoD）

### 6.1 会议室管理



* [ ] 管理员可新增、编辑、删除会议室，字段完整；

* [ ] 状态为 "维护中" 或 "停用" 的会议室不出现在可预约日历中；

* [ ] 删除有未完成预约的会议室时被禁止，并提示先处理预约；

* [ ] 会议室列表支持按楼层、容量、设备、状态筛选。

### 6.2 预约与冲突检测（核心）



* [ ] 员工可在日历视图点击空闲时段创建预约，30 秒内完成；

* [ ] 预约规则生效：最小 / 最大时长、可预约天数、可预约时段、每日上限均正确校验；

* [ ] **冲突检测 100% 准确**：同一会议室同一时段不允许两条已确认预约；

* [ ] 并发测试：100 个请求同时预约同一会议室同一时段，仅 1 条成功，其余正确收到冲突提示；

* [ ] 冲突时展示相邻可用时段建议，点击可一键替换；

* [ ] 取消预约后时段即时释放，可被他人预约。

### 6.3 我的预约



* [ ] "即将开始 / 已完成 / 已取消" 三个 Tab 数据正确；

* [ ] 未开始且在取消时限内可自行取消；超过时限取消按钮置灰并提示；

* [ ] 预约详情信息完整准确；

* [ ] 他人预约不显示主题和参会人（隐私保护）。

### 6.4 通知



* [ ] 预约成功后收到站内通知；

* [ ] 会议开始前 15 分钟收到提醒；

* [ ] 预约被取消时收到通知。

### 6.5 管理后台



* [ ] 管理员可查看全部预约并按条件筛选；

* [ ] 管理员可强制取消未开始预约（需填原因），取消后通知预约人；

* [ ] 预约规则可在管理端配置并即时生效；

* [ ] 数据看板展示使用率、热门会议室、预约趋势，数据准确；

* [ ] 管理员关键操作有日志记录。

### 6.6 非功能



* [ ] 日历视图 P95 加载 ≤ 1.5 秒；

* [ ] 预约提交 P95 ≤ 1 秒；

* [ ] 核心接口可用性 ≥ 99.5%；

* [ ] 管理员破坏性操作需二次确认。



***

## 七、数据库设计

### 7.1 用户与权限表



```
\-- 用户表

CREATE TABLE sys\_user (

&#x20;   id BIGINT PRIMARY KEY AUTO\_INCREMENT,

&#x20;   username VARCHAR(50) NOT NULL COMMENT '用户名',

&#x20;   password VARCHAR(100) NOT NULL COMMENT '密码(BCrypt)',

&#x20;   phone VARCHAR(20) COMMENT '手机号',

&#x20;   real\_name VARCHAR(50) COMMENT '真实姓名',

&#x20;   avatar VARCHAR(200) COMMENT '头像',

&#x20;   dept\_id BIGINT COMMENT '部门ID',

&#x20;   role VARCHAR(20) DEFAULT 'user' COMMENT '角色: user/admin',

&#x20;   status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1正常',

&#x20;   create\_time DATETIME DEFAULT CURRENT\_TIMESTAMP,

&#x20;   update\_time DATETIME DEFAULT CURRENT\_TIMESTAMP ON UPDATE CURRENT\_TIMESTAMP

) COMMENT '用户表';

\-- 部门表

CREATE TABLE sys\_dept (

&#x20;   id BIGINT PRIMARY KEY AUTO\_INCREMENT,

&#x20;   dept\_name VARCHAR(50) NOT NULL COMMENT '部门名称',

&#x20;   sort INT DEFAULT 0 COMMENT '排序',

&#x20;   status TINYINT DEFAULT 1 COMMENT '状态',

&#x20;   create\_time DATETIME DEFAULT CURRENT\_TIMESTAMP

) COMMENT '部门表';

\-- 操作日志表

CREATE TABLE sys\_operation\_log (

&#x20;   id BIGINT PRIMARY KEY AUTO\_INCREMENT,

&#x20;   user\_id BIGINT COMMENT '操作人ID',

&#x20;   username VARCHAR(50) COMMENT '操作人',

&#x20;   module VARCHAR(50) COMMENT '操作模块',

&#x20;   operation VARCHAR(100) COMMENT '操作内容',

&#x20;   method VARCHAR(200) COMMENT '请求方法',

&#x20;   params TEXT COMMENT '请求参数',

&#x20;   ip VARCHAR(50) COMMENT 'IP地址',

&#x20;   status TINYINT DEFAULT 1 COMMENT '操作状态',

&#x20;   error\_msg TEXT COMMENT '错误信息',

&#x20;   cost\_time BIGINT COMMENT '耗时(ms)',

&#x20;   create\_time DATETIME DEFAULT CURRENT\_TIMESTAMP

) COMMENT '操作日志表';
```

### 7.2 会议室表



```
\-- 会议室表

CREATE TABLE meeting\_room (

&#x20;   id BIGINT PRIMARY KEY AUTO\_INCREMENT,

&#x20;   room\_name VARCHAR(100) NOT NULL COMMENT '会议室名称',

&#x20;   floor VARCHAR(50) COMMENT '所在楼层',

&#x20;   location VARCHAR(200) COMMENT '具体位置',

&#x20;   capacity INT NOT NULL COMMENT '容纳人数',

&#x20;   room\_type VARCHAR(20) DEFAULT 'meeting' COMMENT '类型: meeting洽谈室 training培训室 phone电话亭',

&#x20;   equipment JSON COMMENT '配备设备: \["projector","tv","whiteboard"]',

&#x20;   images JSON COMMENT '图片列表',

&#x20;   description TEXT COMMENT '描述/备注',

&#x20;   status TINYINT DEFAULT 1 COMMENT '状态: 0维护中 1可用 2停用',

&#x20;   sort INT DEFAULT 0 COMMENT '排序',

&#x20;   create\_by BIGINT COMMENT '创建人',

&#x20;   create\_time DATETIME DEFAULT CURRENT\_TIMESTAMP,

&#x20;   update\_time DATETIME DEFAULT CURRENT\_TIMESTAMP ON UPDATE CURRENT\_TIMESTAMP,

&#x20;   UNIQUE KEY uk\_room\_floor\_name (floor, room\_name)

) COMMENT '会议室表';
```

### 7.3 预约表（核心）



```
\-- 预约表

CREATE TABLE booking (

&#x20;   id BIGINT PRIMARY KEY AUTO\_INCREMENT,

&#x20;   booking\_no VARCHAR(32) NOT NULL COMMENT '预约编号',

&#x20;   room\_id BIGINT NOT NULL COMMENT '会议室ID',

&#x20;   user\_id BIGINT NOT NULL COMMENT '预约人ID',

&#x20;   booking\_date DATE NOT NULL COMMENT '预约日期',

&#x20;   start\_time TIME NOT NULL COMMENT '开始时间',

&#x20;   end\_time TIME NOT NULL COMMENT '结束时间',

&#x20;   duration\_min INT NOT NULL COMMENT '时长(分钟)',

&#x20;   topic VARCHAR(100) NOT NULL COMMENT '会议主题',

&#x20;   attendee\_count INT DEFAULT 1 COMMENT '参会人数',

&#x20;   equipment\_need JSON COMMENT '设备需求',

&#x20;   remark VARCHAR(500) COMMENT '备注',

&#x20;   status VARCHAR(20) DEFAULT 'confirmed' COMMENT '状态: pending待审批 confirmed已确认 signed已签到 completed已完成 cancelled已取消 expired已过期',

&#x20;   cancel\_reason VARCHAR(500) COMMENT '取消原因',

&#x20;   cancel\_by BIGINT COMMENT '取消人ID',

&#x20;   cancel\_time DATETIME COMMENT '取消时间',

&#x20;   sign\_time DATETIME COMMENT '签到时间',

&#x20;   create\_time DATETIME DEFAULT CURRENT\_TIMESTAMP,

&#x20;   update\_time DATETIME DEFAULT CURRENT\_TIMESTAMP ON UPDATE CURRENT\_TIMESTAMP,

&#x20;   UNIQUE KEY uk\_booking\_no (booking\_no),

&#x20;   KEY idx\_room\_date (room\_id, booking\_date),

&#x20;   KEY idx\_user (user\_id),

&#x20;   KEY idx\_status (status),

&#x20;   KEY idx\_room\_date\_status (room\_id, booking\_date, status)

) COMMENT '预约表';
```

> 冲突检测说明：由于时间段重叠不是等值关系，无法直接建唯一约束。采用 
>
> **应用层查询检测 + 数据库事务 + 数据库行锁**
>
>  三重保障。对同一会议室同一日期的创建操作，使用 `SELECT ... FOR UPDATE` 锁定会议室行，锁内查询是否有重叠预约，无重叠则插入，完成后释放锁。

### 7.4 系统配置表



```
\-- 系统配置表（key-value，存储预约规则等）

CREATE TABLE sys\_config (

&#x20;   id BIGINT PRIMARY KEY AUTO\_INCREMENT,

&#x20;   config\_key VARCHAR(100) NOT NULL COMMENT '配置键',

&#x20;   config\_value TEXT COMMENT '配置值',

&#x20;   config\_name VARCHAR(100) COMMENT '配置名称',

&#x20;   description VARCHAR(500) COMMENT '描述',

&#x20;   create\_time DATETIME DEFAULT CURRENT\_TIMESTAMP,

&#x20;   update\_time DATETIME DEFAULT CURRENT\_TIMESTAMP ON UPDATE CURRENT\_TIMESTAMP,

&#x20;   UNIQUE KEY uk\_config\_key (config\_key)

) COMMENT '系统配置表';

\-- 初始化预约规则配置

INSERT INTO sys\_config (config\_key, config\_value, config\_name, description) VALUES

('booking.min\_duration', '30', '最小预约时长(分钟)', '单次预约不可低于此时长'),

('booking.max\_duration', '240', '最大预约时长(分钟)', '单次预约不可超过此时长'),

('booking.advance\_days', '14', '可预约天数', '只能预约今天起N天内的时段'),

('booking.start\_time', '08:00', '可预约开始时间', '每日可预约的最早时间'),

('booking.end\_time', '22:00', '可预约结束时间', '每日可预约的最晚时间'),

('booking.time\_granularity', '30', '时间粒度(分钟)', '开始/结束时间的最小粒度'),

('booking.cancel\_limit\_min', '15', '提前取消时限(分钟)', '会议开始前N分钟内不可自行取消'),

('booking.daily\_limit', '3', '每人每日预约上限', '0表示不限'),

('booking.holiday\_allowed', 'false', '节假日是否可预约', 'true/false'),

('booking.remind\_min', '15', '开始前提醒(分钟)', '会议开始前N分钟发送提醒'),

('booking.auto\_release', 'false', '是否开启自动释放', '未签到超时自动释放时段'),

('booking.auto\_release\_min', '15', '自动释放超时(分钟)', '会议开始后N分钟未签到则释放');
```

### 7.5 通知表



```
\-- 通知表

CREATE TABLE notification (

&#x20;   id BIGINT PRIMARY KEY AUTO\_INCREMENT,

&#x20;   user\_id BIGINT NOT NULL COMMENT '接收用户ID',

&#x20;   type VARCHAR(30) NOT NULL COMMENT '类型: success预约成功 remind开始提醒 cancel取消通知 system系统通知',

&#x20;   title VARCHAR(200) NOT NULL COMMENT '标题',

&#x20;   content TEXT COMMENT '内容',

&#x20;   biz\_type VARCHAR(30) COMMENT '业务类型: booking',

&#x20;   biz\_id BIGINT COMMENT '业务ID',

&#x20;   is\_read TINYINT DEFAULT 0 COMMENT '是否已读: 0未读 1已读',

&#x20;   read\_time DATETIME COMMENT '阅读时间',

&#x20;   create\_time DATETIME DEFAULT CURRENT\_TIMESTAMP,

&#x20;   KEY idx\_user\_read (user\_id, is\_read),

&#x20;   KEY idx\_user\_create (user\_id, create\_time)

) COMMENT '通知表';
```

### 7.6 工作日历表



```
\-- 工作日历表（节假日/特殊休息日）

CREATE TABLE work\_calendar (

&#x20;   id BIGINT PRIMARY KEY AUTO\_INCREMENT,

&#x20;   calendar\_date DATE NOT NULL COMMENT '日期',

&#x20;   date\_type TINYINT NOT NULL COMMENT '类型: 1工作日 2周末 3节假日 4调休工作日 5特殊休息日',

&#x20;   holiday\_name VARCHAR(50) COMMENT '节假日名称',

&#x20;   remark VARCHAR(500) COMMENT '备注',

&#x20;   create\_time DATETIME DEFAULT CURRENT\_TIMESTAMP,

&#x20;   UNIQUE KEY uk\_calendar\_date (calendar\_date)

) COMMENT '工作日历表';
```



***

## 八、接口设计

### 8.1 接口规范



* 基础路径：`/api/v1`

* 认证方式：Bearer Token (JWT)

* 数据格式：JSON

* 响应格式：



```
{

&#x20;   "code": 200,

&#x20;   "message": "success",

&#x20;   "data": {}

}
```



* 分页参数：`pageNum` / `pageSize`，返回 `{ list, total, pageNum, pageSize }`

* 错误码：`400`参数错误、`401`未认证、`403`无权限、`409`冲突（预约冲突专用）、`500`系统错误

### 8.2 核心接口列表

#### 认证接口



| 接口                    | 方法   | 描述           |
| --------------------- | ---- | ------------ |
| /api/v1/auth/register | POST | 注册           |
| /api/v1/auth/login    | POST | 登录（返回 token） |
| /api/v1/auth/userinfo | GET  | 获取当前用户信息     |
| /api/v1/auth/logout   | POST | 登出           |

#### 会议室接口（前台）



| 接口                              | 方法  | 描述                              |
| ------------------------------- | --- | ------------------------------- |
| /api/v1/rooms                   | GET | 获取会议室列表（支持筛选：楼层 / 容量 / 设备 / 状态） |
| /api/v1/rooms/{id}              | GET | 获取会议室详情                         |
| /api/v1/rooms/{id}/availability | GET | 获取指定会议室在指定日期的可用时段               |
| /api/v1/rooms/calendar          | GET | 获取日历视图数据（指定周 / 日，所有会议室的预约情况）    |

#### 预约接口（前台）



| 接口                           | 方法   | 描述                                          |
| ---------------------------- | ---- | ------------------------------------------- |
| /api/v1/bookings             | POST | 创建预约（含规则校验 + 冲突检测）                          |
| /api/v1/bookings/mine        | GET  | 我的预约列表（支持状态筛选：upcoming/completed/cancelled） |
| /api/v1/bookings/{id}        | GET  | 预约详情                                        |
| /api/v1/bookings/{id}/cancel | PUT  | 取消预约                                        |
| /api/v1/bookings/{id}        | PUT  | 修改预约（重新走冲突检测）                               |
| /api/v1/bookings/{id}/sign   | POST | 签到（演进）                                      |

#### 通知接口



| 接口                                 | 方法  | 描述                |
| ---------------------------------- | --- | ----------------- |
| /api/v1/notifications              | GET | 通知列表（支持已读 / 未读筛选） |
| /api/v1/notifications/{id}/read    | PUT | 标记已读              |
| /api/v1/notifications/read-all     | PUT | 全部标记已读            |
| /api/v1/notifications/unread-count | GET | 未读数量              |

#### 后台 - 会议室管理



| 接口                              | 方法     | 描述            |
| ------------------------------- | ------ | ------------- |
| /api/v1/admin/rooms             | GET    | 会议室列表（含今日预约数） |
| /api/v1/admin/rooms             | POST   | 新增会议室         |
| /api/v1/admin/rooms/{id}        | PUT    | 编辑会议室         |
| /api/v1/admin/rooms/{id}        | DELETE | 删除会议室（逻辑删除）   |
| /api/v1/admin/rooms/{id}/status | PUT    | 修改会议室状态       |

#### 后台 - 预约管理



| 接口                                       | 方法   | 描述                            |
| ---------------------------------------- | ---- | ----------------------------- |
| /api/v1/admin/bookings                   | GET  | 全部预约列表（按日期 / 会议室 / 状态 / 用户筛选） |
| /api/v1/admin/bookings/{id}/force-cancel | PUT  | 强制取消预约（需填原因）                  |
| /api/v1/admin/bookings/create-by-admin   | POST | 代客预约                          |
| /api/v1/admin/bookings/export            | GET  | 导出预约数据 Excel                  |

#### 后台 - 数据统计



| 接口                               | 方法  | 描述            |
| -------------------------------- | --- | ------------- |
| /api/v1/admin/stats/usage        | GET | 会议室使用率统计      |
| /api/v1/admin/stats/hot-rooms    | GET | 热门会议室 Top5    |
| /api/v1/admin/stats/trend        | GET | 预约量趋势（近 30 天） |
| /api/v1/admin/stats/idle-heatmap | GET | 闲置时段热力图       |
| /api/v1/admin/stats/dept         | GET | 部门使用统计        |

#### 后台 - 系统设置



| 接口                                      | 方法     | 描述            |
| --------------------------------------- | ------ | ------------- |
| /api/v1/admin/config/booking-rules      | GET    | 获取预约规则配置      |
| /api/v1/admin/config/booking-rules      | PUT    | 更新预约规则配置      |
| /api/v1/admin/config/work-calendar      | GET    | 获取工作日历列表      |
| /api/v1/admin/config/work-calendar      | POST   | 新增节假日 / 特殊休息日 |
| /api/v1/admin/config/work-calendar/{id} | DELETE | 删除工作日历配置      |
| /api/v1/admin/config/notification       | GET    | 获取通知配置        |
| /api/v1/admin/config/notification       | PUT    | 更新通知配置        |

#### 后台 - 用户与权限



| 接口                                      | 方法                  | 描述        |
| --------------------------------------- | ------------------- | --------- |
| /api/v1/admin/users                     | GET                 | 用户列表      |
| /api/v1/admin/users/{id}/status         | PUT                 | 禁用 / 启用用户 |
| /api/v1/admin/users/{id}/reset-password | PUT                 | 重置密码      |
| /api/v1/admin/users/{id}/role           | PUT                 | 分配角色      |
| /api/v1/admin/depts                     | GET/POST/PUT/DELETE | 部门管理      |
| /api/v1/admin/operation-logs            | GET                 | 操作日志列表    |



***

## 九、项目结构

### 9.1 仓库目录结构



```
meeting-room-booking/

├── meeting-room-web/             # 前台用户端 (Vue3)

│   ├── src/

│   │   ├── api/                  # 接口定义

│   │   ├── assets/               # 静态资源

│   │   ├── components/           # 公共组件

│   │   │   ├── BookingCalendar.vue   # 日历组件封装

│   │   │   ├── BookingForm.vue       # 预约表单弹窗

│   │   │   └── RoomFilter.vue        # 会议室筛选器

│   │   ├── composables/          # 组合式函数

│   │   ├── router/               # 路由配置

│   │   ├── stores/               # Pinia状态 (auth/booking/room)

│   │   ├── styles/               # 全局样式

│   │   ├── types/                # TypeScript类型

│   │   ├── utils/                # 工具函数

│   │   └── views/                # 页面视图

│   │       ├── CalendarView.vue      # 日历视图(首页)

│   │       ├── MyBookings.vue        # 我的预约

│   │       ├── BookingDetail.vue     # 预约详情

│   │       ├── Notifications.vue     # 通知中心

│   │       └── Profile.vue           # 个人中心

│   ├── public/

│   ├── package.json

│   └── vite.config.ts

│

├── meeting-room-admin/           # 后台管理端 (Vue3)

│   ├── src/

│   │   ├── api/

│   │   ├── components/

│   │   ├── layout/               # 后台布局(侧边栏+顶栏)

│   │   ├── router/

│   │   ├── stores/

│   │   └── views/

│   │       ├── room/RoomManage.vue      # 会议室管理

│   │       ├── booking/BookingManage.vue # 预约管理

│   │       ├── stats/StatsDashboard.vue  # 数据看板

│   │       ├── config/BookingRules.vue   # 预约规则配置

│   │       ├── config/WorkCalendar.vue   # 工作日历

│   │       ├── user/UserManage.vue       # 用户管理

│   │       └── log/OperationLog.vue      # 操作日志

│   ├── package.json

│   └── vite.config.ts

│

├── meeting-room-server/          # 后端服务 (Spring Boot)

│   ├── src/main/java/com/example/meeting/

│   │   ├── config/               # 配置类 (Security/Knife4j)

│   │   ├── controller/           # 控制器 (前台/后台)

│   │   │   ├── front/            # 前台接口

│   │   │   └── admin/            # 后台接口

│   │   ├── service/              # 服务层

│   │   │   ├── BookingService.java       # 预约服务(含冲突检测)

│   │   │   ├── RoomService.java          # 会议室服务

│   │   │   ├── NotificationService.java  # 通知服务

│   │   │   ├── StatsService.java         # 统计服务

│   │   │   └── ConfigService.java        # 配置服务

│   │   ├── mapper/               # Mapper接口 (MyBatis-Plus)

│   │   ├── entity/               # 实体类

│   │   ├── dto/                  # 数据传输对象

│   │   ├── vo/                   # 视图对象

│   │   ├── common/               # 公共模块 (Result/异常/常量)

│   │   ├── interceptor/          # 拦截器 (认证/日志)

│   │   └── MeetingApplication.java

│   ├── src/main/resources/

│   │   ├── application.yml

│   │   ├── mapper/               # MyBatis XML

│   │   └── db/init.sql           # 初始化SQL

│   └── pom.xml

│

├── docs/                         # 项目文档

│   ├── PRD.md

│   ├── API.md

│   └── DEPLOY.md

│

├── deploy/                       # 部署配置

│   ├── docker-compose.yml        # MySQL

│   └── nginx/

│

└── README.md
```



***

## 十、开发计划

### 10.1 阶段划分



| 阶段            | 周期  | 主要任务                                 |
| ------------- | --- | ------------------------------------ |
| 第一阶段：基础搭建     | 1 周 | 项目初始化、前后端框架搭建、数据库设计、Docker 环境        |
| 第二阶段：用户与会议室管理 | 2 周 | 登录注册、权限、会议室 CRUD、前台会议室列表             |
| 第三阶段：预约核心功能   | 3 周 | 日历视图、创建预约、规则校验、冲突检测 (含并发锁)、我的预约、取消修改 |
| 第四阶段：通知与管理后台  | 2 周 | 通知提醒、预约管理、数据看板、规则配置、工作日历、用户管理、操作日志   |
| 第五阶段：测试优化     | 1 周 | 功能测试、并发压测 (冲突检测)、性能优化、安全加固           |
| 第六阶段：部署上线     | 1 周 | 环境部署、数据初始化、上线运行                      |

**总计：10 周**

### 10.2 里程碑



| 里程碑       | 时间      | 交付物                              |
| --------- | ------- | -------------------------------- |
| M1：框架就绪   | 第 1 周末  | 前后端基础框架、数据库表、Docker 环境           |
| M2：基础功能可用 | 第 3 周末  | 用户登录、会议室管理、前台会议室列表               |
| M3：MVP 版本 | 第 6 周末  | 预约核心闭环可用 (日历 + 预约 + 冲突检测 + 我的预约) |
| M4：全功能版本  | 第 8 周末  | 通知、管理后台、数据看板、规则配置全部完成            |
| M5：正式上线   | 第 10 周末 | 生产环境部署、压测通过                      |



***

## 十一、风险评估



| 风险                 | 影响 | 概率 | 应对措施                                       |
| ------------------ | -- | -- | ------------------------------------------ |
| 并发预约产生重复           | 高  | 中  | 数据库行锁 + 应用层检测，双重保障；100 并发压测验证 |
| 预约规则复杂导致用户困惑       | 中  | 中  | 表单处实时提示规则限制；失败时给出具体原因和修改建议；冲突时推荐相邻时段       |
| 管理员误操作 (强制取消 / 删除) | 中  | 低  | 所有破坏性操作二次确认 + 操作日志 + 通知受影响人                |
| 日历组件兼容性问题          | 中  | 低  | 使用 FullCalendar 成熟方案；多浏览器测试；移动端适配降级为日视图    |
| 会议室信息不准确 (设备坏了未更新) | 中  | 低  | 管理员定期维护；用户可报告问题 (演进)；预约页展示设备信息供参考          |
| 冷启动无会议室数据          | 低  | 高  | 初始化示例数据脚本；提供 Excel 批量导入；管理员快速录入            |
| 节假日数据维护不及时         | 低  | 中  | 管理端可配置工作日历；每年初批量导入法定节假日；默认所有日期可预约的降级策略     |
| 通知不及时 / 遗漏         | 中  | 低  | 站内通知为主；异步重试机制；通知中心可查历史；演进支持邮件 / 企业微信多渠道    |



***

## 十二、附录

### 12.1 术语表



| 术语           | 解释                                   |
| ------------ | ------------------------------------ |
| PRD          | Product Requirements Document，产品需求文档 |
| RBAC         | Role-Based Access Control，基于角色的访问控制  |
| JWT          | JSON Web Token，JSON 网络令牌             |
| DoD          | Definition of Done，完成定义 / 验收标准       |
| MVP          | Minimum Viable Product，最小可行产品        |
| 冲突检测         | 校验同一会议室的预约时间段不重叠的机制                  |
| 工作日历         | 定义可预约日期与节假日的配置                       |
| 使用率          | 预约时长 / 可预约总时长                        |
| FullCalendar | 成熟的 JavaScript 日历组件库                 |
| 数据库行锁         | 数据库层面的锁机制，用于并发预约保障               |

### 12.2 参考文档



* Vue 3 官方文档：[https://vuejs.org/](https://vuejs.org/)

* Spring Boot 官方文档：[https://spring.io/projects/spring-boot](https://spring.io/projects/spring-boot)

* MyBatis-Plus 官方文档：[https://baomidou.com/](https://baomidou.com/)

* FullCalendar 官方文档：[https://fullcalendar.io/](https://fullcalendar.io/)

* ECharts 官方文档：[https://echarts.apache.org/](https://echarts.apache.org/)



***

*本文档为会议室预约管理平台融合版 PRD，覆盖产品需求与技术实现全貌，适用于毕业设计答辩与研发实施。功能优先级与验收标准在研发排期前由产品与研发共同确认；预约规则默认值可根据实际业务场景在管理端调整。*