package com.example.meeting.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 预约信息VO
 */
@Data
@Schema(description = "预约信息")
public class BookingVO {

    @Schema(description = "预约ID")
    private Long id;

    @Schema(description = "预约编号")
    private String bookingNo;

    @Schema(description = "会议室ID")
    private Long roomId;

    @Schema(description = "会议室名称")
    private String roomName;

    @Schema(description = "会议室位置")
    private String roomLocation;

    @Schema(description = "预约人ID")
    private Long userId;

    @Schema(description = "预约人姓名")
    private String userName;

    @Schema(description = "预约日期")
    private LocalDate bookingDate;

    @Schema(description = "开始时间")
    private LocalTime startTime;

    @Schema(description = "结束时间")
    private LocalTime endTime;

    @Schema(description = "时长(分钟)")
    private Integer durationMin;

    @Schema(description = "会议主题")
    private String topic;

    @Schema(description = "参会人数")
    private Integer attendeeCount;

    @Schema(description = "设备需求")
    private List<String> equipmentNeed;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "取消原因")
    private String cancelReason;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
