package com.example.meeting.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.meeting.entity.MeetingMinutes;
import com.example.meeting.mapper.MeetingMinutesMapper;
import com.example.meeting.service.MeetingMinutesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 会议纪要服务实现类
 */
@Service
public class MeetingMinutesServiceImpl extends ServiceImpl<MeetingMinutesMapper, MeetingMinutes> implements MeetingMinutesService {

    @Autowired
    private MeetingMinutesMapper meetingMinutesMapper;

    @Override
    public MeetingMinutes getMinutesByBookingId(Long bookingId) {
        return meetingMinutesMapper.selectByBookingId(bookingId);
    }

    @Override
    public MeetingMinutes submitMinutes(Long userId, Long bookingId, MeetingMinutes minutes) {
        // 检查是否已有纪要
        MeetingMinutes existing = meetingMinutesMapper.selectByBookingId(bookingId);
        if (existing != null) {
            throw new RuntimeException("该预约已有会议纪要");
        }

        minutes.setBookingId(bookingId);
        minutes.setAuthorId(userId);
        minutes.setSubmitTime(LocalDateTime.now());
        save(minutes);
        return minutes;
    }

    @Override
    public void updateMinutes(Long userId, Long minutesId, MeetingMinutes minutes) {
        lambdaUpdate()
                .eq(MeetingMinutes::getId, minutesId)
                .eq(MeetingMinutes::getAuthorId, userId)
                .set(MeetingMinutes::getAgenda, minutes.getAgenda())
                .set(MeetingMinutes::getContent, minutes.getContent())
                .set(MeetingMinutes::getAttachmentList, minutes.getAttachmentList())
                .update();
    }

    @Override
    public void deleteMinutes(Long userId, Long minutesId) {
        lambdaUpdate()
                .eq(MeetingMinutes::getId, minutesId)
                .eq(MeetingMinutes::getAuthorId, userId)
                .remove();
    }
}
