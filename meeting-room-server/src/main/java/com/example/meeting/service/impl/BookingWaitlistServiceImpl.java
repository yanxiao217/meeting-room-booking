package com.example.meeting.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.meeting.entity.BookingWaitlist;
import com.example.meeting.mapper.BookingWaitlistMapper;
import com.example.meeting.service.BookingWaitlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 候补队列服务实现类
 */
@Service
public class BookingWaitlistServiceImpl extends ServiceImpl<BookingWaitlistMapper, BookingWaitlist> implements BookingWaitlistService {

    @Autowired
    private BookingWaitlistMapper bookingWaitlistMapper;

    @Override
    public void joinWaitlist(Long userId, Long roomId, LocalDate bookingDate, LocalTime startTime, LocalTime endTime) {
        // 检查是否已在候补队列
        List<BookingWaitlist> existing = bookingWaitlistMapper.selectBySlot(roomId, bookingDate, startTime, endTime);
        for (BookingWaitlist w : existing) {
            if (w.getUserId().equals(userId)) {
                throw new RuntimeException("您已在候补队列中");
            }
        }

        // 获取当前最大排序号
        int maxRank = existing.stream()
                .mapToInt(BookingWaitlist::getRankNo)
                .max()
                .orElse(0);

        // 创建候补记录
        BookingWaitlist waitlist = new BookingWaitlist();
        waitlist.setRoomId(roomId);
        waitlist.setBookingDate(bookingDate);
        waitlist.setStartTime(startTime);
        waitlist.setEndTime(endTime);
        waitlist.setUserId(userId);
        waitlist.setRankNo(maxRank + 1);
        waitlist.setStatus("waiting");
        save(waitlist);
    }

    @Override
    public List<BookingWaitlist> getMyWaitlist(Long userId) {
        return bookingWaitlistMapper.selectByUserId(userId);
    }

    @Override
    public void leaveWaitlist(Long userId, Long waitlistId) {
        lambdaUpdate()
                .eq(BookingWaitlist::getId, waitlistId)
                .eq(BookingWaitlist::getUserId, userId)
                .set(BookingWaitlist::getStatus, "cancelled")
                .update();
    }

    @Override
    public void confirmWaitlist(Long userId, Long waitlistId) {
        BookingWaitlist waitlist = getById(waitlistId);
        if (waitlist == null || !waitlist.getUserId().equals(userId)) {
            throw new RuntimeException("候补记录不存在");
        }
        if (!"notified".equals(waitlist.getStatus())) {
            throw new RuntimeException("候补状态异常");
        }
        if (LocalDateTime.now().isAfter(waitlist.getConfirmDeadline())) {
            throw new RuntimeException("候补确认已超时");
        }
        waitlist.setStatus("confirmed");
        updateById(waitlist);
    }

    @Override
    public void notifyWaitlist(Long roomId, LocalDate bookingDate, LocalTime startTime, LocalTime endTime) {
        List<BookingWaitlist> waitlist = bookingWaitlistMapper.selectBySlot(roomId, bookingDate, startTime, endTime);
        if (!waitlist.isEmpty()) {
            BookingWaitlist first = waitlist.get(0);
            first.setStatus("notified");
            first.setNotifyTime(LocalDateTime.now());
            first.setConfirmDeadline(LocalDateTime.now().plusMinutes(10)); // 默认10分钟确认时限
            updateById(first);
            // TODO: 发送通知给候补人
        }
    }

    /**
     * 自动处理过期候补（由定时任务每分钟调用）：
     * 1) notified 且确认时限已过 → 置为 expired，并顺延通知下一位等待者
     * 2) waiting / notified 且会议时段已结束 → 置为 expired
     */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void autoExpireWaitlists() {
        LocalDateTime now = LocalDateTime.now();
        List<BookingWaitlist> expired = bookingWaitlistMapper.selectExpiredWaitlists(now);
        for (BookingWaitlist w : expired) {
            boolean deadlinePassed = "notified".equals(w.getStatus())
                    && w.getConfirmDeadline() != null
                    && now.isAfter(w.getConfirmDeadline());

            w.setStatus("expired");
            updateById(w);

            // 超时未确认：顺延通知同时段的下一位候补（时段已结束的不再顺延）
            if (deadlinePassed) {
                BookingWaitlist next = bookingWaitlistMapper.selectNextWaiting(
                        w.getRoomId(), w.getBookingDate(), w.getStartTime(), w.getEndTime());
                if (next != null) {
                    next.setStatus("notified");
                    next.setNotifyTime(LocalDateTime.now());
                    next.setConfirmDeadline(LocalDateTime.now().plusMinutes(10));
                    updateById(next);
                }
            }
        }
    }
}
