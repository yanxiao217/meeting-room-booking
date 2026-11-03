package com.example.meeting.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 部门配额实体
 */
@Data
@TableName("dept_quota")
public class DeptQuota {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 类型: weekly次数 monthly时长
     */
    private String quotaType;

    /**
     * 配额值
     */
    private Integer quotaValue;

    /**
     * 周期: week/month
     */
    private String period;

    /**
     * 是否启用
     */
    private Integer enabled;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
