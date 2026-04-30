package com.wuyou.chat.api.service;

import com.wuyou.chat.api.dto.UserRegisterRequest;
import com.wuyou.chat.api.dto.UserLoginRequest;
import com.wuyou.chat.api.dto.LoginResponse;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 登录响应
     */
    LoginResponse register(UserRegisterRequest request);

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    LoginResponse login(UserLoginRequest request);

    /**
     * 刷新 Token
     *
     * @param refreshToken 刷新令牌
     * @return 新的登录响应
     */
    LoginResponse refresh(String refreshToken);
}
