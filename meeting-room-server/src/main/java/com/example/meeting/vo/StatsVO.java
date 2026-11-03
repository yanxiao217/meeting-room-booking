package com.example.meeting.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 数据统计VO
 */
@Data
@Schema(description = "数据统计")
public class StatsVO {

    @Schema(description = "会议室ID/名称")
    private Long roomId;

    @Schema(description = "会议室名称")
    private String roomName;

    @Schema(description = "预约次数")
    private Integer bookingCount;

    @Schema(description = "使用率(%)")
    private BigDecimal usageRate;

    @Schema(description = "平均时长(分钟)")
    private Integer avgDuration;

    @Schema(description = "预约总时长(分钟)")
    private Integer totalDuration;
}
