package com.example.meeting.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 智能问答回答VO
 */
@Data
@Schema(description = "智能问答回答")
public class ChatBotAnswerVO {

    @Schema(description = "自然语言回答")
    private String answer;

    @Schema(description = "识别意图")
    private String intent;

    @Schema(description = "意图名称")
    private String intentName;

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "降级/提示原因")
    private String fallback;

    @Schema(description = "推荐会议室卡片")
    private List<RoomCard> rooms;

    @Schema(description = "预约卡片")
    private List<BookingCard> bookings;

    @Data
    @Schema(description = "会议室推荐卡片")
    public static class RoomCard {
        private Long roomId;
        private String roomName;
        private String floor;
        private String location;
        private Integer capacity;
        private List<String> equipment;
        private String equipmentText;
        private String date;
        private String timeRange;
        private Double avgRating;
    }

    @Data
    @Schema(description = "预约卡片")
    public static class BookingCard {
        private Long id;
        private String bookingNo;
        private String roomName;
        private String bookingDate;
        private String startTime;
        private String endTime;
        private String topic;
        private String status;
        private String statusName;
    }
}
