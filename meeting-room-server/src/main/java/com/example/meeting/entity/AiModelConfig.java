package com.example.meeting.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI模型配置实体
 */
@Data
@TableName("ai_model_config")
public class AiModelConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 配置名称
     */
    private String name;

    /**
     * 提供方: openai/deepseek/qwen/zhipu/ollama/custom
     */
    private String provider;

    /**
     * 接口基础地址(OpenAI兼容)
     */
    private String baseUrl;

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 采样温度
     */
    private BigDecimal temperature;

    /**
     * 最大输出tokens
     */
    private Integer maxTokens;

    /**
     * 读取超时(秒)
     */
    private Integer timeoutSeconds;

    /**
     * 是否默认: 0否 1是
     */
    private Integer isDefault;

    /**
     * 是否启用: 0否 1是
     */
    private Integer enabled;

    /**
     * 备注
     */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
