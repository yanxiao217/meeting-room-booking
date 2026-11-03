package com.example.meeting.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.meeting.common.result.Result;
import com.example.meeting.entity.CreditRecord;
import com.example.meeting.entity.SysConfig;
import com.example.meeting.entity.User;
import com.example.meeting.mapper.SysConfigMapper;
import com.example.meeting.mapper.UserMapper;
import com.example.meeting.service.CreditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 后台管理 - 信用分管理
 */
@Tag(name = "后台管理 - 信用分管理")
@RestController
@RequestMapping("/api/v1/admin/credit")
@RequiredArgsConstructor
public class AdminCreditController {

    private final CreditService creditService;
    private final SysConfigMapper sysConfigMapper;
    private final UserMapper userMapper;

    @Operation(summary = "获取信用分规则配置")
    @GetMapping("/rules")
    public Result<List<SysConfig>> getCreditRules() {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(SysConfig::getConfigKey, "credit.");
        wrapper.ne(SysConfig::getConfigKey, "credit.last_recover_week");
        wrapper.orderByAsc(SysConfig::getId);
        return Result.success(sysConfigMapper.selectList(wrapper));
    }

    @Operation(summary = "更新信用分规则配置")
    @PutMapping("/rules")
    public Result<Void> updateCreditRules(@RequestBody List<SysConfig> configs) {
        if (configs != null) {
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
        return Result.success();
    }

    @Operation(summary = "信用记录查询（按用户）")
    @GetMapping("/records")
    public Result<List<CreditRecord>> getCreditRecords(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(creditService.getRecords(userId, pageNum, pageSize));
    }

    @Operation(summary = "管理员调整信用分")
    @PostMapping("/adjust")
    public Result<Void> adjustCredit(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        int value = Integer.parseInt(body.get("value").toString());
        String reason = (String) body.get("reason");
        creditService.adminAdjust(userId, value, reason);
        return Result.success();
    }

    @Operation(summary = "低分用户列表")
    @GetMapping("/low-score-users")
    public Result<List<User>> getLowScoreUsers() {
        String thresholdStr = "60";
        SysConfig config = sysConfigMapper.selectByConfigKey("credit.threshold");
        if (config != null) thresholdStr = config.getConfigValue();
        int threshold = Integer.parseInt(thresholdStr);

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(User::getCreditScore, threshold);
        wrapper.eq(User::getStatus, 1);
        return Result.success(userMapper.selectList(wrapper));
    }
}
