package com.example.meeting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 管理员代客预约请求DTO
 */
@Data
@Schema(description = "管理员代客预约请求")
public class AdminBookingDTO {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "预约人用户ID", required = true)
    private Long userId;

    @NotNull(message = "会议室ID不能为空")
    @Schema(description = "会议室ID", required = true)
    private Long roomId;

    @NotNull(message = "预约日期不能为空")
    @Schema(description = "预约日期", required = true)
    private LocalDate bookingDate;

    @NotNull(message = "开始时间不能为空")
    @Schema(description = "开始时间", required = true)
    private LocalTime startTime;

    @NotNull(message = "结束时间不能为空")
    @Schema(description = "结束时间", required = true)
    private LocalTime endTime;

    @NotBlank(message = "会议主题不能为空")
    @Schema(description = "会议主题", required = true)
    private String topic;

    @Schema(description = "参会人数")
    private Integer attendeeCount;

    @Schema(description = "设备需求")
    private List<String> equipmentNeed;

    @Schema(description = "备注")
    private String remark;
}
