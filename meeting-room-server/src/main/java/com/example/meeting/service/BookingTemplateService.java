package com.example.meeting.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.meeting.entity.BookingTemplate;

import java.util.List;

/**
 * 会议模板服务接口
 */
public interface BookingTemplateService extends IService<BookingTemplate> {

    /**
     * 获取用户的模板列表
     */
    List<BookingTemplate> getMyTemplates(Long userId);

    /**
     * 创建模板
     */
    BookingTemplate createTemplate(Long userId, BookingTemplate template);

    /**
     * 更新模板
     */
    void updateTemplate(Long userId, Long templateId, BookingTemplate template);

    /**
     * 删除模板
     */
    void deleteTemplate(Long userId, Long templateId);
}
