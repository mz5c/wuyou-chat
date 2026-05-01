package com.wuyou.chat.api.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 重命名会话请求
 */
@Data
public class SessionRenameRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 新会话标题
     */
    @NotBlank(message = "会话标题不能为空")
    private String title;
}
