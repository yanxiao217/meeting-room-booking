package com.example.meeting.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 会议室日历VO
 */
@Data
@Schema(description = "会议室日历信息")
public class RoomCalendarVO {

    @Schema(description = "会议室ID")
    private Long roomId;

    @Schema(description = "会议室名称")
    private String roomName;

    @Schema(description = "楼层")
    private String floor;

    @Schema(description = "容量")
    private Integer capacity;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "预约时段列表")
    private List<TimeSlotVO> bookedSlots;
}
