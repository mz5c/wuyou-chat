package com.wuyou.chat.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话记录 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRecordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录 ID
     */
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
     * 思考过程（AI 的推理内容）
     */
    private String reasoningContent;

    /**
     * 会话 ID
     */
    private String conversationId;

    /**
     * 记录状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
