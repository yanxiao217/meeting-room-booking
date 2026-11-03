package com.example.meeting.controller.front;

import com.example.meeting.common.result.Result;
import com.example.meeting.entity.BookingTemplate;
import com.example.meeting.service.BookingTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会议模板接口
 */
@Tag(name = "会议模板接口")
@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final BookingTemplateService templateService;

    @Operation(summary = "我的模板列表")
    @GetMapping
    public Result<List<BookingTemplate>> getMyTemplates() {
        Long userId = getCurrentUserId();
        return Result.success(templateService.getMyTemplates(userId));
    }

    @Operation(summary = "创建模板")
    @PostMapping
    public Result<BookingTemplate> createTemplate(@RequestBody BookingTemplate template) {
        Long userId = getCurrentUserId();
        return Result.success(templateService.createTemplate(userId, template));
    }

    @Operation(summary = "编辑模板")
    @PutMapping("/{id}")
    public Result<Void> updateTemplate(@PathVariable Long id, @RequestBody BookingTemplate template) {
        Long userId = getCurrentUserId();
        templateService.updateTemplate(userId, id, template);
        return Result.success();
    }

    @Operation(summary = "删除模板")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        templateService.deleteTemplate(userId, id);
        return Result.success();
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        return null;
    }
}
