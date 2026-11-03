package com.example.meeting.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会议室评价实体
 */
@Data
@TableName("room_rating")
public class RoomRating {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 预约ID
     */
    private Long bookingId;

    /**
     * 会议室ID
     */
    private Long roomId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 评分 1-5
     */
    private Integer rating;

    /**
     * 标签JSON
     */
    private String tags;

    /**
     * 评价文字
     */
    private String comment;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
