package com.example.meeting.common.constant;

/**
 * 会议室相关常量
 */
public class RoomConstant {

    /**
     * 会议室状态：维护中
     */
    public static final Integer STATUS_MAINTENANCE = 0;

    /**
     * 会议室状态：可用
     */
    public static final Integer STATUS_AVAILABLE = 1;

    /**
     * 会议室状态：停用
     */
    public static final Integer STATUS_DISABLED = 2;

    /**
     * 会议室类型：会议室
     */
    public static final String TYPE_MEETING = "meeting";

    /**
     * 会议室类型：培训室
     */
    public static final String TYPE_TRAINING = "training";

    /**
     * 会议室类型：电话亭
     */
    public static final String TYPE_PHONE = "phone";

    private RoomConstant() {
    }
}
