-- =====================================================
-- 会议室预约管理平台 - 数据库初始化脚本
-- 数据库: MySQL 8.0+
-- 版本: v3.0（全功能版）
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS meeting_room_booking DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE meeting_room_booking;

-- =====================================================
-- 1. 部门表
-- =====================================================
CREATE TABLE sys_dept (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dept_name VARCHAR(50) NOT NULL COMMENT '部门名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '部门表';

-- =====================================================
-- 2. 用户表
-- =====================================================
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码(BCrypt)',
    phone VARCHAR(20) COMMENT '手机号',
    real_name VARCHAR(50) COMMENT '真实姓名',
    avatar VARCHAR(200) COMMENT '头像',
    dept_id BIGINT COMMENT '部门ID',
    role VARCHAR(20) DEFAULT 'user' COMMENT '角色: user/admin/dept_manager',
    credit_score INT DEFAULT 100 COMMENT '信用分',
    status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username)
) COMMENT '用户表';

-- =====================================================
-- 3. 操作日志表
-- =====================================================
CREATE TABLE sys_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT '操作人ID',
    username VARCHAR(50) COMMENT '操作人',
    module VARCHAR(50) COMMENT '操作模块',
    operation VARCHAR(100) COMMENT '操作内容',
    method VARCHAR(200) COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    ip VARCHAR(50) COMMENT 'IP地址',
    status TINYINT DEFAULT 1 COMMENT '操作状态: 0失败 1成功',
    error_msg TEXT COMMENT '错误信息',
    cost_time BIGINT COMMENT '耗时(ms)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '操作日志表';

-- =====================================================
-- 4. 会议室表
-- =====================================================
CREATE TABLE meeting_room (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_name VARCHAR(100) NOT NULL COMMENT '会议室名称',
    floor VARCHAR(50) COMMENT '所在楼层',
    location VARCHAR(200) COMMENT '具体位置',
    floor_map_x DECIMAL(6,2) COMMENT '楼层图X坐标(%)',
    floor_map_y DECIMAL(6,2) COMMENT '楼层图Y坐标(%)',
    capacity INT NOT NULL COMMENT '容纳人数',
    room_type VARCHAR(20) DEFAULT 'meeting' COMMENT '类型: meeting会议室洽谈室 training培训室 phone电话亭',
    equipment JSON COMMENT '配备设备: ["projector","tv","whiteboard"]',
    images JSON COMMENT '图片列表',
    description TEXT COMMENT '描述/备注',
    status TINYINT DEFAULT 1 COMMENT '状态: 0维护中 1可用 2停用',
    need_approval TINYINT DEFAULT 0 COMMENT '预约是否需要审批: 0否 1是',
    sort INT DEFAULT 0 COMMENT '排序',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_room_floor_name (floor, room_name)
) COMMENT '会议室表';

-- =====================================================
-- 5. 预约表（核心）
-- =====================================================
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
    sign_source VARCHAR(20) DEFAULT NULL COMMENT '签到来源: manual手动 qrcode二维码',
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

-- =====================================================
-- 6. 系统配置表
-- =====================================================
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

-- =====================================================
-- 7. 通知表
-- =====================================================
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

-- =====================================================
-- 8. 部门配额表
-- =====================================================
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

-- =====================================================
-- 9. 会议室收藏表
-- =====================================================
CREATE TABLE room_favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    room_id BIGINT NOT NULL COMMENT '会议室ID',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_room (user_id, room_id)
) COMMENT '会议室收藏表';

-- =====================================================
-- 10. 周期预约组表
-- =====================================================
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

-- =====================================================
-- 11. 参会人表
-- =====================================================
CREATE TABLE booking_participant (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL COMMENT '预约ID',
    user_id BIGINT NOT NULL COMMENT '参会人ID',
    status VARCHAR(20) DEFAULT 'invited' COMMENT '状态: invited已邀请 accepted接受 declined拒绝',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_booking_user (booking_id, user_id)
) COMMENT '参会人表';

-- =====================================================
-- 12. 候补队列表
-- =====================================================
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

-- =====================================================
-- 13. 会议模板表
-- =====================================================
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

-- =====================================================
-- 14. 会议纪要表
-- =====================================================
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

-- =====================================================
-- 15. 报修工单表
-- =====================================================
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

-- =====================================================
-- 16. 工作日历表
-- =====================================================
CREATE TABLE work_calendar (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    calendar_date DATE NOT NULL COMMENT '日期',
    date_type TINYINT NOT NULL COMMENT '类型: 1工作日 2周末 3节假日 4调休工作日 5特殊休息日',
    holiday_name VARCHAR(50) COMMENT '节假日名称',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_calendar_date (calendar_date)
) COMMENT '工作日历表';

-- =====================================================
-- 17. 信用分记录表
-- =====================================================
CREATE TABLE credit_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    change_type VARCHAR(30) NOT NULL COMMENT 'no_show/complete/weekly_recover/cancel_late/admin_adjust',
    change_value INT NOT NULL,
    before_score INT NOT NULL,
    after_score INT NOT NULL,
    biz_type VARCHAR(30),
    biz_id BIGINT,
    remark VARCHAR(200),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_user (user_id),
    KEY idx_user_time (user_id, create_time)
) COMMENT '信用分记录表';

-- =====================================================
-- 18. 会议室评价表
-- =====================================================
CREATE TABLE room_rating (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    rating TINYINT NOT NULL COMMENT '1-5星',
    tags JSON COMMENT '标签',
    comment VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_booking_user (booking_id, user_id),
    KEY idx_room (room_id),
    KEY idx_user (user_id)
) COMMENT '会议室评价表';

-- =====================================================
-- 19. 低分检查记录表
-- =====================================================
CREATE TABLE room_rating_check (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rating_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'pending' COMMENT 'pending/converted/closed',
    repair_id BIGINT,
    remark VARCHAR(200),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_rating (rating_id)
) COMMENT '低分检查记录表';

CREATE TABLE chatbot_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    question TEXT NOT NULL,
    answer TEXT,
    intent VARCHAR(30),
    params JSON,
    result_count INT,
    success TINYINT DEFAULT 1,
    cost_ms BIGINT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_user (user_id),
    KEY idx_time (create_time)
) COMMENT '智能问答日志表';

CREATE TABLE ai_model_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '配置名称',
    provider VARCHAR(50) NOT NULL DEFAULT 'openai' COMMENT '提供方: openai/deepseek/qwen/zhipu/ollama/custom',
    base_url VARCHAR(255) NOT NULL COMMENT '接口基础地址(OpenAI兼容)',
    api_key VARCHAR(500) DEFAULT NULL COMMENT 'API密钥',
    model VARCHAR(100) NOT NULL COMMENT '模型名称',
    temperature DECIMAL(3,2) DEFAULT 0.20 COMMENT '采样温度',
    max_tokens INT DEFAULT 2048 COMMENT '最大输出tokens',
    timeout_seconds INT DEFAULT 20 COMMENT '读取超时(秒)',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认: 0否 1是',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用: 0否 1是',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_enabled (enabled),
    KEY idx_default (is_default)
) COMMENT 'AI模型配置表';


-- =====================================================
-- 测试数据
-- =====================================================

-- -----------------------------------------------------
-- 部门表测试数据 (12条)
-- -----------------------------------------------------
INSERT INTO sys_dept (id, dept_name, parent_id, sort, status) VALUES
(1, '总裁办', 0, 1, 1),
(2, '技术部', 0, 2, 1),
(3, '产品部', 0, 3, 1),
(4, '市场部', 0, 4, 1),
(5, '人力资源部', 0, 5, 1),
(6, '财务部', 0, 6, 1),
(7, '行政部', 0, 7, 1),
(8, '前端组', 2, 1, 1),
(9, '后端组', 2, 2, 1),
(10, '测试组', 2, 3, 1),
(11, '运维组', 2, 4, 1),
(12, '设计组', 3, 1, 1);

-- -----------------------------------------------------
-- 用户表测试数据 (12条)
-- 密码统一为: 123456 (BCrypt加密)
-- -----------------------------------------------------
INSERT INTO sys_user (id, username, password, phone, real_name, avatar, dept_id, role, status) VALUES
(1, 'admin', '$2a$10$pK0FTuPl1cvT5F48uclMzOnEInGfpieF9noGQSJAtRuDwH18B9Pb6', '13800000001', '系统管理员', NULL, 7, 'admin', 1),
(2, 'zhangsan', '$2a$10$pK0FTuPl1cvT5F48uclMzOnEInGfpieF9noGQSJAtRuDwH18B9Pb6', '13800000002', '张三', NULL, 8, 'user', 1),
(3, 'lisi', '$2a$10$pK0FTuPl1cvT5F48uclMzOnEInGfpieF9noGQSJAtRuDwH18B9Pb6', '13800000003', '李四', NULL, 9, 'user', 1),
(4, 'wangwu', '$2a$10$pK0FTuPl1cvT5F48uclMzOnEInGfpieF9noGQSJAtRuDwH18B9Pb6', '13800000004', '王五', NULL, 9, 'user', 1),
(5, 'zhaoliu', '$2a$10$pK0FTuPl1cvT5F48uclMzOnEInGfpieF9noGQSJAtRuDwH18B9Pb6', '13800000005', '赵六', NULL, 3, 'user', 1),
(6, 'sunqi', '$2a$10$pK0FTuPl1cvT5F48uclMzOnEInGfpieF9noGQSJAtRuDwH18B9Pb6', '13800000006', '孙七', NULL, 4, 'user', 1),
(7, 'zhouba', '$2a$10$pK0FTuPl1cvT5F48uclMzOnEInGfpieF9noGQSJAtRuDwH18B9Pb6', '13800000007', '周八', NULL, 5, 'user', 1),
(8, 'wujiu', '$2a$10$pK0FTuPl1cvT5F48uclMzOnEInGfpieF9noGQSJAtRuDwH18B9Pb6', '13800000008', '吴九', NULL, 6, 'user', 1),
(9, 'zhengshi', '$2a$10$pK0FTuPl1cvT5F48uclMzOnEInGfpieF9noGQSJAtRuDwH18B9Pb6', '13800000009', '郑十', NULL, 10, 'user', 1),
(10, 'chenyi', '$2a$10$pK0FTuPl1cvT5F48uclMzOnEInGfpieF9noGQSJAtRuDwH18B9Pb6', '13800000010', '陈一', NULL, 8, 'user', 1),
(11, 'liuer', '$2a$10$pK0FTuPl1cvT5F48uclMzOnEInGfpieF9noGQSJAtRuDwH18B9Pb6', '13800000011', '刘二', NULL, 12, 'user', 1),
(12, 'huangsan', '$2a$10$pK0FTuPl1cvT5F48uclMzOnEInGfpieF9noGQSJAtRuDwH18B9Pb6', '13800000012', '黄三', NULL, 11, 'admin', 1);

-- -----------------------------------------------------
-- 会议室表测试数据 (12条)
-- -----------------------------------------------------
INSERT INTO meeting_room (id, room_name, floor, location, capacity, room_type, equipment, description, status, sort, create_by) VALUES
(1, '创新厅', '3楼', '3楼东侧A区', 20, 'meeting', '["projector","tv","whiteboard","video_conference"]', '大型会议室，适合部门会议', 1, 1, 1),
(2, '洽谈室A', '1楼', '1楼西侧B区', 6, 'meeting', '["tv","whiteboard"]', '小型洽谈室', 1, 2, 1),
(3, '洽谈室B', '1楼', '1楼西侧B区', 6, 'meeting', '["tv","whiteboard"]', '小型洽谈室', 1, 3, 1),
(4, '培训室', '2楼', '2楼南侧', 30, 'training', '["projector","tv","whiteboard","音响"]', '培训专用教室', 1, 4, 1),
(5, '电话亭1号', '1楼', '1楼大厅', 2, 'phone', '["phone"]', '单人电话间', 1, 5, 1),
(6, '电话亭2号', '1楼', '1楼大厅', 2, 'phone', '["phone"]', '单人电话间', 1, 6, 1),
(7, '贵宾室', '3楼', '3楼西侧C区', 12, 'meeting', '["projector","tv","video_conference","coffee_machine"]', 'VIP会议室', 1, 7, 1),
(8, '头脑风暴室', '2楼', '2楼北侧', 8, 'meeting', '["whiteboard","tv"]', '创意讨论专用', 1, 8, 1),
(9, '项目室A', '2楼', '2楼东侧', 10, 'meeting', '["projector","whiteboard"]', '项目组专用', 1, 9, 1),
(10, '项目室B', '2楼', '2楼东侧', 10, 'meeting', '["projector","whiteboard"]', '项目组专用', 1, 10, 1),
(11, '会议室A', '4楼', '4楼东侧', 15, 'meeting', '["projector","tv","whiteboard"]', '中型会议室', 0, 11, 1),
(12, '会议室B', '4楼', '4楼西侧', 8, 'meeting', '["tv","whiteboard"]', '小型会议室', 2, 12, 1);

-- -----------------------------------------------------
-- 预约表测试数据 (15条)
-- -----------------------------------------------------
INSERT INTO booking (id, booking_no, room_id, user_id, booking_date, start_time, end_time, duration_min, topic, attendee_count, equipment_need, remark, recurring_id, source, status) VALUES
(1, 'BK20260914001', 1, 2, '2026-09-15', '09:00:00', '10:30:00', 90, 'V2.0需求评审', 8, '["projector","whiteboard"]', '请提前测试投影设备', NULL, 'manual', 'confirmed'),
(2, 'BK20260914002', 2, 3, '2026-09-15', '10:00:00', '11:00:00', 60, '产品方案讨论', 4, '["tv"]', NULL, NULL, 'manual', 'confirmed'),
(3, 'BK20260914003', 1, 4, '2026-09-15', '14:00:00', '15:30:00', 90, '技术架构评审', 6, '["projector","whiteboard"]', NULL, NULL, 'manual', 'confirmed'),
(4, 'BK20260914004', 3, 5, '2026-09-15', '09:30:00', '10:00:00', 30, '客户沟通', 3, '["tv"]', NULL, NULL, 'manual', 'confirmed'),
(5, 'BK20260914005', 4, 6, '2026-09-16', '14:00:00', '17:00:00', 180, '新员工培训', 25, '["projector","tv","whiteboard"]', '准备培训材料', NULL, 'manual', 'confirmed'),
(6, 'BK20260914006', 7, 2, '2026-09-16', '10:00:00', '11:30:00', 90, '投资人会议', 5, '["projector","video_conference"]', '重要会议', NULL, 'manual', 'confirmed'),
(7, 'BK20260914007', 5, 7, '2026-09-14', '14:00:00', '14:30:00', 30, '电话面试', 1, '["phone"]', NULL, NULL, 'manual', 'completed'),
(8, 'BK20260914008', 8, 3, '2026-09-17', '09:00:00', '10:00:00', 60, '产品头脑风暴', 6, '["whiteboard"]', '带好便利贴', NULL, 'manual', 'confirmed'),
(9, 'BK20260914009', 9, 4, '2026-09-17', '13:30:00', '15:00:00', 90, 'Sprint评审', 8, '["projector","whiteboard"]', NULL, NULL, 'manual', 'confirmed'),
(10, 'BK20260914010', 1, 5, '2026-09-18', '09:00:00', '10:00:00', 60, '周会', 15, '["projector"]', NULL, NULL, 'recurring', 'confirmed'),
(11, 'BK20260914011', 2, 6, '2026-09-14', '10:00:00', '11:00:00', 60, '供应商洽谈', 4, '["tv"]', NULL, NULL, 'manual', 'cancelled'),
(12, 'BK20260914012', 10, 10, '2026-09-19', '14:00:00', '16:00:00', 120, '代码评审', 6, '["projector"]', NULL, NULL, 'manual', 'confirmed'),
(13, 'BK20260914013', 1, 11, '2026-09-20', '10:00:00', '11:30:00', 90, '设计评审', 10, '["projector","tv"]', 'UI设计师参加', NULL, 'manual', 'pending'),
(14, 'BK20260914014', 3, 9, '2026-09-15', '14:00:00', '15:00:00', 60, '测试用例评审', 4, '["tv"]', NULL, NULL, 'manual', 'confirmed'),
(15, 'BK20260914015', 8, 12, '2026-09-16', '09:00:00', '10:30:00', 90, '运维会议', 5, '["whiteboard"]', '讨论服务器扩容', NULL, 'manual', 'confirmed');

-- -----------------------------------------------------
-- 系统配置表测试数据 (12条)
-- -----------------------------------------------------
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
('approval.timeout_hour', '2', '审批超时(小时)', '超时未审批自动拒绝'),
('credit.enabled', 'true', '是否启用信用分', '启用后爽约扣分、完成加分、周恢复'),
('credit.initial_score', '100', '初始信用分', '新用户默认信用分'),
('credit.max_score', '100', '信用分上限', '信用分最高值'),
('credit.no_show_deduct', '10', '爽约扣分', '预约过期未签到扣除的分值'),
('credit.complete_add', '2', '完成加分', '正常签到完成会议增加的分值'),
('credit.weekly_recover', '5', '每周恢复', '每周一恢复的分值'),
('credit.cancel_late_deduct', '5', '逾期取消扣分', '超过取消时限取消扣除的分值'),
('credit.threshold', '60', '低分阈值', '低于此值限制可预约天数'),
('credit.restrict_days', '3', '限制预约天数', '低分时只能预约未来N天内'),
('chatbot.enabled', 'true', '智能问答开关', '是否启用智能问答助手'),
('chatbot.provider', 'openai', '大模型提供方', 'openai兼容协议提供方标识'),
('chatbot.base_url', 'https://api.deepseek.com/v1', '大模型接口地址', 'OpenAI兼容的chat/completions基础地址'),
('chatbot.model', 'deepseek-chat', '大模型名称', '用于问答的模型'),
('chatbot.rate_limit_per_minute', '10', '每分钟提问上限', '单用户每分钟最多提问次数');

-- -----------------------------------------------------
-- 通知表测试数据 (15条)
-- -----------------------------------------------------
INSERT INTO notification (id, user_id, type, title, content, biz_type, biz_id, channel, send_status, is_read) VALUES
(1, 2, 'success', '预约成功', '您已成功预约创新厅 2026-09-15 09:00-10:30', 'booking', 1, 'inapp', 1, 1),
(2, 3, 'success', '预约成功', '您已成功预约洽谈室A 2026-09-15 10:00-11:00', 'booking', 2, 'inapp', 1, 1),
(3, 4, 'success', '预约成功', '您已成功预约创新厅 2026-09-15 14:00-15:30', 'booking', 3, 'inapp', 1, 0),
(4, 2, 'remind', '会议提醒', '您预约的创新厅会议将在15分钟后开始', 'booking', 1, 'inapp', 1, 0),
(5, 6, 'success', '预约成功', '您已成功预约贵宾室 2026-09-16 10:00-11:30', 'booking', 6, 'inapp', 1, 1),
(6, 6, 'cancel', '预约取消', '您预约的洽谈室A 2026-09-14 10:00-11:00 已被取消', 'booking', 11, 'inapp', 1, 1),
(7, 7, 'success', '预约成功', '您已成功预约电话亭1号 2026-09-14 14:00-14:30', 'booking', 7, 'inapp', 1, 1),
(8, 3, 'success', '预约成功', '您已成功预约头脑风暴室 2026-09-17 09:00-10:00', 'booking', 8, 'inapp', 1, 0),
(9, 4, 'success', '预约成功', '您已成功预约项目室A 2026-09-17 13:30-15:00', 'booking', 9, 'inapp', 1, 0),
(10, 5, 'success', '预约成功', '您已成功预约创新厅 2026-09-18 09:00-10:00', 'booking', 10, 'inapp', 1, 0),
(11, 2, 'remind', '会议提醒', '您预约的贵宾室会议将在15分钟后开始', 'booking', 6, 'inapp', 1, 0),
(12, 1, 'system', '系统通知', '系统将于今晚22:00进行维护升级', NULL, NULL, 'inapp', 1, 0),
(13, 12, 'success', '预约成功', '您已成功预约头脑风暴室 2026-09-16 09:00-10:30', 'booking', 15, 'inapp', 1, 0),
(14, 11, 'success', '预约成功', '您的预约已提交，等待审批', 'booking', 13, 'inapp', 1, 0),
(15, 1, 'system', '管理员通知', '会议室A已设为维护状态，请通知相关预约人', NULL, NULL, 'inapp', 1, 1);

-- -----------------------------------------------------
-- 操作日志表测试数据 (12条)
-- -----------------------------------------------------
INSERT INTO sys_operation_log (user_id, username, module, operation, method, params, ip, status, cost_time) VALUES
(1, 'admin', '会议室管理', '新增会议室', 'POST /api/v1/admin/rooms', '{"room_name":"创新厅","capacity":20}', '192.168.1.100', 1, 45),
(1, 'admin', '会议室管理', '编辑会议室', 'PUT /api/v1/admin/rooms/1', '{"capacity":25}', '192.168.1.100', 1, 32),
(1, 'admin', '预约管理', '强制取消预约', 'PUT /api/v1/admin/bookings/1/force-cancel', '{"reason":"会议室设备故障"}', '192.168.1.100', 1, 28),
(1, 'admin', '用户管理', '禁用用户', 'PUT /api/v1/admin/users/5/status', '{"status":0}', '192.168.1.100', 1, 15),
(1, 'admin', '用户管理', '重置密码', 'PUT /api/v1/admin/users/3/reset-password', '{}', '192.168.1.100', 1, 22),
(1, 'admin', '系统设置', '更新预约规则', 'PUT /api/v1/admin/config/booking-rules', '{"max_duration":"300"}', '192.168.1.100', 1, 18),
(12, 'huangsan', '会议室管理', '新增会议室', 'POST /api/v1/admin/rooms', '{"room_name":"电话亭3号","capacity":2}', '192.168.1.105', 1, 38),
(1, 'admin', '预约管理', '代客预约', 'POST /api/v1/admin/bookings/create-by-admin', '{"user_id":5,"room_id":2}', '192.168.1.100', 1, 52),
(1, 'admin', '工作日历', '新增节假日', 'POST /api/v1/admin/config/work-calendar', '{"date":"2026-10-01","type":3}', '192.168.1.100', 1, 12),
(1, 'admin', '会议室管理', '删除会议室', 'DELETE /api/v1/admin/rooms/12', '{}', '192.168.1.100', 1, 25),
(2, 'zhangsan', '预约', '创建预约', 'POST /api/v1/bookings', '{"room_id":1,"date":"2026-09-15"}', '192.168.1.101', 1, 85),
(3, 'lisi', '预约', '取消预约', 'PUT /api/v1/bookings/2/cancel', '{"reason":"会议推迟"}', '192.168.1.102', 1, 42);

-- -----------------------------------------------------
-- 部门配额表测试数据 (3条)
-- -----------------------------------------------------
INSERT INTO dept_quota (id, dept_id, quota_type, quota_value, period, enabled) VALUES
(1, 8, 'weekly', 10, 'week', 1),
(2, 9, 'weekly', 15, 'week', 1),
(3, 3, 'monthly', 50, 'month', 1);

-- -----------------------------------------------------
-- 会议室收藏表测试数据 (5条)
-- -----------------------------------------------------
INSERT INTO room_favorite (id, user_id, room_id, sort) VALUES
(1, 2, 1, 1),
(2, 2, 7, 2),
(3, 3, 8, 1),
(4, 4, 9, 1),
(5, 5, 1, 1);

-- -----------------------------------------------------
-- 周期预约组表测试数据 (2条)
-- -----------------------------------------------------
INSERT INTO recurring_booking (id, user_id, room_id, frequency, start_date, end_date, start_time, end_time, topic, attendee_count, equipment_need, enabled) VALUES
(1, 5, 1, 'weekly', '2026-09-15', '2026-10-15', '09:00:00', '10:00:00', '周会', 15, '["projector"]', 1),
(2, 3, 8, 'weekly', '2026-09-17', '2026-10-17', '09:00:00', '10:00:00', '产品头脑风暴', 6, '["whiteboard"]', 1);

-- -----------------------------------------------------
-- 参会人表测试数据 (8条)
-- -----------------------------------------------------
INSERT INTO booking_participant (id, booking_id, user_id, status) VALUES
(1, 1, 3, 'accepted'),
(2, 1, 4, 'accepted'),
(3, 1, 5, 'invited'),
(4, 2, 4, 'accepted'),
(5, 3, 2, 'invited'),
(6, 6, 5, 'accepted'),
(7, 8, 4, 'accepted'),
(8, 9, 3, 'invited');

-- -----------------------------------------------------
-- 候补队列表测试数据 (3条)
-- -----------------------------------------------------
INSERT INTO booking_waitlist (id, room_id, booking_date, start_time, end_time, user_id, rank_no, status) VALUES
(1, 1, '2026-09-15', '09:00:00', '10:30:00', 6, 1, 'waiting'),
(2, 1, '2026-09-15', '09:00:00', '10:30:00', 7, 2, 'waiting'),
(3, 2, '2026-09-15', '10:00:00', '11:00:00', 8, 1, 'waiting');

-- -----------------------------------------------------
-- 会议模板表测试数据 (3条)
-- -----------------------------------------------------
INSERT INTO booking_template (id, user_id, template_name, room_id, start_time, end_time, topic, attendee_count, equipment_need, remark) VALUES
(1, 2, '每周需求评审', 1, '14:00:00', '15:30:00', 'V2.0需求评审', 8, '["projector","whiteboard"]', '请提前测试投影设备'),
(2, 3, '产品头脑风暴', 8, '09:00:00', '10:00:00', '产品头脑风暴', 6, '["whiteboard"]', '带好便利贴'),
(3, 5, '部门周会', 1, '09:00:00', '10:00:00', '周会', 15, '["projector"]', NULL);

-- -----------------------------------------------------
-- 会议纪要表测试数据 (2条)
-- -----------------------------------------------------
INSERT INTO meeting_minutes (id, booking_id, author_id, agenda, content, attachment_list, submit_time) VALUES
(1, 7, 7, '["电话面试流程","候选人评估"]', '电话面试候选人吴九，面试结果：通过，建议进入下一轮', '["面试评估表.pdf"]', '2026-09-14 15:00:00'),
(2, 11, 6, '["供应商报价对比","合同条款"]', '与供应商洽谈结果：价格谈判成功， lowering 5%，下周签约', NULL, '2026-09-14 12:00:00');

-- -----------------------------------------------------
-- 报修工单表测试数据 (3条)
-- -----------------------------------------------------
INSERT INTO repair_order (id, order_no, room_id, reporter_id, device_type, fault_desc, images, status, assignee_id, handle_desc, finished_time) VALUES
(1, 'RP20260914001', 1, 2, 'projector', '投影仪无法开机，指示灯不亮', '["projection_issue.jpg"]', 'finished', 12, '更换投影仪灯泡，已修复', '2026-09-14 16:00:00'),
(2, 'RP20260914002', 8, 3, 'whiteboard', '白板擦拭不干净，有残留痕迹', NULL, 'processing', 12, '已安排清洁人员处理', NULL),
(3, 'RP20260914003', 7, 5, 'aircon', '空调制冷效果差，温度降不下来', '["aircon_issue.jpg"]', 'pending', NULL, NULL, NULL);

-- -----------------------------------------------------
-- 工作日历表测试数据 (15条)
-- -----------------------------------------------------
INSERT INTO work_calendar (id, calendar_date, date_type, holiday_name, remark) VALUES
(1, '2026-01-01', 3, '元旦', '元旦放假'),
(2, '2026-01-26', 3, '春节', '春节放假第一天'),
(3, '2026-01-27', 3, '春节', '春节放假'),
(4, '2026-01-28', 3, '春节', '春节放假'),
(5, '2026-01-29', 3, '春节', '春节放假'),
(6, '2026-01-30', 3, '春节', '春节放假'),
(7, '2026-01-31', 3, '春节', '春节放假'),
(8, '2026-02-01', 3, '春节', '春节放假最后一天'),
(9, '2026-02-07', 4, '调休', '春节调休上班'),
(10, '2026-04-04', 3, '清明节', '清明节放假'),
(11, '2026-05-01', 3, '劳动节', '劳动节放假第一天'),
(12, '2026-05-02', 3, '劳动节', '劳动节放假'),
(13, '2026-05-03', 3, '劳动节', '劳动节放假'),
(14, '2026-06-14', 3, '端午节', '端午节放假'),
(15, '2026-09-15', 5, '公司周年庆', '公司成立周年庆，放假一天');
