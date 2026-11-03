package com.example.meeting.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 日历视图VO
 */
@Data
@Schema(description = "日历视图数据")
public class CalendarVO {

    @Schema(description = "日期范围描述")
    private String dateRange;

    @Schema(description = "会议室列表")
    private List<RoomCalendarVO> rooms;
}
