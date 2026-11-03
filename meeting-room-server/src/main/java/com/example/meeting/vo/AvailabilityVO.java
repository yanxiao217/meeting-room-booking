package com.example.meeting.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 可用时段VO
 */
@Data
@Schema(description = "可用时段信息")
public class AvailabilityVO {

    @Schema(description = "会议室ID")
    private Long roomId;

    @Schema(description = "日期")
    private String date;

    @Schema(description = "可用时段列表")
    private List<TimeSlot> availableSlots;

    @Schema(description = "相邻可用时段(冲突时推荐)")
    private List<TimeSlot> suggestedSlots;

    /**
     * 时段内部类
     */
    @Data
    @Schema(description = "时段信息")
    public static class TimeSlot {

        @Schema(description = "开始时间")
        private String startTime;

        @Schema(description = "结束时间")
        private String endTime;

        @Schema(description = "是否可用")
        private Boolean available;
    }
}
