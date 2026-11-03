package com.example.meeting.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报修工单实体
 */
@Data
@TableName("repair_order")
public class RepairOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 工单编号
     */
    private String orderNo;

    /**
     * 会议室ID
     */
    private Long roomId;

    /**
     * 报修人ID
     */
    private Long reporterId;

    /**
     * 设备类型: projector/tv/whiteboard/aircon/light/other
     */
    private String deviceType;

    /**
     * 故障描述
     */
    private String faultDesc;

    /**
     * 故障图片
     */
    private String images;

    /**
     * 状态: pending待处理 processing处理中 finished已完成
     */
    private String status;

    /**
     * 处理人ID
     */
    private Long assigneeId;

    /**
     * 处理说明
     */
    private String handleDesc;

    /**
     * 完成时间
     */
    private LocalDateTime finishedTime;

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
