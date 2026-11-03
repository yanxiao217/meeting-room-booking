package com.example.meeting.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.meeting.entity.RecurringBooking;
import com.example.meeting.mapper.RecurringBookingMapper;
import com.example.meeting.service.RecurringBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 周期预约组服务实现类
 */
@Service
public class RecurringBookingServiceImpl extends ServiceImpl<RecurringBookingMapper, RecurringBooking> implements RecurringBookingService {

    @Autowired
    private RecurringBookingMapper recurringBookingMapper;

    @Override
    public RecurringBooking createRecurring(Long userId, RecurringBooking recurringBooking) {
        recurringBooking.setUserId(userId);
        recurringBooking.setEnabled(1);
        save(recurringBooking);
        return recurringBooking;
    }

    @Override
    public List<RecurringBooking> getMyRecurrings(Long userId) {
        return recurringBookingMapper.selectByUserId(userId);
    }

    @Override
    public void terminateRecurring(Long userId, Long recurringId) {
        lambdaUpdate()
                .eq(RecurringBooking::getId, recurringId)
                .eq(RecurringBooking::getUserId, userId)
                .set(RecurringBooking::getEnabled, 0)
                .update();
    }
}
