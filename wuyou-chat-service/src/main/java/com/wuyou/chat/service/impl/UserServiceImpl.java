package com.wuyou.chat.service.impl;

import com.wuyou.chat.api.dto.UserInfoResponse;
import com.wuyou.chat.api.service.UserService;
import com.wuyou.chat.service.entity.User;
import com.wuyou.chat.service.exception.BusinessException;
import com.wuyou.chat.service.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
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
    public UserInfoResponse getUserByUsername(String username) {
        User user = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToUserInfoResponse(user);
    }

    /**
     * 查找用户（内部方法）
     */
    public User findByUsername(String username) {
        return userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
    }

    /**
     * 转换为用户信息响应
     */
    private UserInfoResponse convertToUserInfoResponse(User user) {
        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
