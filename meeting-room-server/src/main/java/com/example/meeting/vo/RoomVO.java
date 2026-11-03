package com.example.meeting.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会议室信息VO
 */
@Data
@Schema(description = "会议室信息")
public class RoomVO {

    @Schema(description = "会议室ID")
    private Long id;

    @Schema(description = "会议室名称")
    private String roomName;

    @Schema(description = "所在楼层")
    private String floor;

    @Schema(description = "具体位置")
    private String location;

    @Schema(description = "容纳人数")
    private Integer capacity;

    @Schema(description = "类型")
    private String roomType;

    @Schema(description = "类型名称")
    private String roomTypeName;

    @Schema(description = "配备设备")
    private List<String> equipment;

    @Schema(description = "图片列表")
    private List<String> images;

    @Schema(description = "描述/备注")
    private String description;

    @Schema(description = "状态: 0维护中 1可用 2停用")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "预约是否需要审批: 0否 1是")
    private Integer needApproval;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "今日预约数")
    private Integer todayBookingCount;

    @Schema(description = "平均评分")
    private Double avgRating;

    @Schema(description = "评价数量")
    private Integer ratingCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
