package com.example.meeting.service;

import java.util.List;
import java.util.Map;

/**
 * 统计服务接口
 */
public interface StatsService {

    /**
     * 获取概览指标（会议室总数/总预约数/平均使用率/活跃用户）
     */
    Map<String, Object> getOverview(String startDate, String endDate);

    /**
     * 获取会议室使用率统计
     */
    List<Map<String, Object>> getUsageStats(String startDate, String endDate);

    /**
     * 获取热门会议室
     */
    List<Map<String, Object>> getHotRooms(String startDate, String endDate);

    /**
     * 获取预约趋势
     */
    Map<String, Object> getBookingTrend(String startDate, String endDate);
}
