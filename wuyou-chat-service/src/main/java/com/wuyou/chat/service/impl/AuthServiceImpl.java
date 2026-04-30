package com.wuyou.chat.service.impl;

import cn.hutool.core.util.StrUtil;
import com.wuyou.chat.api.dto.LoginResponse;
import com.wuyou.chat.api.dto.UserLoginRequest;
import com.wuyou.chat.api.dto.UserRegisterRequest;
import com.wuyou.chat.api.service.AuthService;
import com.wuyou.chat.service.common.JwtUtil;
import com.wuyou.chat.service.entity.User;
import com.wuyou.chat.service.exception.BusinessException;
import com.wuyou.chat.service.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务实现
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse register(UserRegisterRequest request) {
        // 检查用户名是否已存在
        User existingUser = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername()));
        if (existingUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (StrUtil.isNotBlank(request.getEmail())) {
            User existingUserWithEmail = userMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                            .eq(User::getEmail, request.getEmail()));
            if (existingUserWithEmail != null) {
                throw new BusinessException("邮箱已被注册");
            }
        }

        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setNickname(request.getUsername()); // 默认昵称与用户名相同
        user.setStatus(1);

        userMapper.insert(user);
        log.info("用户注册成功：{}", user.getUsername());

        // 生成令牌
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        // 这里简化处理，实际项目应将 refreshToken 存储到 Redis
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn((jwtUtil.getExpirationFromToken(accessToken).getTime() - System.currentTimeMillis()) / 1000)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .build();
    }

    @Override
    public LoginResponse login(UserLoginRequest request) {
        // 查找用户
        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername()));
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 检查账户状态
        if (user.getStatus() != 1) {
            throw new BusinessException("账户已被禁用");
        }

        // 生成令牌
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        log.info("用户登录成功：{}", user.getUsername());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn((jwtUtil.getExpirationFromToken(accessToken).getTime() - System.currentTimeMillis()) / 1000)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .build();
    }

    @Override
    public LoginResponse refresh(String refreshToken) {
        // 验证刷新令牌
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException("刷新令牌无效或已过期");
        }

        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        String username = jwtUtil.getUsernameFromToken(refreshToken);

        // 查找用户
        User user = userMapper.selectById(userId);
        if (user == null || user.getStatus() != 1) {
            throw new BusinessException("用户不存在或已被禁用");
        }

        // 生成新的访问令牌
        String newAccessToken = jwtUtil.generateAccessToken(userId, username);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn((jwtUtil.getExpirationFromToken(newAccessToken).getTime() - System.currentTimeMillis()) / 1000)
                .userId(userId)
                .username(username)
                .nickname(user.getNickname())
                .build();
    }
}
