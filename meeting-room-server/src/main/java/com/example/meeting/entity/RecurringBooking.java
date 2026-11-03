package com.example.meeting.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 周期预约组实体
 */
@Data
@TableName("recurring_booking")
public class RecurringBooking {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 创建人
     */
    private Long userId;

    /**
     * 会议室
     */
    private Long roomId;

    /**
     * 频率: daily/weekly/monthly
     */
    private String frequency;

    /**
     * 起始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 开始时间
     */
    private LocalTime startTime;

    /**
     * 结束时间
     */
    private LocalTime endTime;

    /**
     * 会议主题
     */
    private String topic;

    /**
     * 参会人数
     */
    private Integer attendeeCount;

    /**
     * 设备需求
     */
    private String equipmentNeed;

    /**
     * 是否有效
     */
    private Integer enabled;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
