package com.example.meeting.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.meeting.entity.Booking;
import com.example.meeting.entity.MeetingRoom;
import com.example.meeting.entity.Notification;
import com.example.meeting.entity.User;
import com.example.meeting.mapper.MeetingRoomMapper;
import com.example.meeting.mapper.NotificationMapper;
import com.example.meeting.mapper.UserMapper;
import com.example.meeting.service.EmailService;
import com.example.meeting.service.NotificationService;
import com.example.meeting.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知服务实现
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    private final UserMapper userMapper;
    private final MeetingRoomMapper meetingRoomMapper;
    private final EmailService emailService;

    @Override
    public void sendBookingSuccessNotification(Booking booking) {
        User user = userMapper.selectById(booking.getUserId());
        MeetingRoom room = meetingRoomMapper.selectById(booking.getRoomId());

        String roomName = room != null ? room.getRoomName() : "未知会议室";
        String dateStr = booking.getBookingDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String timeStr = booking.getStartTime() + "-" + booking.getEndTime();

        Notification notification = new Notification();
        notification.setUserId(booking.getUserId());
        notification.setType("success");
        notification.setTitle("预约成功");
        notification.setContent("您已成功预约" + roomName + " " + dateStr + " " + timeStr);
        notification.setBizType("booking");
        notification.setBizId(booking.getId());
        notification.setIsRead(0);

        save(notification);
        emailService.sendBookingSuccessEmail(booking);
    }

    @Override
    public void sendCancelNotification(Booking booking) {
        User user = userMapper.selectById(booking.getUserId());
        MeetingRoom room = meetingRoomMapper.selectById(booking.getRoomId());

        String roomName = room != null ? room.getRoomName() : "未知会议室";
        String dateStr = booking.getBookingDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String timeStr = booking.getStartTime() + "-" + booking.getEndTime();

        Notification notification = new Notification();
        notification.setUserId(booking.getUserId());
        notification.setType("cancel");
        notification.setTitle("预约取消");
        notification.setContent("您预约的" + roomName + " " + dateStr + " " + timeStr + " 已被取消");
        notification.setBizType("booking");
        notification.setBizId(booking.getId());
        notification.setIsRead(0);

        save(notification);
        emailService.sendCancelEmail(booking);
    }

    @Override
    public void sendBookingPendingNotification(Booking booking) {
        MeetingRoom room = meetingRoomMapper.selectById(booking.getRoomId());
        String roomName = room != null ? room.getRoomName() : "未知会议室";
        String dateStr = booking.getBookingDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String timeStr = booking.getStartTime() + "-" + booking.getEndTime();

        Notification notification = new Notification();
        notification.setUserId(booking.getUserId());
        notification.setType("pending");
        notification.setTitle("预约待审批");
        notification.setContent("您的预约（" + roomName + " " + dateStr + " " + timeStr + "）已提交，等待管理员审批");
        notification.setBizType("booking");
        notification.setBizId(booking.getId());
        notification.setIsRead(0);

        save(notification);
        emailService.sendBookingPendingEmail(booking);
    }

    @Override
    public void sendBookingApprovedNotification(Booking booking) {
        MeetingRoom room = meetingRoomMapper.selectById(booking.getRoomId());
        String roomName = room != null ? room.getRoomName() : "未知会议室";
        String dateStr = booking.getBookingDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String timeStr = booking.getStartTime() + "-" + booking.getEndTime();

        Notification notification = new Notification();
        notification.setUserId(booking.getUserId());
        notification.setType("approve");
        notification.setTitle("审批通过");
        notification.setContent("您预约的" + roomName + " " + dateStr + " " + timeStr + " 已通过审批");
        notification.setBizType("booking");
        notification.setBizId(booking.getId());
        notification.setIsRead(0);

        save(notification);
        emailService.sendBookingApprovedEmail(booking);
    }

    @Override
    public void sendBookingRejectedNotification(Booking booking, String reason) {
        MeetingRoom room = meetingRoomMapper.selectById(booking.getRoomId());
        String roomName = room != null ? room.getRoomName() : "未知会议室";
        String dateStr = booking.getBookingDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String timeStr = booking.getStartTime() + "-" + booking.getEndTime();

        Notification notification = new Notification();
        notification.setUserId(booking.getUserId());
        notification.setType("reject");
        notification.setTitle("审批拒绝");
        notification.setContent("您预约的" + roomName + " " + dateStr + " " + timeStr + " 未通过审批"
                + (reason != null && !reason.isEmpty() ? "，原因：" + reason : ""));
        notification.setBizType("booking");
        notification.setBizId(booking.getId());
        notification.setIsRead(0);

        save(notification);
        emailService.sendBookingRejectedEmail(booking, reason);
    }

    @Override
    public void sendSystemNotification(Long userId, String title, String content) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType("system");
        notification.setTitle(title);
        notification.setContent(content);
        notification.setIsRead(0);

        save(notification);
    }

    @Override
    public List<NotificationVO> getNotificationList(Long userId, String type, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Notification::getType, type);
        }
        wrapper.orderByDesc(Notification::getCreateTime);

        Page<Notification> page = page(new Page<>(pageNum, pageSize), wrapper);
        return page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = getById(notificationId);
        if (notification != null && notification.getUserId().equals(userId)) {
            notification.setIsRead(1);
            notification.setReadTime(LocalDateTime.now());
            updateById(notification);
        }
    }

    @Override
    public void markAllAsRead(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, 0);

        List<Notification> notifications = list(wrapper);
        for (Notification notification : notifications) {
            notification.setIsRead(1);
            notification.setReadTime(LocalDateTime.now());
        }
        updateBatchById(notifications);
    }

    @Override
    public int getUnreadCount(Long userId) {
        return baseMapper.countUnreadByUserId(userId);
    }

    private NotificationVO convertToVO(Notification notification) {
        NotificationVO vo = new NotificationVO();
        vo.setId(notification.getId());
        vo.setType(notification.getType());
        vo.setTypeName(getTypeName(notification.getType()));
        vo.setTitle(notification.getTitle());
        vo.setContent(notification.getContent());
        vo.setBizType(notification.getBizType());
        vo.setBizId(notification.getBizId());
        vo.setIsRead(notification.getIsRead());
        vo.setCreateTime(notification.getCreateTime());
        return vo;
    }

    private String getTypeName(String type) {
        if (type == null) return "";
        return switch (type) {
            case "success" -> "预约成功";
            case "remind" -> "开始提醒";
            case "cancel" -> "取消通知";
            case "pending" -> "待审批";
            case "approve" -> "审批通过";
            case "reject" -> "审批拒绝";
            case "invite" -> "参会邀请";
            case "waitlist" -> "候补通知";
            case "repair" -> "报修通知";
            case "minutes" -> "纪要提醒";
            case "system" -> "系统通知";
            default -> "";
        };
    }
}
