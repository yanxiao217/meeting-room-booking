package com.example.meeting.controller.admin;

import com.example.meeting.common.result.Result;
import com.example.meeting.dto.AiModelConfigDTO;
import com.example.meeting.entity.AiModelConfig;
import com.example.meeting.service.AiModelConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 后台管理 - AI模型配置
 */
@Tag(name = "后台管理 - AI模型配置")
@RestController
@RequestMapping("/api/v1/admin/ai-models")
@RequiredArgsConstructor
public class AdminAiModelController {

    private final AiModelConfigService aiModelConfigService;

    @Operation(summary = "配置列表")
    @GetMapping
    public Result<List<AiModelConfig>> list() {
        return Result.success(aiModelConfigService.listAll());
    }

    @Operation(summary = "新增配置")
    @PostMapping
    public Result<AiModelConfig> create(@Valid @RequestBody AiModelConfigDTO dto) {
        return Result.success(aiModelConfigService.create(dto));
    }

    @Operation(summary = "更新配置")
    @PutMapping("/{id}")
    public Result<AiModelConfig> update(@PathVariable Long id, @Valid @RequestBody AiModelConfigDTO dto) {
        dto.setId(id);
        return Result.success(aiModelConfigService.update(dto));
    }

    @Operation(summary = "删除配置")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        aiModelConfigService.delete(id);
        return Result.success();
    }

    @Operation(summary = "设为默认")
    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id) {
        aiModelConfigService.setDefault(id);
        return Result.success();
    }

    @Operation(summary = "启用/停用")
    @PutMapping("/{id}/status")
    public Result<Void> toggle(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        aiModelConfigService.toggle(id, body.get("enabled"));
        return Result.success();
    }

    @Operation(summary = "测试连通性")
    @PostMapping("/{id}/test")
    public Result<Map<String, Object>> test(@PathVariable Long id) {
        return Result.success(aiModelConfigService.testConnection(id));
    }
}
