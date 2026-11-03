-- =====================================================
-- 会议室预约管理平台 - 增量迁移脚本 v4.0
-- 用途: 支持"特定会议室需审批"（全局审批开关 + 会议室维度开关）
-- 特点: 幂等（可重复执行）、非破坏性
-- 适用: MySQL 8.0+
-- 执行: mysql --default-character-set=utf8mb4 -uroot -p123456 meeting_room_booking -e "source db/migration_v4.sql"
-- =====================================================

USE meeting_room_booking;

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

CALL __add_col_if_absent('meeting_room', 'need_approval', 'need_approval TINYINT DEFAULT 0 COMMENT ''预约是否需要审批: 0否 1是''');

DROP PROCEDURE IF EXISTS __add_col_if_absent;

-- 示例：将"贵宾室"(id=7)设为需审批
UPDATE meeting_room SET need_approval = 1 WHERE id = 7;

-- 迁移完成
