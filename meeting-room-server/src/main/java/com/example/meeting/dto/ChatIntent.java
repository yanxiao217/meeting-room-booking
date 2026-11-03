package com.example.meeting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 解析后的问答意图（仅参数，不含SQL）
 */
@Data
@Schema(description = "问答意图")
public class ChatIntent {

    public static final String FIND_ROOMS = "find_available_rooms";
    public static final String MY_BOOKINGS = "my_bookings";
    public static final String ROOM_INFO = "room_info";
    public static final String UNKNOWN = "unknown";

    @Schema(description = "意图")
    private String intent = UNKNOWN;

    @Schema(description = "日期 yyyy-MM-dd")
    private String date;

    @Schema(description = "开始时间 HH:mm")
    private String startTime;

    @Schema(description = "结束时间 HH:mm")
    private String endTime;

    @Schema(description = "容纳人数")
    private Integer capacity;

    @Schema(description = "设备编码列表")
    private List<String> equipment;

    @Schema(description = "楼层")
    private String floor;

    @Schema(description = "关键词(会议室名)")
    private String keyword;

    @Schema(description = "预约状态")
    private String status;

    @Schema(description = "解析来源: llm/rule")
    private String source;
}
