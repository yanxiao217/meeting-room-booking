-- =====================================================
-- 会议室预约管理平台 - 增量迁移脚本 v6.0
-- 用途: 会议室评价系统
-- 特点: 幂等（可重复执行）、非破坏性
-- 适用: MySQL 8.0+
-- 执行: mysql --default-character-set=utf8mb4 -uroot -p123456 meeting_room_booking -e "source db/migration_v6.sql"
-- =====================================================

USE meeting_room_booking;

-- =====================================================
-- 1. room_rating 会议室评价表
-- =====================================================
CREATE TABLE IF NOT EXISTS room_rating (
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
-- 2. room_rating_check 低分检查记录表
-- =====================================================
CREATE TABLE IF NOT EXISTS room_rating_check (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rating_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'pending' COMMENT 'pending/converted/closed',
    repair_id BIGINT,
    remark VARCHAR(200),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_rating (rating_id)
) COMMENT '低分检查记录表';

-- 迁移完成
