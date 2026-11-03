package com.example.meeting.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 时段VO
 */
@Data
@Schema(description = "时段信息")
public class TimeSlotVO {

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "是否已预约")
    private Boolean booked;

    @Schema(description = "预约ID")
    private Long bookingId;

    @Schema(description = "会议主题(仅显示自己的)")
    private String topic;

    @Schema(description = "是否是自己的预约")
    private Boolean isMine;
}
