package com.example.meeting.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会议纪要实体
 */
@Data
@TableName("meeting_minutes")
public class MeetingMinutes {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 预约ID
     */
    private Long bookingId;

    /**
     * 填写人
     */
    private Long authorId;

    /**
     * 议题列表
     */
    private String agenda;

    /**
     * 纪要内容(富文本)
     */
    private String content;

    /**
     * 附件列表
     */
    private String attachmentList;

    /**
     * 提交时间
     */
    private LocalDateTime submitTime;

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
