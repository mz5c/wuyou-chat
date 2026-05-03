package com.wuyou.chat.api.service;

import com.wuyou.chat.api.dto.UserInfoResponse;

/**
 * 用户资料服务接口
 */
public interface ProfileService {

    /**
     * 更新用户资料
     *
     * @param userId   用户 ID
     * @param nickname 昵称
     * @param email    邮箱
     * @param avatar   头像 URL
     * @return 更新后的用户信息
     */
    UserInfoResponse updateProfile(Long userId, String nickname, String email, String avatar);

    /**
     * 获取用户信息
     *
     * @param userId 用户 ID
     * @return 用户信息
     */
    UserInfoResponse getUserInfo(Long userId);

    /**
     * 修改密码
     *
     * @param userId      用户 ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);
}
