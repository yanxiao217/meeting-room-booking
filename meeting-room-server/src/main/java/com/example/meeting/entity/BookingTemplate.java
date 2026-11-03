package com.example.meeting.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 会议模板实体
 */
@Data
@TableName("booking_template")
public class BookingTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 创建人
     */
    private Long userId;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 会议室ID(可选)
     */
    private Long roomId;

    /**
     * 开始时间(可选)
     */
    private LocalTime startTime;

    /**
     * 结束时间(可选)
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
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
