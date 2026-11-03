package com.example.meeting.service;

import com.example.meeting.common.result.PageResult;
import com.example.meeting.entity.ChatbotLog;
import com.example.meeting.vo.ChatBotAnswerVO;

/**
 * 智能问答助手服务
 */
public interface ChatBotService {

    /**
     * 提问并获取回答
     *
     * @param userId   当前用户
     * @param question 用户问题
     */
    ChatBotAnswerVO ask(Long userId, String question);

    /**
     * 管理端：问答日志分页
     */
    PageResult<ChatbotLog> getLogs(String intent, Integer success, Integer pageNum, Integer pageSize);
}
