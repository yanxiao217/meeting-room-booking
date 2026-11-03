package com.example.meeting.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.meeting.entity.BookingParticipant;
import com.example.meeting.mapper.BookingParticipantMapper;
import com.example.meeting.service.BookingParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 参会人服务实现类
 */
@Service
public class BookingParticipantServiceImpl extends ServiceImpl<BookingParticipantMapper, BookingParticipant> implements BookingParticipantService {

    @Autowired
    private BookingParticipantMapper bookingParticipantMapper;

    @Override
    public List<BookingParticipant> getParticipantsByBookingId(Long bookingId) {
        return bookingParticipantMapper.selectByBookingId(bookingId);
    }

    @Override
    public void addParticipant(Long bookingId, Long userId) {
        if (!isParticipant(bookingId, userId)) {
            BookingParticipant participant = new BookingParticipant();
            participant.setBookingId(bookingId);
            participant.setUserId(userId);
            participant.setStatus("invited");
            save(participant);
        }
    }

    @Override
    public void removeParticipant(Long bookingId, Long userId) {
        lambdaUpdate()
                .eq(BookingParticipant::getBookingId, bookingId)
                .eq(BookingParticipant::getUserId, userId)
                .remove();
    }

    @Override
    public void updateParticipantStatus(Long bookingId, Long userId, String status) {
        lambdaUpdate()
                .eq(BookingParticipant::getBookingId, bookingId)
                .eq(BookingParticipant::getUserId, userId)
                .set(BookingParticipant::getStatus, status)
                .update();
    }

    @Override
    public boolean isParticipant(Long bookingId, Long userId) {
        return bookingParticipantMapper.countByBookingAndUser(bookingId, userId) > 0;
    }
}
