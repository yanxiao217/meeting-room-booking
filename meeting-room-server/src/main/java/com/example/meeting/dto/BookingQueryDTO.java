package com.example.meeting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 预约查询请求DTO
 */
@Data
@Schema(description = "预约查询请求")
public class BookingQueryDTO {

    @Schema(description = "开始日期")
    private String startDate;

    @Schema(description = "结束日期")
    private String endDate;

    @Schema(description = "会议室ID")
    private Long roomId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "页码")
    private Integer pageNum = 1;

    @Schema(description = "每页数量")
    private Integer pageSize = 10;

    @Schema(description = "时间范围: true=仅进行中(未结束) false=仅历史(已结束) 不传=全部")
    private Boolean activeOnly;
}
