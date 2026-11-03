package com.example.meeting.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 智能问答日志实体
 */
@Data
@TableName("chatbot_log")
public class ChatbotLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 提问用户ID
     */
    private Long userId;

    /**
     * 用户问题
     */
    private String question;

    /**
     * 助手回答
     */
    private String answer;

    /**
     * 识别意图
     */
    private String intent;

    /**
     * 解析参数(JSON)
     */
    private String params;

    /**
     * 结果条数
     */
    private Integer resultCount;

    /**
     * 是否成功
     */
    private Integer success;

    /**
     * 耗时(毫秒)
     */
    private Long costMs;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
