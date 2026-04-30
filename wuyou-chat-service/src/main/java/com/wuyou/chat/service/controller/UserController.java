package com.wuyou.chat.service.controller;

import com.wuyou.chat.api.dto.Result;
import com.wuyou.chat.api.dto.UserInfoResponse;
import com.wuyou.chat.api.service.UserService;
import com.wuyou.chat.service.common.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@Tag(name = "用户接口", description = "用户信息查询")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息")
    public Result<UserInfoResponse> getCurrentUserInfo(@RequestHeader("Authorization") String token) {
        String bearerToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        Long userId = jwtUtil.getUserIdFromToken(bearerToken);
        UserInfoResponse userInfo = userService.getUserInfo(userId);
        return Result.success(userInfo);
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "根据用户名获取用户信息")
    public Result<UserInfoResponse> getUserByUsername(@PathVariable String username) {
        UserInfoResponse userInfo = userService.getUserByUsername(username);
        return Result.success(userInfo);
    }
}
