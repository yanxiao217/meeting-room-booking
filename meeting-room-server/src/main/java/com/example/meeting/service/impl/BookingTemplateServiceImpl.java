package com.example.meeting.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.meeting.entity.BookingTemplate;
import com.example.meeting.mapper.BookingTemplateMapper;
import com.example.meeting.service.BookingTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 会议模板服务实现类
 */
@Service
public class BookingTemplateServiceImpl extends ServiceImpl<BookingTemplateMapper, BookingTemplate> implements BookingTemplateService {

    @Autowired
    private BookingTemplateMapper bookingTemplateMapper;

    @Override
    public List<BookingTemplate> getMyTemplates(Long userId) {
        return bookingTemplateMapper.selectByUserId(userId);
    }

    @Override
    public BookingTemplate createTemplate(Long userId, BookingTemplate template) {
        template.setUserId(userId);
        save(template);
        return template;
    }

    @Override
    public void updateTemplate(Long userId, Long templateId, BookingTemplate template) {
        lambdaUpdate()
                .eq(BookingTemplate::getId, templateId)
                .eq(BookingTemplate::getUserId, userId)
                .set(BookingTemplate::getTemplateName, template.getTemplateName())
                .set(BookingTemplate::getRoomId, template.getRoomId())
                .set(BookingTemplate::getStartTime, template.getStartTime())
                .set(BookingTemplate::getEndTime, template.getEndTime())
                .set(BookingTemplate::getTopic, template.getTopic())
                .set(BookingTemplate::getAttendeeCount, template.getAttendeeCount())
                .set(BookingTemplate::getEquipmentNeed, template.getEquipmentNeed())
                .set(BookingTemplate::getRemark, template.getRemark())
                .update();
    }

    @Override
    public void deleteTemplate(Long userId, Long templateId) {
        lambdaUpdate()
                .eq(BookingTemplate::getId, templateId)
                .eq(BookingTemplate::getUserId, userId)
                .remove();
    }
}
