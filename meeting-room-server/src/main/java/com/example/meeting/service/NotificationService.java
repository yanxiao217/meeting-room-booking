package com.example.meeting.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.meeting.entity.Booking;
import com.example.meeting.entity.Notification;
import com.example.meeting.vo.NotificationVO;

import java.util.List;

/**
 * 通知服务接口
 */
public interface NotificationService extends IService<Notification> {

    /**
     * 发送预约成功通知
     */
    void sendBookingSuccessNotification(Booking booking);

    /**
     * 发送取消通知
     */
    void sendCancelNotification(Booking booking);

    /**
     * 发送预约待审批通知
     */
    void sendBookingPendingNotification(Booking booking);

    /**
     * 发送审批通过通知
     */
    void sendBookingApprovedNotification(Booking booking);

    /**
     * 发送审批拒绝通知
     */
    void sendBookingRejectedNotification(Booking booking, String reason);

    /**
     * 发送系统通知
     */
    void sendSystemNotification(Long userId, String title, String content);

    /**
     * 获取用户通知列表
     */
    List<NotificationVO> getNotificationList(Long userId, String type, Integer pageNum, Integer pageSize);

    /**
     * 标记已读
     */
    void markAsRead(Long userId, Long notificationId);

    /**
     * 全部标记已读
     */
    void markAllAsRead(Long userId);

    /**
     * 获取未读数量
     */
    int getUnreadCount(Long userId);
}
