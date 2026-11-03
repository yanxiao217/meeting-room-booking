package com.example.meeting.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.meeting.common.exception.BusinessException;
import com.example.meeting.dto.AiModelConfigDTO;
import com.example.meeting.entity.AiModelConfig;
import com.example.meeting.mapper.AiModelConfigMapper;
import com.example.meeting.service.AiModelConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI模型配置服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiModelConfigServiceImpl extends ServiceImpl<AiModelConfigMapper, AiModelConfig>
        implements AiModelConfigService {

    private static final String MASK = "******";

    @Override
    public List<AiModelConfig> listAll() {
        List<AiModelConfig> list = list(new LambdaQueryWrapper<AiModelConfig>()
                .orderByDesc(AiModelConfig::getIsDefault)
                .orderByAsc(AiModelConfig::getId));
        list.forEach(c -> c.setApiKey(mask(c.getApiKey())));
        return list;
    }

    @Override
    public AiModelConfig create(AiModelConfigDTO dto) {
        AiModelConfig entity = new AiModelConfig();
        copy(dto, entity);
        if (entity.getEnabled() == null) {
            entity.setEnabled(1);
        }
        if (entity.getIsDefault() == null) {
            entity.setIsDefault(0);
        }
        if (entity.getIsDefault() == 1) {
            clearDefault(null);
            entity.setEnabled(1);
        }
        save(entity);
        return entity;
    }

    @Override
    public AiModelConfig update(AiModelConfigDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(400, "缺少配置ID");
        }
        AiModelConfig entity = getById(dto.getId());
        if (entity == null) {
            throw new BusinessException(404, "配置不存在");
        }
        // 密钥留空或为掩码时，保留原密钥
        String originalKey = entity.getApiKey();
        String incomingKey = dto.getApiKey();
        copy(dto, entity);
        if (incomingKey == null || incomingKey.isBlank() || MASK.equals(incomingKey)) {
            entity.setApiKey(originalKey);
        }
        if (entity.getIsDefault() != null && entity.getIsDefault() == 1) {
            clearDefault(entity.getId());
            entity.setEnabled(1);
        }
        updateById(entity);
        return entity;
    }

    @Override
    public void delete(Long id) {
        AiModelConfig entity = getById(id);
        if (entity == null) {
            throw new BusinessException(404, "配置不存在");
        }
        removeById(id);
    }

    @Override
    public void setDefault(Long id) {
        AiModelConfig entity = getById(id);
        if (entity == null) {
            throw new BusinessException(404, "配置不存在");
        }
        clearDefault(id);
        entity.setIsDefault(1);
        entity.setEnabled(1);
        updateById(entity);
    }

    @Override
    public void toggle(Long id, Integer enabled) {
        AiModelConfig entity = getById(id);
        if (entity == null) {
            throw new BusinessException(404, "配置不存在");
        }
        entity.setEnabled(enabled != null && enabled == 1 ? 1 : 0);
        updateById(entity);
    }

    @Override
    public AiModelConfig getActive() {
        // 1) 默认 + 启用 + 已配置密钥
        AiModelConfig cfg = getOne(withKey().eq(AiModelConfig::getIsDefault, 1).last("LIMIT 1"), false);
        if (cfg != null) {
            return cfg;
        }
        // 2) 任一启用 + 已配置密钥（优先默认）
        cfg = getOne(withKey().orderByDesc(AiModelConfig::getIsDefault)
                .orderByAsc(AiModelConfig::getId).last("LIMIT 1"), false);
        if (cfg != null) {
            return cfg;
        }
        // 3) 默认 + 启用（未配置密钥，供前端展示）
        cfg = getOne(new LambdaQueryWrapper<AiModelConfig>()
                .eq(AiModelConfig::getEnabled, 1)
                .eq(AiModelConfig::getIsDefault, 1)
                .last("LIMIT 1"), false);
        if (cfg != null) {
            return cfg;
        }
        // 4) 任一启用
        return getOne(new LambdaQueryWrapper<AiModelConfig>()
                .eq(AiModelConfig::getEnabled, 1)
                .orderByAsc(AiModelConfig::getId)
                .last("LIMIT 1"), false);
    }

    private LambdaQueryWrapper<AiModelConfig> withKey() {
        return new LambdaQueryWrapper<AiModelConfig>()
                .eq(AiModelConfig::getEnabled, 1)
                .isNotNull(AiModelConfig::getApiKey)
                .ne(AiModelConfig::getApiKey, "");
    }

    @Override
    public Map<String, Object> testConnection(Long id) {
        AiModelConfig cfg = getById(id);
        if (cfg == null) {
            throw new BusinessException(404, "配置不存在");
        }
        Map<String, Object> result = new HashMap<>();
        if (cfg.getApiKey() == null || cfg.getApiKey().isBlank()) {
            result.put("success", false);
            result.put("message", "尚未配置 API Key");
            return result;
        }

        long start = System.currentTimeMillis();
        try {
            String reply = chat(cfg, "ping", "请回复：pong");
            result.put("success", true);
            result.put("message", "连接成功");
            result.put("reply", reply);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "连接失败：" + rootMessage(e));
        } finally {
            result.put("latencyMs", System.currentTimeMillis() - start);
        }
        return result;
    }

    // ------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private String chat(AiModelConfig cfg, String system, String user) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(Math.max(5, nz(cfg.getTimeoutSeconds(), 20)) * 1000);

        RestClient client = RestClient.builder()
                .baseUrl(cfg.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + cfg.getApiKey())
                .requestFactory(factory)
                .build();

        Map<String, Object> body = new HashMap<>();
        body.put("model", cfg.getModel());
        body.put("temperature", cfg.getTemperature() != null ? cfg.getTemperature() : new BigDecimal("0.2"));
        if (cfg.getMaxTokens() != null) {
            body.put("max_tokens", cfg.getMaxTokens());
        }
        body.put("messages", List.of(
                Map.of("role", "system", "content", system),
                Map.of("role", "user", "content", user)
        ));

        Map<String, Object> resp = client.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        if (resp == null) {
            throw new IllegalStateException("空响应");
        }
        Object choices = resp.get("choices");
        if (!(choices instanceof List<?> list) || list.isEmpty()) {
            throw new IllegalStateException("响应缺少choices");
        }
        Object msg = ((Map<?, ?>) list.get(0)).get("message");
        if (msg instanceof Map<?, ?> mm && mm.get("content") != null) {
            return mm.get("content").toString();
        }
        return "";
    }

    private void copy(AiModelConfigDTO dto, AiModelConfig entity) {
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setProvider(dto.getProvider() != null ? dto.getProvider() : "openai");
        entity.setBaseUrl(dto.getBaseUrl());
        entity.setApiKey(dto.getApiKey());
        entity.setModel(dto.getModel());
        entity.setTemperature(dto.getTemperature() != null ? dto.getTemperature() : new BigDecimal("0.20"));
        entity.setMaxTokens(dto.getMaxTokens() != null ? dto.getMaxTokens() : 2048);
        entity.setTimeoutSeconds(dto.getTimeoutSeconds() != null ? dto.getTimeoutSeconds() : 20);
        entity.setIsDefault(dto.getIsDefault());
        entity.setEnabled(dto.getEnabled());
        entity.setRemark(dto.getRemark());
    }

    private void clearDefault(Long exceptId) {
        LambdaUpdateWrapper<AiModelConfig> uw = new LambdaUpdateWrapper<>();
        uw.set(AiModelConfig::getIsDefault, 0);
        if (exceptId != null) {
            uw.ne(AiModelConfig::getId, exceptId);
        }
        update(uw);
    }

    private String mask(String key) {
        return (key == null || key.isBlank()) ? "" : MASK;
    }

    private int nz(Integer v, int def) {
        return v == null ? def : v;
    }

    private String rootMessage(Throwable e) {
        Throwable t = e;
        while (t.getCause() != null && t.getCause() != t) {
            t = t.getCause();
        }
        String msg = t.getMessage();
        return msg == null ? t.getClass().getSimpleName() : msg;
    }
}
