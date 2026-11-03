package com.example.meeting.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 预约实体
 */
@Data
@TableName("booking")
public class Booking {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 预约编号
     */
    private String bookingNo;

    /**
     * 会议室ID
     */
    private Long roomId;

    /**
     * 预约人ID
     */
    private Long userId;

    /**
     * 预约日期
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
     * 时长(分钟)
     */
    private Integer durationMin;

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
     * 备注
     */
    private String remark;

    /**
     * 周期预约组ID
     */
    private Long recurringId;

    /**
     * 来源: manual表单 recommend推荐 template模板 recurring周期 nl自然语言
     */
    private String source;

    /**
     * 状态: pending待审批 confirmed已确认 signed已签到 completed已完成 cancelled已取消 expired已过期
     */
    private String status;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 取消人ID
     */
    private Long cancelBy;

    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;

    /**
     * 签到时间
     */
    private LocalDateTime signTime;

    /**
     * 签到来源: manual手动 qrcode二维码
     */
    private String signSource;

    /**
     * 自动释放时间
     */
    private LocalDateTime releaseTime;

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
