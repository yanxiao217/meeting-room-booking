package com.example.meeting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 创建预约请求DTO
 */
@Data
@Schema(description = "创建预约请求")
public class BookingCreateDTO {

    @NotNull(message = "会议室ID不能为空")
    @Schema(description = "会议室ID", required = true)
    private Long roomId;

    @NotNull(message = "预约日期不能为空")
    @FutureOrPresent(message = "不能预约过去的日期")
    @Schema(description = "预约日期", required = true)
    private LocalDate bookingDate;

    @NotNull(message = "开始时间不能为空")
    @Schema(description = "开始时间", required = true)
    private LocalTime startTime;

    @NotNull(message = "结束时间不能为空")
    @Schema(description = "结束时间", required = true)
    private LocalTime endTime;

    @NotBlank(message = "会议主题不能为空")
    @Size(min = 1, max = 100, message = "会议主题长度1-100字")
    @Schema(description = "会议主题", required = true)
    private String topic;

    @Min(value = 1, message = "参会人数不能小于1")
    @Schema(description = "参会人数")
    private Integer attendeeCount;

    @Schema(description = "设备需求")
    private List<String> equipmentNeed;

    @Size(max = 500, message = "备注不能超过500字")
    @Schema(description = "备注")
    private String remark;
}
