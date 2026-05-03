package com.wuyou.chat.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 对话记录实体
 */
@Data
@TableName("chat_record")
public class ChatRecord {

    /**
     * 记录 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 问题内容
     */
    private String question;

    /**
     * AI 回答
     */
    private String answer;

    /**
     * 会话 ID
     */
    private String conversationId;

    /** 会话 ID */
    private Long sessionId;

    /**
     * 使用的模型配置 ID
     */
    private Long modelId;

    /**
     * 记录状态：1-正常，0-删除
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
