-- =====================================================
-- 会议室预约管理平台 - 增量迁移脚本 v3.0
-- 用途: 为已存在的旧版数据库补齐 PRD v3.0 新增的表与字段
-- 特点: 幂等（可重复执行）、非破坏性（不删除已有数据）
-- 适用: MySQL 8.0+
-- 执行: mysql -uroot -p123456 meeting_room_booking -e "source db/migration_v3.sql"
-- =====================================================

USE meeting_room_booking;

-- =====================================================
-- 1. 新增列（幂等：列不存在时才添加）
-- =====================================================
DROP PROCEDURE IF EXISTS __add_col_if_absent;
DELIMITER $$
CREATE PROCEDURE __add_col_if_absent(IN tbl VARCHAR(64), IN col VARCHAR(64), IN ddl TEXT)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tbl AND COLUMN_NAME = col
    ) THEN
        SET @ddl_sql = CONCAT('ALTER TABLE `', tbl, '` ADD COLUMN ', ddl);
        PREPARE stmt FROM @ddl_sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

CALL __add_col_if_absent('meeting_room', 'floor_map_x', 'floor_map_x DECIMAL(6,2) COMMENT ''楼层图X坐标(%)''');
CALL __add_col_if_absent('meeting_room', 'floor_map_y', 'floor_map_y DECIMAL(6,2) COMMENT ''楼层图Y坐标(%)''');
CALL __add_col_if_absent('booking', 'recurring_id', 'recurring_id BIGINT COMMENT ''周期预约组ID''');
CALL __add_col_if_absent('booking', 'source', 'source VARCHAR(20) DEFAULT ''manual'' COMMENT ''来源: manual表单 recommend推荐 template模板 recurring周期 nl自然语言''');
CALL __add_col_if_absent('booking', 'release_time', 'release_time DATETIME COMMENT ''自动释放时间''');
CALL __add_col_if_absent('notification', 'channel', 'channel VARCHAR(20) DEFAULT ''inapp'' COMMENT ''渠道: inapp站内 email邮件 wecom企业微信''');
CALL __add_col_if_absent('notification', 'send_status', 'send_status TINYINT DEFAULT 1 COMMENT ''发送状态''');

DROP PROCEDURE IF EXISTS __add_col_if_absent;

-- 补充索引（幂等）
DROP PROCEDURE IF EXISTS __add_idx_if_absent;
DELIMITER $$
CREATE PROCEDURE __add_idx_if_absent(IN tbl VARCHAR(64), IN idx VARCHAR(64), IN ddl TEXT)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tbl AND INDEX_NAME = idx
    ) THEN
        SET @idx_sql = CONCAT('ALTER TABLE `', tbl, '` ADD ', ddl);
        PREPARE stmt FROM @idx_sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

CALL __add_idx_if_absent('booking', 'idx_recurring', 'KEY idx_recurring (recurring_id)');

DROP PROCEDURE IF EXISTS __add_idx_if_absent;

-- =====================================================
-- 2. 新增表（幂等：表不存在时才创建）
-- =====================================================

-- 部门配额表
CREATE TABLE IF NOT EXISTS dept_quota (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dept_id BIGINT NOT NULL COMMENT '部门ID',
    quota_type VARCHAR(20) NOT NULL COMMENT '类型: weekly次数 monthly时长',
    quota_value INT NOT NULL COMMENT '配额值',
    period VARCHAR(10) DEFAULT 'week' COMMENT '周期: week/month',
    enabled TINYINT DEFAULT 0 COMMENT '是否启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_dept_type (dept_id, quota_type)
) COMMENT '部门配额表';

-- 会议室收藏表
CREATE TABLE IF NOT EXISTS room_favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    room_id BIGINT NOT NULL COMMENT '会议室ID',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_room (user_id, room_id)
) COMMENT '会议室收藏表';

-- 周期预约组表
CREATE TABLE IF NOT EXISTS recurring_booking (
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
CREATE TABLE IF NOT EXISTS booking_participant (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL COMMENT '预约ID',
    user_id BIGINT NOT NULL COMMENT '参会人ID',
    status VARCHAR(20) DEFAULT 'invited' COMMENT '状态: invited已邀请 accepted接受 declined拒绝',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_booking_user (booking_id, user_id)
) COMMENT '参会人表';

-- 候补队列表
CREATE TABLE IF NOT EXISTS booking_waitlist (
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
CREATE TABLE IF NOT EXISTS booking_template (
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

-- 会议纪要表
CREATE TABLE IF NOT EXISTS meeting_minutes (
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

-- 报修工单表
CREATE TABLE IF NOT EXISTS repair_order (
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
-- 3. 补充系统配置项（幂等）
-- =====================================================
INSERT IGNORE INTO sys_config (config_key, config_value, config_name, description) VALUES
('waitlist.enabled', 'true', '是否开启候补', '冲突时可加入候补队列'),
('waitlist.confirm_min', '10', '候补确认时限(分钟)', '通知后N分钟内确认，超时顺延'),
('waitlist.max_rank', '5', '候补队列上限', '同一时段每会议室候补人数上限'),
('recurring.max_count', '4', '周期预约最大次数', '单次周期预约最多生成次数'),
('dept_quota.enabled', 'false', '是否启用部门配额', '启用后按部门限制预约'),
('recommend.enabled', 'true', '是否启用智能推荐', '智能推荐会议室开关'),
('approval.enabled', 'false', '是否启用审批', '特定会议室预约需审批'),
('approval.timeout_hour', '2', '审批超时(小时)', '超时未审批自动拒绝');

-- =====================================================
-- 4. 新增表示例数据（幂等：主键冲突时忽略）
-- =====================================================
INSERT IGNORE INTO dept_quota (id, dept_id, quota_type, quota_value, period, enabled) VALUES
(1, 8, 'weekly', 10, 'week', 1),
(2, 9, 'weekly', 15, 'week', 1),
(3, 3, 'monthly', 50, 'month', 1);

INSERT IGNORE INTO room_favorite (id, user_id, room_id, sort) VALUES
(1, 2, 1, 1),
(2, 2, 7, 2),
(3, 3, 8, 1),
(4, 4, 9, 1),
(5, 5, 1, 1);

INSERT IGNORE INTO recurring_booking (id, user_id, room_id, frequency, start_date, end_date, start_time, end_time, topic, attendee_count, equipment_need, enabled) VALUES
(1, 5, 1, 'weekly', '2026-09-15', '2026-10-15', '09:00:00', '10:00:00', '周会', 15, '["projector"]', 1),
(2, 3, 8, 'weekly', '2026-09-17', '2026-10-17', '09:00:00', '10:00:00', '产品头脑风暴', 6, '["whiteboard"]', 1);

INSERT IGNORE INTO booking_participant (id, booking_id, user_id, status) VALUES
(1, 1, 3, 'accepted'),
(2, 1, 4, 'accepted'),
(3, 1, 5, 'invited'),
(4, 2, 4, 'accepted'),
(5, 3, 2, 'invited'),
(6, 6, 5, 'accepted'),
(7, 8, 4, 'accepted'),
(8, 9, 3, 'invited');

INSERT IGNORE INTO booking_waitlist (id, room_id, booking_date, start_time, end_time, user_id, rank_no, status) VALUES
(1, 1, '2026-09-15', '09:00:00', '10:30:00', 6, 1, 'waiting'),
(2, 1, '2026-09-15', '09:00:00', '10:30:00', 7, 2, 'waiting'),
(3, 2, '2026-09-15', '10:00:00', '11:00:00', 8, 1, 'waiting');

INSERT IGNORE INTO booking_template (id, user_id, template_name, room_id, start_time, end_time, topic, attendee_count, equipment_need, remark) VALUES
(1, 2, '每周需求评审', 1, '14:00:00', '15:30:00', 'V2.0需求评审', 8, '["projector","whiteboard"]', '请提前测试投影设备'),
(2, 3, '产品头脑风暴', 8, '09:00:00', '10:00:00', '产品头脑风暴', 6, '["whiteboard"]', '带好便利贴'),
(3, 5, '部门周会', 1, '09:00:00', '10:00:00', '周会', 15, '["projector"]', NULL);

INSERT IGNORE INTO meeting_minutes (id, booking_id, author_id, agenda, content, attachment_list, submit_time) VALUES
(1, 7, 7, '["电话面试流程","候选人评估"]', '电话面试候选人吴九，面试结果：通过，建议进入下一轮', '["面试评估表.pdf"]', '2026-09-14 15:00:00'),
(2, 11, 6, '["供应商报价对比","合同条款"]', '与供应商洽谈结果：价格谈判成功，下周签约', NULL, '2026-09-14 12:00:00');

INSERT IGNORE INTO repair_order (id, order_no, room_id, reporter_id, device_type, fault_desc, images, status, assignee_id, handle_desc, finished_time) VALUES
(1, 'RP20260914001', 1, 2, 'projector', '投影仪无法开机，指示灯不亮', '["projection_issue.jpg"]', 'finished', 12, '更换投影仪灯泡，已修复', '2026-09-14 16:00:00'),
(2, 'RP20260914002', 8, 3, 'whiteboard', '白板擦拭不干净，有残留痕迹', NULL, 'processing', 12, '已安排清洁人员处理', NULL),
(3, 'RP20260914003', 7, 5, 'aircon', '空调制冷效果差，温度降不下来', '["aircon_issue.jpg"]', 'pending', NULL, NULL, NULL);

-- =====================================================
-- 迁移完成
-- =====================================================
