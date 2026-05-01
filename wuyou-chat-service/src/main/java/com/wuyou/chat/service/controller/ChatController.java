package com.wuyou.chat.service.controller;

import com.wuyou.chat.api.dto.ChatRequest;
import com.wuyou.chat.api.dto.ChatResponse;
import com.wuyou.chat.api.dto.ChatRecordDTO;
import com.wuyou.chat.api.dto.ChatStreamRequest;
import com.wuyou.chat.api.dto.Result;
import com.wuyou.chat.api.service.AiChatService;
import com.wuyou.chat.service.common.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 聊天控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@Tag(name = "AI 聊天接口", description = "AI 问答、对话历史管理")
public class ChatController {

    private final AiChatService aiChatService;
    private final JwtUtil jwtUtil;

    public ChatController(AiChatService aiChatService, JwtUtil jwtUtil) {
        this.aiChatService = aiChatService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/ask")
    @Operation(summary = "AI 问答")
    public Result<ChatResponse> ask(@RequestHeader("Authorization") String token,
                                     @RequestBody @Validated ChatRequest request) {
        Long userId = getUserIdFromToken(token);
        ChatResponse response = aiChatService.ask(userId, request);
        return Result.success("问答成功", response);
    }

    @PostMapping("/ask/stream")
    @Operation(summary = "流式 AI 问答")
    public SseEmitter askStream(@RequestHeader("Authorization") String token,
                                 @RequestBody @Validated ChatStreamRequest request) {
        Long userId = getUserIdFromToken(token);
        return (SseEmitter) aiChatService.askStream(userId, request.getSessionId(), request.getMessage());
    }

    @GetMapping("/record/list/{sessionId}")
    @Operation(summary = "按会话获取聊天记录")
    public Result<List<ChatRecordDTO>> getHistoryBySession(@RequestHeader("Authorization") String token,
                                                            @PathVariable Long sessionId) {
        Long userId = getUserIdFromToken(token);
        List<ChatRecordDTO> records = aiChatService.getHistoryBySession(userId, sessionId);
        return Result.success(records);
    }

    @GetMapping("/history")
    @Operation(summary = "获取对话历史")
    public Result<List<ChatRecordDTO>> getHistory(@RequestHeader("Authorization") String token,
                                                   @RequestParam(defaultValue = "1") Integer page,
                                                   @RequestParam(defaultValue = "20") Integer size) {
        Long userId = getUserIdFromToken(token);
        List<ChatRecordDTO> history = aiChatService.getHistory(userId, page, size);
        return Result.success(history);
    }

    @GetMapping("/record/{id}")
    @Operation(summary = "获取单条对话记录")
    public Result<ChatRecordDTO> getRecord(@RequestHeader("Authorization") String token,
                                           @PathVariable Long id) {
        Long userId = getUserIdFromToken(token);
        ChatRecordDTO record = aiChatService.getRecord(userId, id);
        return Result.success(record);
    }

    @DeleteMapping("/record/{id}")
    @Operation(summary = "删除对话记录")
    public Result<Void> deleteRecord(@RequestHeader("Authorization") String token,
                                     @PathVariable Long id) {
        Long userId = getUserIdFromToken(token);
        aiChatService.deleteRecord(userId, id);
        return Result.success();
    }

    /**
     * 从 Authorization header 中提取 userId
     */
    private Long getUserIdFromToken(String token) {
        String bearerToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        return jwtUtil.getUserIdFromToken(bearerToken);
    }
}
