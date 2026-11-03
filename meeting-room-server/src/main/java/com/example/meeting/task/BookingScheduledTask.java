package com.example.meeting.task;

import com.example.meeting.service.impl.BookingServiceImpl;
import com.example.meeting.service.impl.BookingWaitlistServiceImpl;
import com.example.meeting.service.impl.CreditServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务：自动释放过期预约 + 完成已结束预约 + 过期候补清理 + 每周信用分恢复
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingScheduledTask {

    private final BookingServiceImpl bookingService;
    private final BookingWaitlistServiceImpl waitlistService;
    private final CreditServiceImpl creditService;

    /**
     * 每分钟检查并释放过期预约（爽约扣分）
     */
    @Scheduled(fixedRate = 60000)
    public void autoReleaseExpired() {
        try {
            bookingService.autoReleaseExpiredBookings();
        } catch (Exception e) {
            log.error("自动释放过期预约失败", e);
        }
    }

    /**
     * 每分钟检查并完成已结束的预约（完成加分）
     */
    @Scheduled(fixedRate = 60000)
    public void completeFinished() {
        try {
            bookingService.completeFinishedBookings();
        } catch (Exception e) {
            log.error("自动完成预约失败", e);
        }
    }

    /**
     * 每分钟过期待审批的预约（pending 超时自动释放）
     */
    @Scheduled(fixedRate = 60000)
    public void expirePendingBookings() {
        try {
            bookingService.autoExpirePendingBookings();
        } catch (Exception e) {
            log.error("自动过期待审批预约失败", e);
        }
    }

    /**
     * 每分钟清理过期候补（确认超时 / 时段已过），超时未确认的顺延通知下一位
     */
    @Scheduled(fixedRate = 60000)
    public void expireWaitlists() {
        try {
            waitlistService.autoExpireWaitlists();
        } catch (Exception e) {
            log.error("自动清理过期候补失败", e);
        }
    }

    /**
     * 每5分钟扫描即将开始的预约，发送提醒通知
     */
    @Scheduled(fixedRate = 300000)
    public void meetingStartReminder() {
        try {
            bookingService.sendMeetingStartReminders();
        } catch (Exception e) {
            log.error("会议开始提醒发送失败", e);
        }
    }

    /**
     * 每周一 00:00 恢复信用分
     */
    @Scheduled(cron = "0 0 0 ? * MON")
    public void weeklyCreditRecover() {
        try {
            creditService.weeklyRecover();
            log.info("每周信用分恢复任务执行完成");
        } catch (Exception e) {
            log.error("每周信用分恢复失败", e);
        }
    }
}
