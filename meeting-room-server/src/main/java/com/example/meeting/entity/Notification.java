package com.example.meeting.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知实体
 */
@Data
@TableName("notification")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 接收用户ID
     */
    private Long userId;

    /**
     * 类型: success预约成功 remind开始提醒 cancel取消通知 invite参会邀请 waitlist候补通知 repair报修通知 minutes纪要提醒 system系统通知
     */
    private String type;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 业务类型: booking/waitlist/repair/minutes
     */
    private String bizType;

    /**
     * 业务ID
     */
    private Long bizId;

    /**
     * 渠道: inapp站内 email邮件 wecom企业微信
     */
    private String channel;

    /**
     * 发送状态
     */
    private Integer sendStatus;

    /**
     * 是否已读: 0未读 1已读
     */
    private Integer isRead;

    /**
     * 阅读时间
     */
    private LocalDateTime readTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
