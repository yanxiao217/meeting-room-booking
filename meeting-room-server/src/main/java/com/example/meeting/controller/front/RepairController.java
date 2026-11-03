package com.example.meeting.controller.front;

import com.example.meeting.common.result.Result;
import com.example.meeting.entity.RepairOrder;
import com.example.meeting.service.RepairOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 报修接口
 */
@Tag(name = "报修接口")
@RestController
@RequestMapping("/api/v1/repairs")
@RequiredArgsConstructor
public class RepairController {

    private final RepairOrderService repairOrderService;

    @Operation(summary = "发起报修")
    @PostMapping
    public Result<RepairOrder> createRepair(@RequestBody RepairOrder repairOrder) {
        Long userId = getCurrentUserId();
        return Result.success(repairOrderService.createRepair(userId, repairOrder));
    }

    @Operation(summary = "我的报修列表")
    @GetMapping("/mine")
    public Result<List<RepairOrder>> getMyRepairs() {
        Long userId = getCurrentUserId();
        return Result.success(repairOrderService.getMyRepairs(userId));
    }

    @Operation(summary = "报修详情")
    @GetMapping("/{id}")
    public Result<RepairOrder> getRepairDetail(@PathVariable Long id) {
        return Result.success(repairOrderService.getRepairDetail(id));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        return null;
    }
}
