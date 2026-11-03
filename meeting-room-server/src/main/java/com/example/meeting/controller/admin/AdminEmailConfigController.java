package com.example.meeting.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.meeting.entity.SysConfig;
import com.example.meeting.mapper.SysConfigMapper;
import com.example.meeting.service.EmailService;
import com.example.meeting.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理后台 - 邮件配置管理
 */
@Tag(name = "管理后台-邮件配置")
@RestController
@RequestMapping("/api/v1/admin/email")
@RequiredArgsConstructor
public class AdminEmailConfigController {

    private final SysConfigMapper sysConfigMapper;
    private final EmailService emailService;

    @Data
    public static class SmtpConfigDTO {
        private String host;
        private String port;
        private String username;
        private String password;
        private String fromName;
        private String testRecipient;
    }

    @Operation(summary = "获取SMTP配置")
    @GetMapping("/smtp")
    public Result<Map<String, String>> getSmtpConfig() {
        Map<String, String> map = new HashMap<>();
        String[] keys = {"email.smtp.host", "email.smtp.port", "email.smtp.username",
                "email.smtp.password", "email.smtp.from-name", "email.smtp.test-recipient"};
        for (String key : keys) {
            SysConfig c = sysConfigMapper.selectByConfigKey(key);
            map.put(key, c != null ? c.getConfigValue() : "");
        }
        return Result.success(map);
    }

    @Operation(summary = "保存SMTP配置")
    @PutMapping("/smtp")
    public Result<Void> saveSmtpConfig(@RequestBody SmtpConfigDTO dto) {
        saveConfig("email.smtp.host", dto.getHost());
        saveConfig("email.smtp.port", dto.getPort());
        saveConfig("email.smtp.username", dto.getUsername());
        saveConfig("email.smtp.password", dto.getPassword());
        saveConfig("email.smtp.from-name", dto.getFromName());
        saveConfig("email.smtp.test-recipient", dto.getTestRecipient());
        return Result.success();
    }

    @Operation(summary = "发送测试邮件")
    @PostMapping("/test")
    public Result<Void> sendTestEmail(@RequestBody Map<String, String> body) {
        String toEmail = body.get("email");
        if (toEmail == null || toEmail.isEmpty()) {
            return Result.error("请输入测试邮箱");
        }
        boolean ok = emailService.sendTestEmail(toEmail);
        return ok ? Result.success() : Result.error("邮件发送失败，请检查SMTP配置");
    }

    private void saveConfig(String key, String value) {
        SysConfig config = sysConfigMapper.selectByConfigKey(key);
        if (config != null) {
            config.setConfigValue(value != null ? value : "");
            sysConfigMapper.updateById(config);
        } else {
            config = new SysConfig();
            config.setConfigKey(key);
            config.setConfigValue(value != null ? value : "");
            sysConfigMapper.insert(config);
        }
    }
}
