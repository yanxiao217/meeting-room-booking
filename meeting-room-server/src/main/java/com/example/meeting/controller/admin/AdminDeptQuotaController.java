package com.example.meeting.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.meeting.common.result.Result;
import com.example.meeting.entity.DeptQuota;
import com.example.meeting.service.DeptQuotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台管理 - 部门配额管理
 */
@Tag(name = "后台管理 - 部门配额管理")
@RestController
@RequestMapping("/api/v1/admin/dept-quotas")
@RequiredArgsConstructor
public class AdminDeptQuotaController {

    private final DeptQuotaService deptQuotaService;

    @Operation(summary = "部门配额列表")
    @GetMapping
    public Result<List<DeptQuota>> getQuotaList() {
        return Result.success(deptQuotaService.list());
    }

    @Operation(summary = "新增部门配额")
    @PostMapping
    public Result<Void> createQuota(@RequestBody DeptQuota deptQuota) {
        deptQuotaService.save(deptQuota);
        return Result.success();
    }

    @Operation(summary = "更新部门配额")
    @PutMapping("/{id}")
    public Result<Void> updateQuota(@PathVariable Long id, @RequestBody DeptQuota deptQuota) {
        deptQuota.setId(id);
        deptQuotaService.updateDeptQuota(deptQuota);
        return Result.success();
    }

    @Operation(summary = "删除部门配额")
    @DeleteMapping("/{id}")
    public Result<Void> deleteQuota(@PathVariable Long id) {
        deptQuotaService.removeById(id);
        return Result.success();
    }
}
