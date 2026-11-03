package com.example.meeting.controller.front;

import com.example.meeting.common.result.Result;
import com.example.meeting.entity.MeetingMinutes;
import com.example.meeting.service.MeetingMinutesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 会议纪要接口
 */
@Tag(name = "会议纪要接口")
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class MinutesController {

    private final MeetingMinutesService minutesService;

    @Operation(summary = "提交会议纪要")
    @PostMapping("/{id}/minutes")
    public Result<MeetingMinutes> submitMinutes(@PathVariable Long id, @RequestBody MeetingMinutes minutes) {
        Long userId = getCurrentUserId();
        return Result.success(minutesService.submitMinutes(userId, id, minutes));
    }

    @Operation(summary = "获取会议纪要")
    @GetMapping("/{id}/minutes")
    public Result<MeetingMinutes> getMinutes(@PathVariable Long id) {
        return Result.success(minutesService.getMinutesByBookingId(id));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        return null;
    }
}
