package com.wuyou.chat.service.controller;

import com.wuyou.chat.api.dto.ChangePasswordRequest;
import com.wuyou.chat.api.dto.Result;
import com.wuyou.chat.api.dto.UpdateProfileRequest;
import com.wuyou.chat.api.dto.UserInfoResponse;
import com.wuyou.chat.api.service.ProfileService;
import com.wuyou.chat.service.common.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户资料控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/profile")
@Tag(name = "用户资料接口", description = "用户资料更新、密码修改")
public class ProfileController {

    private final ProfileService profileService;
    private final JwtUtil jwtUtil;

    public ProfileController(ProfileService profileService, JwtUtil jwtUtil) {
        this.profileService = profileService;
        this.jwtUtil = jwtUtil;
    }

    @PutMapping("/update")
    @Operation(summary = "更新用户资料")
    public Result<UserInfoResponse> updateProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody @Validated UpdateProfileRequest request) {
        Long userId = getUserIdFromToken(token);
        UserInfoResponse userInfo = profileService.updateProfile(
                userId, request.getNickname(), request.getEmail(), request.getAvatar());
        return Result.success("资料更新成功", userInfo);
    }

    @PostMapping("/change-password")
    @Operation(summary = "修改密码")
    public Result<Void> changePassword(
            @RequestHeader("Authorization") String token,
            @RequestBody @Validated ChangePasswordRequest request) {
        Long userId = getUserIdFromToken(token);
        profileService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        return Result.success("密码修改成功", null);
    }

    /**
     * 从 Authorization header 中提取 userId
     */
    private Long getUserIdFromToken(String token) {
        String bearerToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        return jwtUtil.getUserIdFromToken(bearerToken);
    }
}
