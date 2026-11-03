package com.example.meeting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 会议室创建/编辑请求DTO
 */
@Data
@Schema(description = "会议室请求")
public class RoomDTO {

    @NotBlank(message = "会议室名称不能为空")
    @Schema(description = "会议室名称", required = true)
    private String roomName;

    @NotBlank(message = "所在楼层不能为空")
    @Schema(description = "所在楼层", required = true)
    private String floor;

    @Schema(description = "具体位置")
    private String location;

    @NotNull(message = "容纳人数不能为空")
    @Schema(description = "容纳人数", required = true)
    private Integer capacity;

    @Schema(description = "类型: meeting培训室 training培训室 phone电话亭")
    private String roomType;

    @Schema(description = "配备设备")
    private List<String> equipment;

    @Schema(description = "图片列表")
    private List<String> images;

    @Schema(description = "描述/备注")
    private String description;

    @Schema(description = "状态: 0维护中 1可用 2停用")
    private Integer status;

    @Schema(description = "预约是否需要审批: 0否 1是")
    private Integer needApproval;

    @Schema(description = "排序")
    private Integer sort;
}
