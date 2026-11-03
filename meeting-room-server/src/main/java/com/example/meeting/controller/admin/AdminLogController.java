package com.example.meeting.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.meeting.common.result.PageResult;
import com.example.meeting.common.result.Result;
import com.example.meeting.entity.OperationLog;
import com.example.meeting.mapper.OperationLogMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台管理 - 操作日志
 */
@Tag(name = "后台管理 - 操作日志")
@RestController
@RequestMapping("/api/v1/admin/operation-logs")
@RequiredArgsConstructor
public class AdminLogController {

    private final OperationLogMapper operationLogMapper;

    @Operation(summary = "操作日志列表")
    @GetMapping
    public Result<List<OperationLog>> getOperationLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String module,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(OperationLog::getUserId, userId);
        }
        if (module != null && !module.isEmpty()) {
            wrapper.eq(OperationLog::getModule, module);
        }
        wrapper.orderByDesc(OperationLog::getCreateTime);

        int offset = (pageNum - 1) * pageSize;
        List<OperationLog> logs = operationLogMapper.selectList(wrapper);
        int total = logs.size();

        // 简单分页
        List<OperationLog> pageList = logs.stream()
                .skip(offset)
                .limit(pageSize)
                .toList();

        return Result.success(pageList);
    }
}
