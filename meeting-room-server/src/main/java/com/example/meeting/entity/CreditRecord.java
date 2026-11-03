package com.example.meeting.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 信用分记录实体
 */
@Data
@TableName("credit_record")
public class CreditRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 变动类型: no_show/complete/weekly_recover/cancel_late/admin_adjust
     */
    private String changeType;

    /**
     * 变动分值（正数加分，负数扣分）
     */
    private Integer changeValue;

    /**
     * 变动前分值
     */
    private Integer beforeScore;

    /**
     * 变动后分值
     */
    private Integer afterScore;

    /**
     * 关联业务类型
     */
    private String bizType;

    /**
     * 关联业务ID
     */
    private Long bizId;

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
