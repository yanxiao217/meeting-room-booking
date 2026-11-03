package com.example.meeting.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.meeting.entity.MeetingMinutes;

/**
 * 会议纪要服务接口
 */
public interface MeetingMinutesService extends IService<MeetingMinutes> {

    /**
     * 获取预约的纪要
     */
    MeetingMinutes getMinutesByBookingId(Long bookingId);

    /**
     * 提交会议纪要
     */
    MeetingMinutes submitMinutes(Long userId, Long bookingId, MeetingMinutes minutes);

    /**
     * 更新会议纪要
     */
    void updateMinutes(Long userId, Long minutesId, MeetingMinutes minutes);

    /**
     * 删除会议纪要
     */
    void deleteMinutes(Long userId, Long minutesId);
}
