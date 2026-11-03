package com.example.meeting.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.meeting.entity.RoomRating;

import java.util.List;
import java.util.Map;

/**
 * 会议室评价服务接口
 */
public interface RatingService extends IService<RoomRating> {

    /**
     * 提交评价
     * @param userId 用户ID
     * @param bookingId 预约ID
     * @param rating 评分 1-5
     * @param tags 标签JSON
     * @param comment 评价文字
     */
    void submitRating(Long userId, Long bookingId, int rating, String tags, String comment);

    /**
     * 获取会议室评价列表
     */
    List<Map<String, Object>> getRoomRatings(Long roomId, int limit);

    /**
     * 获取会议室平均分
     */
    Double getRoomAvgRating(Long roomId);

    /**
     * 低分记录转报修
     * @param checkId 低分检查记录ID
     * @param adminId 管理员ID
     */
    void convertToRepair(Long checkId, Long adminId);

    /**
     * 获取低分检查列表
     */
    List<Map<String, Object>> getPendingChecks();

    /**
     * 获取全部会议室评价统计
     */
    List<Map<String, Object>> getRoomRatingStats();

    /**
     * 获取指定会议室评分分布
     */
    List<Map<String, Object>> getRatingDistribution(Long roomId);
}
