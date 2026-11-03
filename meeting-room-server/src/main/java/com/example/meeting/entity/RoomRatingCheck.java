package com.example.meeting.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 低分检查记录实体
 */
@Data
@TableName("room_rating_check")
public class RoomRatingCheck {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 评价ID
     */
    private Long ratingId;

    /**
     * 会议室ID
     */
    private Long roomId;

    /**
     * 状态: pending/converted/closed
     */
    private String status;

    /**
     * 关联报修工单ID
     */
    private Long repairId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
