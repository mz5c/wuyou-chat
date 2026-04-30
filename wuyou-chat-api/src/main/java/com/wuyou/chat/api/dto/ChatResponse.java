package com.wuyou.chat.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * AI 问答响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话 ID
     */
    private String conversationId;

    /**
     * AI 回答
     */
    private String answer;

    /**
     * 思考过程（AI 的推理内容，与最终回答分开）
     */
    private String reasoningContent;

    /**
     * 是否是新会话
     */
    private Boolean isNewConversation;
}
