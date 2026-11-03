package com.example.meeting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 取消预约请求DTO
 */
@Data
@Schema(description = "取消预约请求")
public class BookingCancelDTO {

    @Schema(description = "取消原因")
    private String reason;
}
