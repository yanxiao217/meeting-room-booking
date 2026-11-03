package com.example.meeting.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.meeting.entity.BookingWaitlist;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 候补队列服务接口
 */
public interface BookingWaitlistService extends IService<BookingWaitlist> {

    /**
     * 加入候补队列
     */
    void joinWaitlist(Long userId, Long roomId, LocalDate bookingDate, LocalTime startTime, LocalTime endTime);

    /**
     * 获取用户的候补列表
     */
    List<BookingWaitlist> getMyWaitlist(Long userId);

    /**
     * 退出候补
     */
    void leaveWaitlist(Long userId, Long waitlistId);

    /**
     * 候补成功确认
     */
    void confirmWaitlist(Long userId, Long waitlistId);

    /**
     * 通知候补人
     */
    void notifyWaitlist(Long roomId, LocalDate bookingDate, LocalTime startTime, LocalTime endTime);
}
