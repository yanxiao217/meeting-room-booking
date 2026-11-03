package com.example.meeting.controller.front;

import com.example.meeting.common.result.Result;
import com.example.meeting.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户端 - 评价接口
 */
@Tag(name = "用户端 - 评价接口")
@RestController
@RequestMapping("/api/v1/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @Operation(summary = "提交评价")
    @PostMapping
    public Result<Void> submitRating(@RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId();
        Long bookingId = Long.valueOf(body.get("bookingId").toString());
        int rating = Integer.parseInt(body.get("rating").toString());
        String tags = (String) body.getOrDefault("tags", null);
        String comment = (String) body.getOrDefault("comment", null);
        ratingService.submitRating(userId, bookingId, rating, tags, comment);
        return Result.success();
    }

    @Operation(summary = "获取会议室评价列表")
    @GetMapping("/room/{roomId}")
    public Result<?> getRoomRatings(@PathVariable Long roomId,
                                     @RequestParam(defaultValue = "10") int limit) {
        return Result.success(ratingService.getRoomRatings(roomId, limit));
    }

    @Operation(summary = "获取会议室平均分")
    @GetMapping("/room/{roomId}/avg")
    public Result<Double> getRoomAvgRating(@PathVariable Long roomId) {
        return Result.success(ratingService.getRoomAvgRating(roomId));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        return null;
    }
}
