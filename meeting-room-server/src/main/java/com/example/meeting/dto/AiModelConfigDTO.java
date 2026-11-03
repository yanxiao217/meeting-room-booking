package com.example.meeting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

/**
 * AI模型配置DTO
 */
@Data
@Schema(description = "AI模型配置")
public class AiModelConfigDTO {

    @Schema(description = "配置ID(更新时必填)")
    private Long id;

    @NotBlank(message = "配置名称不能为空")
    @Schema(description = "配置名称")
    private String name;

    @Schema(description = "提供方")
    private String provider;

    @NotBlank(message = "接口地址不能为空")
    @Schema(description = "接口基础地址")
    private String baseUrl;

    @Schema(description = "API密钥")
    private String apiKey;

    @NotBlank(message = "模型名称不能为空")
    @Schema(description = "模型名称")
    private String model;

    @Schema(description = "采样温度")
    private BigDecimal temperature;

    @Schema(description = "最大输出tokens")
    private Integer maxTokens;

    @Schema(description = "读取超时(秒)")
    private Integer timeoutSeconds;

    @Schema(description = "是否默认")
    private Integer isDefault;

    @Schema(description = "是否启用")
    private Integer enabled;

    @Schema(description = "备注")
    private String remark;
}
