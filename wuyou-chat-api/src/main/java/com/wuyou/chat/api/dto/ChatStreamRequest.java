package com.wuyou.chat.api.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 流式对话请求
 */
@Data
public class ChatStreamRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话 ID
     */
    @NotNull(message = "会话 ID 不能为空")
    private Long sessionId;

    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String message;

    /**
     * 使用的 AI 模型配置 ID，不传则使用默认
     */
    private Long modelId;
}
