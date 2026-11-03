package com.example.meeting.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 工作日历实体
 */
@Data
@TableName("work_calendar")
public class WorkCalendar {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 日期
     */
    private LocalDate calendarDate;

    /**
     * 类型: 1工作日 2周末 3节假日 4调休工作日 5特殊休息日
     */
    private Integer dateType;

    /**
     * 节假日名称
     */
    private String holidayName;

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
