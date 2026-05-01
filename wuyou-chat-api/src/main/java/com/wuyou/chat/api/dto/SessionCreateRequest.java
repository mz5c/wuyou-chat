package com.wuyou.chat.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建会话请求
 */
@Data
public class SessionCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话标题（可选，不指定时自动生成）
     */
    private String title;

    /**
     * 角色类型（可选，不指定时使用默认角色）
     */
    private String roleType;
}
