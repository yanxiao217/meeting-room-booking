package com.example.meeting.controller.admin;

import com.example.meeting.common.result.Result;
import com.example.meeting.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 后台管理 - 统计接口
 */
@Tag(name = "后台管理 - 数据统计")
@RestController
@RequestMapping("/api/v1/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final StatsService statsService;

    @Operation(summary = "概览指标")
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(statsService.getOverview(startDate, endDate));
    }

    @Operation(summary = "会议室使用率统计")
    @GetMapping("/usage")
    public Result<List<Map<String, Object>>> getUsageStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(statsService.getUsageStats(startDate, endDate));
    }

    @Operation(summary = "热门会议室排行")
    @GetMapping("/hot-rooms")
    public Result<List<Map<String, Object>>> getHotRooms(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(statsService.getHotRooms(startDate, endDate));
    }

    @Operation(summary = "预约趋势统计")
    @GetMapping("/trend")
    public Result<Map<String, Object>> getBookingTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(statsService.getBookingTrend(startDate, endDate));
    }
}
