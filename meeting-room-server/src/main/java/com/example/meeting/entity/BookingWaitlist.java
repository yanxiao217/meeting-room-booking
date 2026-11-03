package com.example.meeting.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 候补队列实体
 */
@Data
@TableName("booking_waitlist")
public class BookingWaitlist {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会议室ID
     */
    private Long roomId;

    /**
     * 日期
     */
    private LocalDate bookingDate;

    /**
     * 开始时间
     */
    private LocalTime startTime;

    /**
     * 结束时间
     */
    private LocalTime endTime;

    /**
     * 候补人ID
     */
    private Long userId;

    /**
     * 候补顺序
     */
    private Integer rankNo;

    /**
     * 状态: waiting等待 notified已通知 confirmed已确认 cancelled已放弃 expired已超时
     */
    private String status;

    /**
     * 通知时间
     */
    private LocalDateTime notifyTime;

    /**
     * 确认截止时间
     */
    private LocalDateTime confirmDeadline;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
