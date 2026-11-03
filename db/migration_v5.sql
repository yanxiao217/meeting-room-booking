-- =====================================================
-- 会议室预约管理平台 - 增量迁移脚本 v5.0
-- 用途: 信用分与爽约机制
-- 特点: 幂等（可重复执行）、非破坏性
-- 适用: MySQL 8.0+
-- 执行: mysql --default-character-set=utf8mb4 -uroot -p123456 meeting_room_booking -e "source db/migration_v5.sql"
-- =====================================================

USE meeting_room_booking;

-- =====================================================
-- 1. sys_user 加 credit_score 字段
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

CALL __add_col_if_absent('sys_user', 'credit_score', 'credit_score INT DEFAULT 100 COMMENT ''信用分'' AFTER `role`');

-- =====================================================
-- 2. credit_record 信用分记录表
-- =====================================================
CREATE TABLE IF NOT EXISTS credit_record (
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

DROP PROCEDURE IF EXISTS __add_col_if_absent;

-- =====================================================
-- 3. sys_config 新增信用分相关配置
-- =====================================================
INSERT IGNORE INTO sys_config (config_key, config_value, config_name, description) VALUES
('credit.enabled', 'true', '是否启用信用分', '启用后爽约扣分、完成加分、周恢复'),
('credit.initial_score', '100', '初始信用分', '新用户默认信用分'),
('credit.max_score', '100', '信用分上限', '信用分最高值'),
('credit.no_show_deduct', '10', '爽约扣分', '预约过期未签到扣除的分值'),
('credit.complete_add', '2', '完成加分', '正常签到完成会议增加的分值'),
('credit.weekly_recover', '5', '每周恢复', '每周一恢复的分值'),
('credit.cancel_late_deduct', '5', '逾期取消扣分', '超过取消时限取消扣除的分值'),
('credit.threshold', '60', '低分阈值', '低于此值限制可预约天数'),
('credit.restrict_days', '3', '限制预约天数', '低分时只能预约未来N天内');

-- 迁移完成
