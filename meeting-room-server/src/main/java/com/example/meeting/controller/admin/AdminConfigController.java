package com.example.meeting.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.meeting.common.result.Result;
import com.example.meeting.entity.SysConfig;
import com.example.meeting.mapper.SysConfigMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台管理 - 系统设置
 */
@Tag(name = "后台管理 - 系统设置")
@RestController
@RequestMapping("/api/v1/admin/config")
@RequiredArgsConstructor
public class AdminConfigController {

    private final SysConfigMapper sysConfigMapper;

    @Operation(summary = "获取预约规则配置")
    @GetMapping("/booking-rules")
    public Result<List<SysConfig>> getBookingRules() {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(SysConfig::getConfigKey, "booking.");
        wrapper.orderByAsc(SysConfig::getId);
        List<SysConfig> configs = sysConfigMapper.selectList(wrapper);
        return Result.success(configs);
    }

    @Operation(summary = "更新预约规则配置")
    @PutMapping("/booking-rules")
    public Result<Void> updateBookingRules(@RequestBody List<SysConfig> configs) {
        upsertAll(configs);
        return Result.success();
    }

    @Operation(summary = "获取系统配置")
    @GetMapping("/system")
    public Result<List<SysConfig>> getSystemConfig() {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.notLike(SysConfig::getConfigKey, "booking.");
        wrapper.orderByAsc(SysConfig::getId);
        List<SysConfig> configs = sysConfigMapper.selectList(wrapper);
        return Result.success(configs);
    }

    @Operation(summary = "获取候补规则配置")
    @GetMapping("/waitlist")
    public Result<List<SysConfig>> getWaitlistRules() {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(SysConfig::getConfigKey, "waitlist.");
        wrapper.orderByAsc(SysConfig::getId);
        return Result.success(sysConfigMapper.selectList(wrapper));
    }

    @Operation(summary = "更新候补规则配置")
    @PutMapping("/waitlist")
    public Result<Void> updateWaitlistRules(@RequestBody List<SysConfig> configs) {
        upsertAll(configs);
        return Result.success();
    }

    @Operation(summary = "获取周期预约配置")
    @GetMapping("/recurring")
    public Result<List<SysConfig>> getRecurringRules() {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(SysConfig::getConfigKey, "recurring.");
        wrapper.orderByAsc(SysConfig::getId);
        return Result.success(sysConfigMapper.selectList(wrapper));
    }

    @Operation(summary = "更新周期预约配置")
    @PutMapping("/recurring")
    public Result<Void> updateRecurringRules(@RequestBody List<SysConfig> configs) {
        upsertAll(configs);
        return Result.success();
    }

    @Operation(summary = "获取审批配置")
    @GetMapping("/approval")
    public Result<List<SysConfig>> getApprovalRules() {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(SysConfig::getConfigKey, "approval.");
        wrapper.orderByAsc(SysConfig::getId);
        return Result.success(sysConfigMapper.selectList(wrapper));
    }

    @Operation(summary = "更新审批配置")
    @PutMapping("/approval")
    public Result<Void> updateApprovalRules(@RequestBody List<SysConfig> configs) {
        upsertAll(configs);
        return Result.success();
    }

    /**
     * 按 configKey 更新配置；不存在则新增。
     * 前端传入的 id 可能为 0 或缺失，因此以 configKey 为准。
     */
    private void upsertAll(List<SysConfig> configs) {
        if (configs == null) return;
        for (SysConfig config : configs) {
            if (config.getConfigKey() == null || config.getConfigKey().isEmpty()) continue;
            SysConfig existing = sysConfigMapper.selectByConfigKey(config.getConfigKey());
            if (existing != null) {
                existing.setConfigValue(config.getConfigValue());
                if (config.getConfigName() != null) {
                    existing.setConfigName(config.getConfigName());
                }
                sysConfigMapper.updateById(existing);
            } else {
                SysConfig nc = new SysConfig();
                nc.setConfigKey(config.getConfigKey());
                nc.setConfigValue(config.getConfigValue());
                nc.setConfigName(config.getConfigName());
                sysConfigMapper.insert(nc);
            }
        }
    }
}
