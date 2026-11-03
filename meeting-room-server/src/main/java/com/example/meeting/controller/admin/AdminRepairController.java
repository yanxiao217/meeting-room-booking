package com.example.meeting.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.meeting.common.result.PageResult;
import com.example.meeting.common.result.Result;
import com.example.meeting.entity.RepairOrder;
import com.example.meeting.service.RepairOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 后台管理 - 报修管理
 */
@Tag(name = "后台管理 - 报修管理")
@RestController
@RequestMapping("/api/v1/admin/repairs")
@RequiredArgsConstructor
public class AdminRepairController {

    private final RepairOrderService repairOrderService;

    @Operation(summary = "报修工单列表")
    @GetMapping
    public Result<PageResult<RepairOrder>> getRepairList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long roomId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<RepairOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(RepairOrder::getStatus, status);
        }
        if (roomId != null) {
            wrapper.eq(RepairOrder::getRoomId, roomId);
        }
        wrapper.orderByDesc(RepairOrder::getCreateTime);
        repairOrderService.page(page, wrapper);
        PageResult<RepairOrder> result = new PageResult<>();
        result.setList(page.getRecords());
        result.setTotal(page.getTotal());
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        return Result.success(result);
    }

    @Operation(summary = "转派/接单")
    @PutMapping("/{id}/assign")
    public Result<Void> assignRepair(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        Long assigneeId = body.get("assigneeId");
        repairOrderService.assignRepair(null, id, assigneeId);
        return Result.success();
    }

    @Operation(summary = "完成工单")
    @PutMapping("/{id}/finish")
    public Result<Void> finishRepair(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String handleDesc = body.get("handleDesc");
        repairOrderService.finishRepair(null, id, handleDesc);
        return Result.success();
    }
}
