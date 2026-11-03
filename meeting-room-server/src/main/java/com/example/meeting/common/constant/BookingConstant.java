package com.example.meeting.common.constant;

/**
 * 预约相关常量
 */
public class BookingConstant {

    /**
     * 预约状态：待审批
     */
    public static final String STATUS_PENDING = "pending";

    /**
     * 预约状态：已确认
     */
    public static final String STATUS_CONFIRMED = "confirmed";

    /**
     * 预约状态：已签到
     */
    public static final String STATUS_SIGNED = "signed";

    /**
     * 预约状态：已完成
     */
    public static final String STATUS_COMPLETED = "completed";

    /**
     * 预约状态：已取消
     */
    public static final String STATUS_CANCELLED = "cancelled";

    /**
     * 预约状态：已过期
     */
    public static final String STATUS_EXPIRED = "expired";

    /**
     * 预约编号前缀
     */
    public static final String BOOKING_NO_PREFIX = "BK";

    private BookingConstant() {
    }
}
