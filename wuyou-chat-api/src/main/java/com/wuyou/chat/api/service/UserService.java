package com.wuyou.chat.api.service;

import com.wuyou.chat.api.dto.UserInfoResponse;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 根据 ID 获取用户信息
     *
     * @param userId 用户 ID
     * @return 用户信息
     */
    UserInfoResponse getUserInfo(Long userId);

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    UserInfoResponse getUserByUsername(String username);
}
