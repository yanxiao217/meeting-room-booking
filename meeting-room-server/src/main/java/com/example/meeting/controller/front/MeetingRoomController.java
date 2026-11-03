package com.example.meeting.controller.front;

import com.example.meeting.common.result.Result;
import com.example.meeting.service.MeetingRoomService;
import com.example.meeting.service.RoomFavoriteService;
import com.example.meeting.vo.AvailabilityVO;
import com.example.meeting.vo.CalendarVO;
import com.example.meeting.vo.RoomVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 会议室接口（前台）
 */
@Tag(name = "会议室接口（前台）")
@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class MeetingRoomController {

    private final MeetingRoomService meetingRoomService;
    private final com.example.meeting.service.BookingService bookingService;
    private final RoomFavoriteService roomFavoriteService;

    @Operation(summary = "获取会议室列表")
    @GetMapping
    public Result<List<RoomVO>> getRoomList(
            @RequestParam(required = false) String floor,
            @RequestParam(required = false) Integer capacity,
            @RequestParam(required = false) Integer status) {
        List<RoomVO> rooms = meetingRoomService.getRoomList(floor, capacity, status != null ? status : 1);
        return Result.success(rooms);
    }

    @Operation(summary = "获取会议室详情")
    @GetMapping("/{id}")
    public Result<RoomVO> getRoomDetail(@PathVariable Long id) {
        RoomVO room = meetingRoomService.getRoomDetail(id);
        return Result.success(room);
    }

    @Operation(summary = "获取会议室可用时段")
    @GetMapping("/{id}/availability")
    public Result<AvailabilityVO> getAvailability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        AvailabilityVO availability = bookingService.getAvailability(id, date);
        return Result.success(availability);
    }

    @Operation(summary = "获取日历视图数据")
    @GetMapping("/calendar")
    public Result<CalendarVO> getCalendarData(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String floor,
            @RequestParam(required = false) Integer capacity) {
        CalendarVO calendarVO = bookingService.getCalendarData(startDate, endDate, floor, capacity);
        return Result.success(calendarVO);
    }

    @Operation(summary = "收藏会议室")
    @PostMapping("/{id}/favorite")
    public Result<Void> addFavorite(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        roomFavoriteService.addFavorite(userId, id);
        return Result.success();
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/{id}/favorite")
    public Result<Void> removeFavorite(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        roomFavoriteService.removeFavorite(userId, id);
        return Result.success();
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        return null;
    }
}
