-- =====================================================
-- 迁移脚本 v10: 邮件通知功能
-- =====================================================

-- 1. sys_user 表添加 email 字段
-- =====================================================
DELIMITER //
CREATE PROCEDURE __migration_v10_add_email_if_absent()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE()
        AND table_name = 'sys_user'
        AND column_name = 'email'
    ) THEN
        ALTER TABLE sys_user ADD COLUMN email VARCHAR(100) COMMENT '邮箱地址' AFTER phone;
    END IF;
END //
DELIMITER ;

CALL __migration_v10_add_email_if_absent();
DROP PROCEDURE IF EXISTS __migration_v10_add_email_if_absent;

-- 2. 添加邮箱到已有测试用户
-- =====================================================
UPDATE sys_user SET email='zhangsan@example.com' WHERE username='zhangsan';
UPDATE sys_user SET email='admin@example.com' WHERE username='admin';

-- 3. SMTP 邮件配置
-- =====================================================
INSERT INTO sys_config (config_key, config_value, config_name, description) VALUES
('email.smtp.host', '', 'SMTP服务器', '邮件SMTP服务器地址（如smtp.qq.com）')
ON DUPLICATE KEY UPDATE config_value=VALUES(config_value);

INSERT INTO sys_config (config_key, config_value, config_name, description) VALUES
('email.smtp.port', '587', 'SMTP端口', '邮件SMTP端口（QQ邮箱587，Gmail 587，Outlook 587）')
ON DUPLICATE KEY UPDATE config_value=VALUES(config_value);

INSERT INTO sys_config (config_key, config_value, config_name, description) VALUES
('email.smtp.username', '', '发件人邮箱', 'SMTP登录邮箱地址')
ON DUPLICATE KEY UPDATE config_value=VALUES(config_value);

INSERT INTO sys_config (config_key, config_value, config_name, description) VALUES
('email.smtp.password', '', 'SMTP密码/授权码', 'SMTP登录密码或授权码')
ON DUPLICATE KEY UPDATE config_value=VALUES(config_value);

INSERT INTO sys_config (config_key, config_value, config_name, description) VALUES
('email.smtp.from-name', '会议室预约系统', '发件人名称', '邮件发件人显示名称')
ON DUPLICATE KEY UPDATE config_value=VALUES(config_value);

INSERT INTO sys_config (config_key, config_value, config_name, description) VALUES
('email.smtp.test-recipient', '', '测试收件人', '用于测试邮件发送的邮箱地址')
ON DUPLICATE KEY UPDATE config_value=VALUES(config_value);
