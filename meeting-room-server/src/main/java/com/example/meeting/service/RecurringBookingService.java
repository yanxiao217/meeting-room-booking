package com.example.meeting.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.meeting.entity.RecurringBooking;

import java.util.List;

/**
 * 周期预约组服务接口
 */
public interface RecurringBookingService extends IService<RecurringBooking> {

    /**
     * 创建周期预约组
     */
    RecurringBooking createRecurring(Long userId, RecurringBooking recurringBooking);

    /**
     * 获取用户的有效周期预约
     */
    List<RecurringBooking> getMyRecurrings(Long userId);

    /**
     * 终止周期预约
     */
    void terminateRecurring(Long userId, Long recurringId);
}
