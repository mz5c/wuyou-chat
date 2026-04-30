package com.wuyou.chat.service.controller;

import com.wuyou.chat.api.dto.LoginResponse;
import com.wuyou.chat.api.dto.Result;
import com.wuyou.chat.api.dto.UserLoginRequest;
import com.wuyou.chat.api.dto.UserRegisterRequest;
import com.wuyou.chat.api.dto.UserTokenRefreshRequest;
import com.wuyou.chat.api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证接口", description = "用户注册、登录、令牌刷新")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<LoginResponse> register(@RequestBody @Validated UserRegisterRequest request) {
        LoginResponse response = authService.register(request);
        return Result.success("注册成功", response);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<LoginResponse> login(@RequestBody @Validated UserLoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success("登录成功", response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌")
    public Result<LoginResponse> refresh(@RequestBody UserTokenRefreshRequest request) {
        LoginResponse response = authService.refresh(request.getToken());
        return Result.success("刷新成功", response);
    }
}
