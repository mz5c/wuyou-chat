package com.wuyou.chat.api.dto;

import lombok.Data;

/**
 * 令牌刷新请求
 */
@Data
public class UserTokenRefreshRequest {

    /**
     * 刷新令牌
     */
    private String token;
}
