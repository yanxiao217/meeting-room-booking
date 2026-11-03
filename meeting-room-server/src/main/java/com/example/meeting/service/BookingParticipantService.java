package com.example.meeting.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.meeting.entity.BookingParticipant;

import java.util.List;

/**
 * 参会人服务接口
 */
public interface BookingParticipantService extends IService<BookingParticipant> {

    /**
     * 获取预约的参会人列表
     */
    List<BookingParticipant> getParticipantsByBookingId(Long bookingId);

    /**
     * 添加参会人
     */
    void addParticipant(Long bookingId, Long userId);

    /**
     * 移除参会人
     */
    void removeParticipant(Long bookingId, Long userId);

    /**
     * 更新参会人状态
     */
    void updateParticipantStatus(Long bookingId, Long userId, String status);

    /**
     * 检查用户是否为参会人
     */
    boolean isParticipant(Long bookingId, Long userId);
}
