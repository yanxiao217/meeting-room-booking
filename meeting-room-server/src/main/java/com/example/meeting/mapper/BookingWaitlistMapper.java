package com.example.meeting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.meeting.entity.BookingWaitlist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 候补队列Mapper接口
 */
@Mapper
public interface BookingWaitlistMapper extends BaseMapper<BookingWaitlist> {

    /**
     * 查询指定时段的候补队列
     */
    @Select("SELECT * FROM booking_waitlist " +
            "WHERE room_id = #{roomId} " +
            "AND booking_date = #{bookingDate} " +
            "AND start_time = #{startTime} " +
            "AND end_time = #{endTime} " +
            "AND status IN ('waiting', 'notified') " +
            "ORDER BY rank_no")
    List<BookingWaitlist> selectBySlot(@Param("roomId") Long roomId,
                                       @Param("bookingDate") LocalDate bookingDate,
                                       @Param("startTime") LocalTime startTime,
                                       @Param("endTime") LocalTime endTime);

    /**
     * 查询用户的候补记录
     */
    @Select("SELECT * FROM booking_waitlist WHERE user_id = #{userId} AND status IN ('waiting', 'notified')")
    List<BookingWaitlist> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询指定时段的候补人数
     */
    @Select("SELECT COUNT(*) FROM booking_waitlist " +
            "WHERE room_id = #{roomId} " +
            "AND booking_date = #{bookingDate} " +
            "AND start_time = #{startTime} " +
            "AND end_time = #{endTime} " +
            "AND status IN ('waiting', 'notified')")
    int countBySlot(@Param("roomId") Long roomId,
                    @Param("bookingDate") LocalDate bookingDate,
                    @Param("startTime") LocalTime startTime,
                    @Param("endTime") LocalTime endTime);

    /**
     * 查询需要过期的候补记录（定时任务用）：
     * 1) notified 且确认时限已过
     * 2) waiting / notified 且会议时段已结束
     */
    @Select("SELECT * FROM booking_waitlist " +
            "WHERE status IN ('waiting', 'notified') " +
            "AND ((status = 'notified' AND confirm_deadline IS NOT NULL AND confirm_deadline < #{now}) " +
            "OR (booking_date < DATE(#{now}) " +
            "OR (booking_date = DATE(#{now}) AND end_time < TIME(#{now}))))")
    List<BookingWaitlist> selectExpiredWaitlists(@Param("now") LocalDateTime now);

    /**
     * 查询指定时段的下一个待通知候补（超时顺延用）
     */
    @Select("SELECT * FROM booking_waitlist " +
            "WHERE room_id = #{roomId} " +
            "AND booking_date = #{bookingDate} " +
            "AND start_time = #{startTime} " +
            "AND end_time = #{endTime} " +
            "AND status = 'waiting' " +
            "ORDER BY rank_no LIMIT 1")
    BookingWaitlist selectNextWaiting(@Param("roomId") Long roomId,
                                      @Param("bookingDate") LocalDate bookingDate,
                                      @Param("startTime") LocalTime startTime,
                                      @Param("endTime") LocalTime endTime);
}
