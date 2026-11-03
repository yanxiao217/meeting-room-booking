package com.example.meeting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.meeting.entity.Booking;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 预约Mapper接口
 */
@Mapper
public interface BookingMapper extends BaseMapper<Booking> {

    /**
     * 查询指定日期范围的预约（不分页）
     */
    @Select("SELECT b.* FROM booking b " +
            "WHERE b.booking_date BETWEEN #{startDate} AND #{endDate} " +
            "AND b.status IN ('confirmed', 'signed', 'pending') " +
            "ORDER BY b.room_id, b.start_time")
    List<Booking> selectBookingsByDateRange(@Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    /**
     * 查询指定日期范围、会议室的预约（不分页）
     */
    @Select("SELECT b.* FROM booking b " +
            "WHERE b.room_id = #{roomId} " +
            "AND b.booking_date BETWEEN #{startDate} AND #{endDate} " +
            "AND b.status IN ('confirmed', 'signed', 'pending') " +
            "ORDER BY b.start_time")
    List<Booking> selectBookingsByRoomAndDate(@Param("roomId") Long roomId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    /**
     * 查询冲突预约
     */
    @Select("SELECT COUNT(*) FROM booking " +
            "WHERE room_id = #{roomId} " +
            "AND booking_date = #{bookingDate} " +
            "AND status IN ('confirmed', 'signed', 'pending') " +
            "AND id != #{excludeId} " +
            "AND start_time < #{endTime} " +
            "AND end_time > #{startTime}")
    int countConflictBookings(@Param("roomId") Long roomId,
                               @Param("bookingDate") LocalDate bookingDate,
                               @Param("startTime") java.time.LocalTime startTime,
                               @Param("endTime") java.time.LocalTime endTime,
                               @Param("excludeId") Long excludeId);

    /**
     * 统计部门自某日起的有效预约次数（配额用，weekly）
     */
    @Select("SELECT COUNT(*) FROM booking b " +
            "INNER JOIN sys_user u ON b.user_id = u.id " +
            "WHERE u.dept_id = #{deptId} " +
            "AND b.status IN ('pending', 'confirmed', 'signed') " +
            "AND b.booking_date >= #{startDate}")
    long countValidByDeptSince(@Param("deptId") Long deptId,
                               @Param("startDate") LocalDate startDate);

    /**
     * 统计部门自某日起的有效预约总时长（分钟，配额用，monthly）
     */
    @Select("SELECT COALESCE(SUM(b.duration_min), 0) FROM booking b " +
            "INNER JOIN sys_user u ON b.user_id = u.id " +
            "WHERE u.dept_id = #{deptId} " +
            "AND b.status IN ('pending', 'confirmed', 'signed') " +
            "AND b.booking_date >= #{startDate}")
    long sumDurationByDeptSince(@Param("deptId") Long deptId,
                                @Param("startDate") LocalDate startDate);

    /**
     * 查询用户今日预约数
     */
    @Select("SELECT COUNT(*) FROM booking " +
            "WHERE user_id = #{userId} " +
            "AND booking_date = #{bookingDate} " +
            "AND status IN ('confirmed', 'signed', 'pending')")
    int countUserDailyBookings(@Param("userId") Long userId,
                                @Param("bookingDate") LocalDate bookingDate);

    /**
     * 查询会议室今日预约数
     */
    @Select("SELECT COUNT(*) FROM booking " +
            "WHERE room_id = #{roomId} " +
            "AND booking_date = #{bookingDate} " +
            "AND status IN ('confirmed', 'signed', 'pending')")
    int countRoomDailyBookings(@Param("roomId") Long roomId,
                                @Param("bookingDate") LocalDate bookingDate);

    /**
     * 查询会议室今日预约数
     */
    @Select("SELECT COUNT(*) FROM booking " +
            "WHERE room_id = #{roomId} " +
            "AND booking_date = CURDATE() " +
            "AND status IN ('confirmed', 'signed')")
    int countRoomTodayBookings(@Param("roomId") Long roomId);

    /**
     * 统计各会议室使用率
     */
    @Select("SELECT mr.id AS roomId, mr.room_name AS roomName, " +
            "COUNT(b.id) AS bookingCount, " +
            "COALESCE(SUM(b.duration_min), 0) AS totalDuration, " +
            "COALESCE(AVG(b.duration_min), 0) AS avgDuration, " +
            "ROUND(COALESCE(SUM(b.duration_min), 0) / (DATEDIFF(#{endDate}, #{startDate}) + 1) / 840.0 * 100, 1) AS usageRate " +
            "FROM meeting_room mr " +
            "LEFT JOIN booking b ON mr.id = b.room_id " +
            "AND b.booking_date BETWEEN #{startDate} AND #{endDate} " +
            "AND b.status IN ('confirmed', 'signed', 'completed') " +
            "GROUP BY mr.id, mr.room_name " +
            "ORDER BY bookingCount DESC")
    List<Map<String, Object>> selectUsageStats(@Param("startDate") String startDate,
                                               @Param("endDate") String endDate);

    /**
     * 统计日期范围内的预约数（不含已取消/已过期）
     */
    @Select("SELECT COUNT(*) FROM booking " +
            "WHERE booking_date BETWEEN #{startDate} AND #{endDate} " +
            "AND status IN ('confirmed', 'signed', 'completed', 'pending')")
    long countBookingsByDateRange(@Param("startDate") String startDate,
                                  @Param("endDate") String endDate);

    /**
     * 统计日期范围内的活跃用户数（去重）
     */
    @Select("SELECT COUNT(DISTINCT user_id) FROM booking " +
            "WHERE booking_date BETWEEN #{startDate} AND #{endDate} " +
            "AND status IN ('confirmed', 'signed', 'completed', 'pending')")
    int countActiveUsersByDateRange(@Param("startDate") String startDate,
                                    @Param("endDate") String endDate);

    /**
     * 热门会议室排行
     */
    @Select("SELECT mr.id AS roomId, mr.room_name AS roomName, COUNT(b.id) AS bookingCount " +
            "FROM meeting_room mr " +
            "INNER JOIN booking b ON mr.id = b.room_id " +
            "AND b.booking_date BETWEEN #{startDate} AND #{endDate} " +
            "AND b.status IN ('confirmed', 'signed', 'completed') " +
            "GROUP BY mr.id, mr.room_name " +
            "ORDER BY bookingCount DESC LIMIT 5")
    List<Map<String, Object>> selectHotRooms(@Param("startDate") String startDate,
                                             @Param("endDate") String endDate);

    /**
     * 预约趋势统计（按天）
     */
    @Select("SELECT DATE_FORMAT(booking_date, '%m/%d') AS `dates`, COUNT(*) AS `cnt` " +
            "FROM booking " +
            "WHERE booking_date BETWEEN #{startDate} AND #{endDate} " +
            "AND status IN ('confirmed', 'signed', 'completed') " +
            "GROUP BY booking_date " +
            "ORDER BY booking_date")
    List<Map<String, Object>> selectBookingTrend(@Param("startDate") String startDate,
                                                 @Param("endDate") String endDate);

    /**
     * 查询过期未签到的预约（已确认、会议开始已超过deadline、未签到）
     */
    @Select("SELECT * FROM booking " +
            "WHERE status = 'confirmed' " +
            "AND (booking_date < DATE(#{deadline}) " +
            "OR (booking_date = DATE(#{deadline}) AND start_time < TIME(#{deadline})))")
    List<Booking> selectExpiredBookings(@Param("deadline") LocalDateTime deadline);

    /**
     * 查询已签到且会议已结束的预约（用于自动完成）
     */
    @Select("SELECT * FROM booking " +
            "WHERE status = 'signed' " +
            "AND (booking_date < DATE(#{now}) " +
            "OR (booking_date = DATE(#{now}) AND end_time <= TIME(#{now})))")
    List<Booking> selectFinishedBookings(@Param("now") LocalDateTime now);

    /**
     * 查询已过期的待审批预约（pending 且会议开始已超过 deadline，用于自动过期）
     */
    @Select("SELECT * FROM booking " +
            "WHERE status = 'pending' " +
            "AND (booking_date < DATE(#{deadline}) " +
            "OR (booking_date = DATE(#{deadline}) AND start_time < TIME(#{deadline})))")
    List<Booking> selectPendingExpiredBookings(@Param("deadline") LocalDateTime deadline);

    /**
     * 查询即将开始的预约（会议开始前N分钟内，用于发送提醒通知）
     */
    @Select("SELECT * FROM booking " +
            "WHERE status = 'confirmed' " +
            "AND booking_date = DATE(#{date}) " +
            "AND start_time > TIME(#{date}) " +
            "AND start_time <= TIME(#{deadline})")
    List<Booking> selectUpcomingBookings(@Param("date") LocalDateTime date,
                                         @Param("deadline") LocalDateTime deadline);
}
