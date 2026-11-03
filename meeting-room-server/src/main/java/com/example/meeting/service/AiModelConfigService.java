package com.example.meeting.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.meeting.dto.AiModelConfigDTO;
import com.example.meeting.entity.AiModelConfig;

import java.util.List;
import java.util.Map;

/**
 * AI模型配置服务
 */
public interface AiModelConfigService extends IService<AiModelConfig> {

    /**
     * 配置列表（密钥脱敏）
     */
    List<AiModelConfig> listAll();

    /**
     * 新增配置
     */
    AiModelConfig create(AiModelConfigDTO dto);

    /**
     * 更新配置
     */
    AiModelConfig update(AiModelConfigDTO dto);

    /**
     * 删除配置
     */
    void delete(Long id);

    /**
     * 设为默认（同时启用）
     */
    void setDefault(Long id);

    /**
     * 启用/停用
     */
    void toggle(Long id, Integer enabled);

    /**
     * 获取当前生效的模型配置（默认且启用；否则取第一个启用的）
     */
    AiModelConfig getActive();

    /**
     * 测试连通性
     */
    Map<String, Object> testConnection(Long id);
}
