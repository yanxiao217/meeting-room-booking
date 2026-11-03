package com.example.meeting.service;

import com.example.meeting.config.MailConfig;
import com.example.meeting.entity.Booking;
import com.example.meeting.entity.MeetingRoom;
import com.example.meeting.entity.User;
import com.example.meeting.mapper.MeetingRoomMapper;
import com.example.meeting.mapper.UserMapper;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * 邮件服务 - 发送各类通知邮件
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final MailConfig mailConfig;
    private final UserMapper userMapper;
    private final MeetingRoomMapper meetingRoomMapper;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 发送预约成功邮件
     */
    @Async
    public void sendBookingSuccessEmail(Booking booking) {
        if (!canSend()) return;
        User user = userMapper.selectById(booking.getUserId());
        if (user == null || user.getEmail() == null || user.getEmail().isEmpty()) return;

        MeetingRoom room = meetingRoomMapper.selectById(booking.getRoomId());
        String roomName = room != null ? room.getRoomName() : "未知会议室";
        String date = booking.getBookingDate().format(DATE_FMT);
        String time = booking.getStartTime() + "-" + booking.getEndTime();

        String subject = "预约成功 - " + roomName;
        String html = buildHtml("预约成功",
                "<p>您已成功预约 <b>" + roomName + "</b></p>" +
                "<p>日期：" + date + "</p>" +
                "<p>时间：" + time + "</p>" +
                "<p>会议主题：" + booking.getTopic() + "</p>" +
                "<p style='color:#888;font-size:12px'>如需取消请前往系统操作</p>");

        sendMail(user.getEmail(), subject, html);
    }

    /**
     * 发送预约取消邮件
     */
    @Async
    public void sendCancelEmail(Booking booking) {
        if (!canSend()) return;
        User user = userMapper.selectById(booking.getUserId());
        if (user == null || user.getEmail() == null || user.getEmail().isEmpty()) return;

        MeetingRoom room = meetingRoomMapper.selectById(booking.getRoomId());
        String roomName = room != null ? room.getRoomName() : "未知会议室";
        String date = booking.getBookingDate().format(DATE_FMT);
        String time = booking.getStartTime() + "-" + booking.getEndTime();

        String subject = "预约取消 - " + roomName;
        String html = buildHtml("预约取消",
                "<p>您预约的 <b>" + roomName + "</b> 已取消</p>" +
                "<p>日期：" + date + "</p>" +
                "<p>时间：" + time + "</p>");

        sendMail(user.getEmail(), subject, html);
    }

    /**
     * 发送预约待审批邮件
     */
    @Async
    public void sendBookingPendingEmail(Booking booking) {
        if (!canSend()) return;
        User user = userMapper.selectById(booking.getUserId());
        if (user == null || user.getEmail() == null || user.getEmail().isEmpty()) return;

        MeetingRoom room = meetingRoomMapper.selectById(booking.getRoomId());
        String roomName = room != null ? room.getRoomName() : "未知会议室";
        String date = booking.getBookingDate().format(DATE_FMT);
        String time = booking.getStartTime() + "-" + booking.getEndTime();

        String subject = "预约待审批 - " + roomName;
        String html = buildHtml("预约待审批",
                "<p>您预约的 <b>" + roomName + "</b> 已提交，等待管理员审批</p>" +
                "<p>日期：" + date + "</p>" +
                "<p>时间：" + time + "</p>" +
                "<p style='color:#888;font-size:12px'>审批结果将通过邮件通知您</p>");

        sendMail(user.getEmail(), subject, html);
    }

    /**
     * 发送审批通过邮件
     */
    @Async
    public void sendBookingApprovedEmail(Booking booking) {
        if (!canSend()) return;
        User user = userMapper.selectById(booking.getUserId());
        if (user == null || user.getEmail() == null || user.getEmail().isEmpty()) return;

        MeetingRoom room = meetingRoomMapper.selectById(booking.getRoomId());
        String roomName = room != null ? room.getRoomName() : "未知会议室";
        String date = booking.getBookingDate().format(DATE_FMT);
        String time = booking.getStartTime() + "-" + booking.getEndTime();

        String subject = "审批通过 - " + roomName;
        String html = buildHtml("审批通过",
                "<p>您预约的 <b>" + roomName + "</b> 已通过审批</p>" +
                "<p>日期：" + date + "</p>" +
                "<p>时间：" + time + "</p>" +
                "<p style='color:#22c55e;font-size:13px'>请按时到达会议室</p>");

        sendMail(user.getEmail(), subject, html);
    }

    /**
     * 发送审批拒绝邮件
     */
    @Async
    public void sendBookingRejectedEmail(Booking booking, String reason) {
        if (!canSend()) return;
        User user = userMapper.selectById(booking.getUserId());
        if (user == null || user.getEmail() == null || user.getEmail().isEmpty()) return;

        MeetingRoom room = meetingRoomMapper.selectById(booking.getRoomId());
        String roomName = room != null ? room.getRoomName() : "未知会议室";
        String date = booking.getBookingDate().format(DATE_FMT);
        String time = booking.getStartTime() + "-" + booking.getEndTime();

        String subject = "审批拒绝 - " + roomName;
        String html = buildHtml("审批拒绝",
                "<p>您预约的 <b>" + roomName + "</b> 未通过审批</p>" +
                "<p>日期：" + date + "</p>" +
                "<p>时间：" + time + "</p>" +
                (reason != null && !reason.isEmpty() ? "<p>原因：" + reason + "</p>" : "") +
                "<p style='color:#888;font-size:12px'>如有疑问请联系管理员</p>");

        sendMail(user.getEmail(), subject, html);
    }

    /**
     * 发送会议开始提醒邮件
     */
    @Async
    public void sendMeetingReminderEmail(Booking booking) {
        if (!canSend()) return;
        User user = userMapper.selectById(booking.getUserId());
        if (user == null || user.getEmail() == null || user.getEmail().isEmpty()) return;

        MeetingRoom room = meetingRoomMapper.selectById(booking.getRoomId());
        String roomName = room != null ? room.getRoomName() : "未知会议室";
        String time = booking.getStartTime() + "-" + booking.getEndTime();

        String subject = "会议提醒 - " + roomName;
        String html = buildHtml("会议即将开始",
                "<p>您预约的 <b>" + roomName + "</b> 即将开始</p>" +
                "<p>时间：" + time + "</p>" +
                "<p style='color:#f59e0b;font-size:13px'>请尽快前往会议室</p>");

        sendMail(user.getEmail(), subject, html);
    }

    /**
     * 发送测试邮件
     */
    public boolean sendTestEmail(String toEmail) {
        if (!canSend()) return false;
        try {
            String subject = "邮件配置测试 - 会议室预约系统";
            String html = buildHtml("邮件配置测试",
                    "<p>如果您收到此邮件，说明邮件配置正确</p>" +
                    "<p>系统将在以下场景自动发送邮件通知：</p>" +
                    "<ul>" +
                    "<li>预约成功 / 取消</li>" +
                    "<li>审批通过 / 拒绝</li>" +
                    "<li>会议开始提醒</li>" +
                    "</ul>");
            sendMail(toEmail, subject, html);
            return true;
        } catch (Exception e) {
            log.error("[EmailService] Test email failed: {}", e.getMessage());
            return false;
        }
    }

    // ========== 私有方法 ==========

    private boolean canSend() {
        if (!mailConfig.isConfigured()) {
            log.debug("[EmailService] SMTP not configured, skip sending");
            return false;
        }
        return true;
    }

    private void sendMail(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailConfig.getFromAddress(), mailConfig.getFromName());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("[EmailService] Email sent to {}: {}", to, subject);
        } catch (Exception e) {
            log.error("[EmailService] Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private String buildHtml(String title, String content) {
        return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'>" +
                "<style>" +
                "body{font-family:'Microsoft YaHei','PingFang SC',sans-serif;background:#f5f5f5;margin:0;padding:20px;}" +
                ".container{max-width:560px;margin:0 auto;background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,.08);}" +
                ".header{background:#161A17;color:#F3E24A;padding:18px 24px;font-size:16px;font-weight:600;}" +
                ".body{padding:24px;line-height:1.7;color:#333;font-size:14px;}" +
                ".footer{padding:12px 24px;background:#fafafa;border-top:1px solid #eee;font-size:11px;color:#999;text-align:center;}" +
                "</style></head><body>" +
                "<div class='container'>" +
                "<div class='header'>🏢 " + title + "</div>" +
                "<div class='body'>" + content + "</div>" +
                "<div class='footer'>会议室预约管理系统 · 自动通知邮件</div>" +
                "</div></body></html>";
    }
}
