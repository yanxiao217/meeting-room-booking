package com.example.meeting.service.impl;

import com.example.meeting.mapper.BookingMapper;
import com.example.meeting.mapper.MeetingRoomMapper;
import com.example.meeting.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计服务实现
 */
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final BookingMapper bookingMapper;
    private final MeetingRoomMapper meetingRoomMapper;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public Map<String, Object> getOverview(String startDate, String endDate) {
        if (startDate == null || endDate == null) {
            LocalDate now = LocalDate.now();
            startDate = now.withDayOfMonth(1).format(FMT);
            endDate = now.format(FMT);
        }

        // 会议室总数（全部会议室）
        long totalRooms = meetingRoomMapper.selectCount(null);
        // 总预约数
        long totalBookings = bookingMapper.countBookingsByDateRange(startDate, endDate);
        // 活跃用户数
        int activeUsers = bookingMapper.countActiveUsersByDateRange(startDate, endDate);
        // 平均使用率（各会议室使用率均值）
        List<Map<String, Object>> usage = bookingMapper.selectUsageStats(startDate, endDate);
        double avgUsageRate = usage.stream()
                .mapToDouble(m -> toDouble(m.get("usageRate")))
                .average()
                .orElse(0.0);

        Map<String, Object> result = new HashMap<>();
        result.put("totalRooms", totalRooms);
        result.put("totalBookings", totalBookings);
        result.put("activeUsers", activeUsers);
        result.put("avgUsageRate", Math.round(avgUsageRate * 10) / 10.0);
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        return result;
    }

    @Override
    public List<Map<String, Object>> getUsageStats(String startDate, String endDate) {
        if (startDate == null || endDate == null) {
            LocalDate now = LocalDate.now();
            startDate = now.withDayOfMonth(1).format(FMT);
            endDate = now.format(FMT);
        }
        return bookingMapper.selectUsageStats(startDate, endDate);
    }

    @Override
    public List<Map<String, Object>> getHotRooms(String startDate, String endDate) {
        if (startDate == null || endDate == null) {
            LocalDate now = LocalDate.now();
            startDate = now.withDayOfMonth(1).format(FMT);
            endDate = now.format(FMT);
        }
        return bookingMapper.selectHotRooms(startDate, endDate);
    }

    @Override
    public Map<String, Object> getBookingTrend(String startDate, String endDate) {
        if (startDate == null || endDate == null) {
            LocalDate now = LocalDate.now();
            startDate = now.minusDays(6).format(FMT);
            endDate = now.format(FMT);
        }
        List<Map<String, Object>> rows = bookingMapper.selectBookingTrend(startDate, endDate);
        Map<String, Object> result = new HashMap<>();
        result.put("dates", rows.stream().map(r -> r.get("dates")).toList());
        result.put("values", rows.stream().map(r -> r.get("cnt")).toList());
        return result;
    }

    private double toDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
