package com.example.meeting.controller.front;

import com.example.meeting.common.result.Result;
import com.example.meeting.dto.ChatBotAskDTO;
import com.example.meeting.service.ChatBotService;
import com.example.meeting.vo.ChatBotAnswerVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 智能问答助手接口
 */
@Tag(name = "智能问答助手")
@RestController
@RequestMapping("/api/v1/chatbot")
@RequiredArgsConstructor
public class ChatBotController {

    private final ChatBotService chatBotService;

    @Operation(summary = "提问")
    @PostMapping("/ask")
    public Result<ChatBotAnswerVO> ask(@Valid @RequestBody ChatBotAskDTO dto) {
        Long userId = getCurrentUserId();
        return Result.success(chatBotService.ask(userId, dto.getQuestion()));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        return null;
    }
}
