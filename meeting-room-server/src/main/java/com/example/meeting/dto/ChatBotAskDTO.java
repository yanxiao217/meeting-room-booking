package com.example.meeting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 智能问答提问DTO
 */
@Data
@Schema(description = "智能问答提问")
public class ChatBotAskDTO {

    @NotBlank(message = "问题不能为空")
    @Size(max = 500, message = "问题过长")
    @Schema(description = "用户问题")
    private String question;
}
