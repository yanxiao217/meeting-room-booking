package com.example.meeting.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.meeting.common.result.Result;
import com.example.meeting.entity.Dept;
import com.example.meeting.mapper.DeptMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台管理 - 部门管理
 */
@Tag(name = "后台管理 - 部门管理")
@RestController
@RequestMapping("/api/v1/admin/depts")
@RequiredArgsConstructor
public class AdminDeptController {

    private final DeptMapper deptMapper;

    @Operation(summary = "部门列表")
    @GetMapping
    public Result<List<Dept>> getDeptList() {
        LambdaQueryWrapper<Dept> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Dept::getSort);
        return Result.success(deptMapper.selectList(wrapper));
    }

    @Operation(summary = "新增部门")
    @PostMapping
    public Result<Void> createDept(@RequestBody Dept dept) {
        deptMapper.insert(dept);
        return Result.success();
    }

    @Operation(summary = "编辑部门")
    @PutMapping("/{id}")
    public Result<Void> updateDept(@PathVariable Long id, @RequestBody Dept dept) {
        dept.setId(id);
        deptMapper.updateById(dept);
        return Result.success();
    }

    @Operation(summary = "删除部门")
    @DeleteMapping("/{id}")
    public Result<Void> deleteDept(@PathVariable Long id) {
        deptMapper.deleteById(id);
        return Result.success();
    }
}
