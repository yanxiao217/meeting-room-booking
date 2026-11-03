package com.example.meeting.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.meeting.common.result.PageResult;
import com.example.meeting.dto.BookingCancelDTO;
import com.example.meeting.dto.BookingCreateDTO;
import com.example.meeting.dto.BookingQueryDTO;
import com.example.meeting.dto.BookingUpdateDTO;
import com.example.meeting.entity.Booking;
import com.example.meeting.vo.AvailabilityVO;
import com.example.meeting.vo.BookingVO;
import com.example.meeting.vo.CalendarVO;
import com.example.meeting.vo.TimeSlotVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 预约服务接口
 */
public interface BookingService extends IService<Booking> {

    /**
     * 创建预约
     */
    BookingVO createBooking(Long userId, BookingCreateDTO dto);

    /**
     * 修改预约
     */
    BookingVO updateBooking(Long userId, Long bookingId, BookingUpdateDTO dto);

    /**
     * 取消预约
     */
    void cancelBooking(Long userId, Long bookingId, BookingCancelDTO dto);

    /**
     * 获取我的预约列表
     */
    PageResult<BookingVO> getMyBookings(Long userId, String status, Integer pageNum, Integer pageSize);

    /**
     * 获取预约详情
     */
    BookingVO getBookingDetail(Long userId, Long bookingId);

    /**
     * 获取日历视图数据
     */
    CalendarVO getCalendarData(LocalDate startDate, LocalDate endDate, String floor, Integer capacity);

    /**
     * 获取会议室可用时段
     */
    AvailabilityVO getAvailability(Long roomId, LocalDate date);

    /**
     * 获取相邻可用时段（冲突时推荐）
     */
    List<TimeSlotVO> getSuggestedSlots(Long roomId, LocalDate date,
                                       java.time.LocalTime startTime, java.time.LocalTime endTime);

    /**
     * 管理员：获取全部预约列表
     */
    PageResult<BookingVO> getAllBookings(BookingQueryDTO dto);

    /**
     * 管理员：强制取消预约
     */
    void forceCancelBooking(Long adminId, Long bookingId, String reason);

    /**
     * 管理员：审批通过
     */
    BookingVO approveBooking(Long adminId, Long bookingId);

    /**
     * 管理员：审批拒绝
     */
    void rejectBooking(Long adminId, Long bookingId, String reason);

    /**
     * 管理员：代客预约
     */
    BookingVO createBookingByAdmin(Long adminId, com.example.meeting.dto.AdminBookingDTO dto);

    /**
     * 签到：确认到达会议室
     * @param signSource 签到来源：manual手动 qrcode二维码
     */
    void signBooking(Long userId, Long bookingId, String signSource);

    /**
     * 会议开始前发送提醒通知（定时任务调用）
     */
    void sendMeetingStartReminders();
}
