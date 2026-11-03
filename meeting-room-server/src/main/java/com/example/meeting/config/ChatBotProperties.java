package com.example.meeting.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 智能问答助手配置（OpenAI兼容协议）
 */
@Data
@Component
@ConfigurationProperties(prefix = "chatbot")
public class ChatBotProperties {

    /**
     * 是否启用大模型（关闭时使用规则解析兜底）
     */
    private boolean enabled = true;

    /**
     * 提供方标识
     */
    private String provider = "openai";

    /**
     * OpenAI兼容接口基础地址，如 https://api.deepseek.com/v1
     */
    private String baseUrl = "https://api.deepseek.com/v1";

    /**
     * API密钥（仅服务端持有）
     */
    private String apiKey = "";

    /**
     * 模型名称
     */
    private String model = "deepseek-chat";

    /**
     * 读取超时(秒)
     */
    private int timeoutSeconds = 20;

    /**
     * 单用户每分钟提问上限
     */
    private int rateLimitPerMinute = 10;

    /**
     * 采样温度
     */
    private double temperature = 0.2;
}
