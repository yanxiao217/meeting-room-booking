package com.example.meeting.controller.admin;

import com.example.meeting.common.result.PageResult;
import com.example.meeting.common.result.Result;
import com.example.meeting.entity.ChatbotLog;
import com.example.meeting.service.ChatBotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 后台管理 - 智能问答日志
 */
@Tag(name = "后台管理 - 智能问答日志")
@RestController
@RequestMapping("/api/v1/admin/chatbot-logs")
@RequiredArgsConstructor
public class AdminChatBotController {

    private final ChatBotService chatBotService;

    @Operation(summary = "问答日志列表")
    @GetMapping
    public Result<PageResult<ChatbotLog>> getLogs(
            @RequestParam(required = false) String intent,
            @RequestParam(required = false) Integer success,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(chatBotService.getLogs(intent, success, pageNum, pageSize));
    }
}
