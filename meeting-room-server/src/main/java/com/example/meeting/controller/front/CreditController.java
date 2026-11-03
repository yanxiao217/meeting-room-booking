package com.example.meeting.controller.front;

import com.example.meeting.common.result.Result;
import com.example.meeting.entity.CreditRecord;
import com.example.meeting.entity.User;
import com.example.meeting.mapper.UserMapper;
import com.example.meeting.service.CreditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户端 - 信用分接口
 */
@Tag(name = "用户端 - 信用分")
@RestController
@RequestMapping("/api/v1/credit")
@RequiredArgsConstructor
public class CreditController {

    private final CreditService creditService;
    private final UserMapper userMapper;

    @Operation(summary = "查看我的信用分")
    @GetMapping("/my-score")
    public Result<Map<String, Object>> getMyScore() {
        Long userId = getCurrentUserId();
        User user = userMapper.selectById(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("score", user != null && user.getCreditScore() != null ? user.getCreditScore() : 100);
        data.put("threshold", Integer.parseInt(creditService.getConfigValue("credit.threshold", "60")));
        data.put("restrictDays", Integer.parseInt(creditService.getConfigValue("credit.restrict_days", "3")));
        return Result.success(data);
    }

    @Operation(summary = "查看我的信用记录")
    @GetMapping("/my-records")
    public Result<List<CreditRecord>> getMyRecords(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = getCurrentUserId();
        return Result.success(creditService.getRecords(userId, pageNum, pageSize));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        return null;
    }
}
