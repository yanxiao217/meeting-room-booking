package com.example.meeting.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 参会人实体
 */
@Data
@TableName("booking_participant")
public class BookingParticipant {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 预约ID
     */
    private Long bookingId;

    /**
     * 参会人ID
     */
    private Long userId;

    /**
     * 状态: invited已邀请 accepted接受 declined拒绝
     */
    private String status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
