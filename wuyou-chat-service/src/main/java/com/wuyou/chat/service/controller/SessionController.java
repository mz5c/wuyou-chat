package com.wuyou.chat.service.controller;

import com.wuyou.chat.api.dto.*;
import com.wuyou.chat.api.service.SessionService;
import com.wuyou.chat.service.common.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会话管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/session")
@Tag(name = "会话管理接口", description = "会话创建、列表、详情、重命名、角色切换、删除")
public class SessionController {

    private final SessionService sessionService;
    private final JwtUtil jwtUtil;

    public SessionController(SessionService sessionService, JwtUtil jwtUtil) {
        this.sessionService = sessionService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/create")
    @Operation(summary = "创建会话")
    public Result<SessionDTO> create(@RequestHeader("Authorization") String token,
                                     @RequestBody SessionCreateRequest request) {
        Long userId = getUserId(token);
        SessionDTO session = sessionService.createSession(userId, request.getTitle(), request.getRoleType(), request.getModelId());
        return Result.success("创建成功", session);
    }

    @GetMapping("/list")
    @Operation(summary = "获取会话列表")
    public Result<List<SessionDTO>> list(@RequestHeader("Authorization") String token) {
        Long userId = getUserId(token);
        List<SessionDTO> sessions = sessionService.listSessions(userId);
        return Result.success(sessions);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取会话详情")
    public Result<SessionDTO> get(@RequestHeader("Authorization") String token,
                                  @PathVariable Long id) {
        Long userId = getUserId(token);
        SessionDTO session = sessionService.getSession(userId, id);
        return Result.success(session);
    }

    @PutMapping("/{id}/rename")
    @Operation(summary = "重命名会话")
    public Result<SessionDTO> rename(@RequestHeader("Authorization") String token,
                                     @PathVariable Long id,
                                     @RequestBody @Validated SessionRenameRequest request) {
        Long userId = getUserId(token);
        SessionDTO session = sessionService.renameSession(userId, id, request.getTitle());
        return Result.success("重命名成功", session);
    }

    @PutMapping("/{id}/role")
    @Operation(summary = "切换会话角色")
    public Result<SessionDTO> updateRole(@RequestHeader("Authorization") String token,
                                         @PathVariable Long id,
                                         @RequestBody SessionRenameRequest request) {
        Long userId = getUserId(token);
        SessionDTO session = sessionService.updateRole(userId, id, request.getTitle());
        return Result.success("切换角色成功", session);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除会话")
    public Result<Void> delete(@RequestHeader("Authorization") String token,
                               @PathVariable Long id) {
        Long userId = getUserId(token);
        sessionService.deleteSession(userId, id);
        return Result.success();
    }

    /**
     * 从 Authorization header 中提取 userId
     */
    private Long getUserId(String token) {
        String bearerToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        return jwtUtil.getUserIdFromToken(bearerToken);
    }
}
