package com.example.meeting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.meeting.entity.ChatbotLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 智能问答日志Mapper
 */
@Mapper
public interface ChatbotLogMapper extends BaseMapper<ChatbotLog> {
}
