package com.wuyou.chat.api.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * AI 问答请求
 */
@Data
public class ChatRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 问题内容
     */
    @NotBlank(message = "问题内容不能为空")
    private String question;

    /**
     * 会话 ID（可选，用于保持上下文）
     */
    private String conversationId;
}
