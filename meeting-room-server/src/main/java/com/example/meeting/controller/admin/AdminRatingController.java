package com.example.meeting.controller.admin;

import com.example.meeting.common.result.Result;
import com.example.meeting.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 后台管理 - 评价管理
 */
@Tag(name = "后台管理 - 评价管理")
@RestController
@RequestMapping("/api/v1/admin/ratings")
@RequiredArgsConstructor
public class AdminRatingController {

    private final RatingService ratingService;

    @Operation(summary = "全部会议室评价统计")
    @GetMapping("/stats")
    public Result<List<Map<String, Object>>> getRoomRatingStats() {
        return Result.success(ratingService.getRoomRatingStats());
    }

    @Operation(summary = "指定会议室评分分布")
    @GetMapping("/distribution/{roomId}")
    public Result<List<Map<String, Object>>> getRatingDistribution(@PathVariable Long roomId) {
        return Result.success(ratingService.getRatingDistribution(roomId));
    }

    @Operation(summary = "低分检查列表")
    @GetMapping("/pending-checks")
    public Result<List<Map<String, Object>>> getPendingChecks() {
        return Result.success(ratingService.getPendingChecks());
    }

    @Operation(summary = "低分记录转报修")
    @PostMapping("/convert-to-repair/{checkId}")
    public Result<Void> convertToRepair(@PathVariable Long checkId) {
        Long adminId = getCurrentUserId();
        ratingService.convertToRepair(checkId, adminId);
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
