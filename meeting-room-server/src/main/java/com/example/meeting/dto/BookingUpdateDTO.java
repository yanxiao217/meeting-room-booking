package com.example.meeting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 修改预约请求DTO
 */
@Data
@Schema(description = "修改预约请求")
public class BookingUpdateDTO {

    @Schema(description = "会议室ID")
    private Long roomId;

    @Schema(description = "预约日期")
    private LocalDate bookingDate;

    @Schema(description = "开始时间")
    private LocalTime startTime;

    @Schema(description = "结束时间")
    private LocalTime endTime;

    @Schema(description = "会议主题")
    private String topic;

    @Schema(description = "参会人数")
    private Integer attendeeCount;

    @Schema(description = "设备需求")
    private List<String> equipmentNeed;

    @Schema(description = "备注")
    private String remark;
}
