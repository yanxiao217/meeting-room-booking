-- Migration v9: AI模型配置表（支持接入多种大模型）

CREATE TABLE IF NOT EXISTS ai_model_config (
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

INSERT INTO ai_model_config (name, provider, base_url, api_key, model, temperature, max_tokens, timeout_seconds, is_default, enabled, remark) VALUES
('DeepSeek', 'deepseek', 'https://api.deepseek.com/v1', '', 'deepseek-chat', 0.20, 2048, 20, 1, 1, '默认配置，填写API Key后即可启用'),
('通义千问', 'qwen', 'https://dashscope.aliyuncs.com/compatible-mode/v1', '', 'qwen-plus', 0.20, 2048, 20, 0, 0, '阿里云百炼 OpenAI 兼容模式'),
('智谱GLM', 'zhipu', 'https://open.bigmodel.cn/api/paas/v4', '', 'glm-4-flash', 0.20, 2048, 20, 0, 0, '智谱AI开放平台'),
('OpenAI', 'openai', 'https://api.openai.com/v1', '', 'gpt-4o-mini', 0.20, 2048, 20, 0, 0, 'OpenAI官方接口'),
('Ollama本地', 'ollama', 'http://localhost:11434/v1', 'ollama', 'qwen2.5', 0.20, 2048, 60, 0, 0, '本地部署，无需真实密钥');
