package com.wuyou.chat.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wuyou.chat.api.dto.UserInfoResponse;
import com.wuyou.chat.api.service.ProfileService;
import com.wuyou.chat.service.entity.User;
import com.wuyou.chat.service.exception.BusinessException;
import com.wuyou.chat.service.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户资料服务实现
 */
@Slf4j
@Service
public class ProfileServiceImpl implements ProfileService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public ProfileServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoResponse updateProfile(Long userId, String nickname, String email, String avatar) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        boolean updated = false;

        if (StrUtil.isNotBlank(nickname) && !nickname.equals(user.getNickname())) {
            user.setNickname(nickname);
            updated = true;
        }

        if (StrUtil.isNotBlank(email) && !email.equals(user.getEmail())) {
            User existingUserWithEmail = userMapper.selectOne(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getEmail, email));
            if (existingUserWithEmail != null && !existingUserWithEmail.getId().equals(userId)) {
                throw new BusinessException("邮箱已被其他用户使用");
            }
            user.setEmail(email);
            updated = true;
        }

        if (StrUtil.isNotBlank(avatar)) {
            user.setAvatar(avatar);
            updated = true;
        }

        if (updated) {
            userMapper.updateById(user);
            log.info("用户资料更新成功：userId={}", userId);
        }

        return convertToUserInfoResponse(user);
    }

    @Override
    public UserInfoResponse getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToUserInfoResponse(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        log.info("密码修改成功：userId={}", userId);
    }

    private UserInfoResponse convertToUserInfoResponse(User user) {
        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
