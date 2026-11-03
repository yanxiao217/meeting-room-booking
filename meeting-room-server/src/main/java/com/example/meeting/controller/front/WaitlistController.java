package com.example.meeting.controller.front;

import com.example.meeting.common.result.Result;
import com.example.meeting.entity.BookingWaitlist;
import com.example.meeting.service.BookingWaitlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * 候补队列接口
 */
@Tag(name = "候补队列接口")
@RestController
@RequestMapping("/api/v1/waitlist")
@RequiredArgsConstructor
public class WaitlistController {

    private final BookingWaitlistService waitlistService;

    @Operation(summary = "加入候补队列")
    @PostMapping
    public Result<Void> joinWaitlist(@RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId();
        Long roomId = Long.valueOf(body.get("roomId"));
        LocalDate bookingDate = LocalDate.parse(body.get("bookingDate"));
        LocalTime startTime = LocalTime.parse(body.get("startTime"));
        LocalTime endTime = LocalTime.parse(body.get("endTime"));
        waitlistService.joinWaitlist(userId, roomId, bookingDate, startTime, endTime);
        return Result.success();
    }

    @Operation(summary = "我的候补列表")
    @GetMapping("/mine")
    public Result<List<BookingWaitlist>> getMyWaitlist() {
        Long userId = getCurrentUserId();
        return Result.success(waitlistService.getMyWaitlist(userId));
    }

    @Operation(summary = "退出候补")
    @DeleteMapping("/{id}")
    public Result<Void> leaveWaitlist(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        waitlistService.leaveWaitlist(userId, id);
        return Result.success();
    }

    @Operation(summary = "候补确认")
    @PostMapping("/{id}/confirm")
    public Result<Void> confirmWaitlist(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        waitlistService.confirmWaitlist(userId, id);
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
