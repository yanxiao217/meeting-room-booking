-- Migration v8: 智能问答助手 — 日志表 + 配置项

CREATE TABLE IF NOT EXISTS chatbot_log (
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

INSERT INTO sys_config (config_key, config_value, config_name, description) VALUES
('chatbot.enabled', 'true', '智能问答开关', '是否启用智能问答助手'),
('chatbot.provider', 'openai', '大模型提供方', 'openai兼容协议提供方标识'),
('chatbot.base_url', 'https://api.deepseek.com/v1', '大模型接口地址', 'OpenAI兼容的chat/completions基础地址'),
('chatbot.model', 'deepseek-chat', '大模型名称', '用于问答的模型'),
('chatbot.rate_limit_per_minute', '10', '每分钟提问上限', '单用户每分钟最多提问次数')
ON DUPLICATE KEY UPDATE config_name = VALUES(config_name);
