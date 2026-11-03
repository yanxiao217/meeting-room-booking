package com.example.meeting.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会议室实体
 */
@Data
@TableName("meeting_room")
public class MeetingRoom {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会议室名称
     */
    private String roomName;

    /**
     * 所在楼层
     */
    private String floor;

    /**
     * 具体位置
     */
    private String location;

    /**
     * 楼层图X坐标(%)
     */
    private BigDecimal floorMapX;

    /**
     * 楼层图Y坐标(%)
     */
    private BigDecimal floorMapY;

    /**
     * 容纳人数
     */
    private Integer capacity;

    /**
     * 类型: meeting会议室 training培训室 phone电话亭
     */
    private String roomType;

    /**
     * 配备设备
     */
    private String equipment;

    /**
     * 图片列表
     */
    private String images;

    /**
     * 描述/备注
     */
    private String description;

    /**
     * 状态: 0维护中 1可用 2停用
     */
    private Integer status;

    /**
     * 预约是否需要审批: 0否 1是
     */
    private Integer needApproval;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 创建人
     */
    private Long createBy;

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
