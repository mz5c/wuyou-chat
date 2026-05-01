package com.wuyou.chat.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.wuyou.chat.api.dto.ChatRequest;
import com.wuyou.chat.api.dto.ChatResponse;
import com.wuyou.chat.api.dto.ChatRecordDTO;
import com.wuyou.chat.api.service.AiChatService;
import com.wuyou.chat.service.entity.ChatRecord;
import com.wuyou.chat.service.entity.User;
import com.wuyou.chat.service.exception.BusinessException;
import com.wuyou.chat.service.mapper.ChatRecordMapper;
import com.wuyou.chat.service.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.wuyou.chat.api.enums.RoleType;
import com.wuyou.chat.service.entity.ChatSession;
import com.wuyou.chat.service.mapper.ChatSessionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 聊天服务实现
 */
@Slf4j
@Service
public class AiChatServiceImpl implements AiChatService {

    private final ChatRecordMapper chatRecordMapper;
    private final UserMapper userMapper;
    private final ChatSessionMapper chatSessionMapper;
    private final WebClient webClient;

    @Value("${ai.provider.api-url:}")
    private String apiUrl;

    @Value("${ai.provider.api-key:}")
    private String apiKey;

    @Value("${ai.provider.model:gpt-3.5-turbo}")
    private String model;

    public AiChatServiceImpl(ChatRecordMapper chatRecordMapper, UserMapper userMapper, ChatSessionMapper chatSessionMapper) {
        this.chatRecordMapper = chatRecordMapper;
        this.userMapper = userMapper;
        this.chatSessionMapper = chatSessionMapper;
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
    }

    @Override
    public ChatResponse ask(Long userId, ChatRequest request) {
        // 验证用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 生成或获取会话 ID
        String conversationId = StrUtil.isNotBlank(request.getConversationId())
                ? request.getConversationId()
                : UUID.randomUUID().toString().replace("-", "");

        boolean isNewConversation = StrUtil.isBlank(request.getConversationId());

        // 调用 AI API
        ChatResponse aiResponse = callAiApi(request.getQuestion(), conversationId, userId, null);

        // 保存对话记录（保留完整原始内容）
        ChatRecord record = new ChatRecord();
        record.setUserId(userId);
        record.setQuestion(request.getQuestion());
        record.setAnswer(aiResponse.getAnswer());
        record.setConversationId(conversationId);
        record.setStatus(1);
        record.setCreatedAt(LocalDateTime.now());

        chatRecordMapper.insert(record);

        // 自动更新会话标题（首次对话时）
        Long sessionId = null; // 当前非流式接口暂不传递 sessionId
        if (isNewConversation && sessionId != null) {
            String title = request.getQuestion().length() > 30
                    ? request.getQuestion().substring(0, 30) + "..."
                    : request.getQuestion();
            ChatSession session = chatSessionMapper.selectById(sessionId);
            if (session != null && "新对话".equals(session.getTitle())) {
                session.setTitle(title);
                session.setUpdatedAt(LocalDateTime.now());
                chatSessionMapper.updateById(session);
            }
        }

        log.info("AI 问答完成，用户：{}, 会话：{}", userId, conversationId);

        return ChatResponse.builder()
                .conversationId(conversationId)
                .answer(aiResponse.getAnswer())
                .reasoningContent(aiResponse.getReasoningContent())
                .isNewConversation(isNewConversation)
                .build();
    }

    @Override
    public List<ChatRecordDTO> getHistory(Long userId, Integer page, Integer size) {
        // 验证用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 计算偏移量
        int offset = (page - 1) * size;

        // 查询对话记录（按创建时间倒序）
        List<ChatRecord> records = chatRecordMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatRecord>()
                        .eq(ChatRecord::getUserId, userId)
                        .eq(ChatRecord::getStatus, 1)
                        .orderByDesc(ChatRecord::getCreatedAt)
                        .last("LIMIT " + offset + ", " + size));

        return records.stream()
                .map(this::convertToChatRecordDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ChatRecordDTO getRecord(Long userId, Long recordId) {
        ChatRecord record = chatRecordMapper.selectById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException("对话记录不存在");
        }
        return convertToChatRecordDTO(record);
    }

    @Override
    public void deleteRecord(Long userId, Long recordId) {
        ChatRecord record = chatRecordMapper.selectById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException("对话记录不存在");
        }
        // 软删除
        record.setStatus(0);
        chatRecordMapper.updateById(record);
    }

    @Override
    public List<ChatRecordDTO> getHistoryBySession(Long userId, Long sessionId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        List<ChatRecord> records = chatRecordMapper.selectList(
                new LambdaQueryWrapper<ChatRecord>()
                        .eq(ChatRecord::getUserId, userId)
                        .eq(ChatRecord::getSessionId, sessionId)
                        .eq(ChatRecord::getStatus, 1)
                        .orderByAsc(ChatRecord::getCreatedAt));
        return records.stream().map(this::convertToChatRecordDTO).collect(Collectors.toList());
    }

    // 匹配 DeepSeek R1 风格 或通用 <reasoning> 标签
    private static final Pattern REASONING_PATTERN = Pattern.compile(
            "\\s*<reasoning>([\\s\\S]*?)</reasoning>\\s*" +
            "|\\s*<think>([\\s\\S]*?)</think>\\s*",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 调用 AI API
     */
    private ChatResponse callAiApi(String question, String conversationId, Long userId, String systemPrompt) {
        // 如果没有配置 API 地址，返回模拟响应
        if (StrUtil.isBlank(apiUrl)) {
            log.warn("AI API 未配置，返回模拟响应");
            log.info("AI 配置检查 - URL: {}, Model: {}, API Key 配置：{}",
                    apiUrl != null ? apiUrl : "未配置",
                    model,
                    (apiKey != null && !apiKey.isEmpty()) ? "已配置" : "未配置");
            String simulated = "您好！我是 AI 助手。您问我：" + question + "\n\n注意：请在 application.yml 中配置 AI API 来使用真实的 AI 服务。";
            return ChatResponse.builder().answer(simulated).build();
        }

        try {
            // 构建 messages 数组 (OpenAI 格式)
            cn.hutool.json.JSONArray messages = new cn.hutool.json.JSONArray();

            // 添加系统提示词（如果存在）
            if (StrUtil.isNotBlank(systemPrompt)) {
                JSONObject systemMsg = new JSONObject();
                systemMsg.set("role", "system");
                systemMsg.set("content", systemPrompt);
                messages.add(systemMsg);
            }

            // 如果存在 conversationId，加载历史记录作为上下文
            if (StrUtil.isNotBlank(conversationId)) {
                List<ChatRecord> historyRecords = chatRecordMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatRecord>()
                                .eq(ChatRecord::getConversationId, conversationId)
                                .eq(ChatRecord::getUserId, userId)
                                .eq(ChatRecord::getStatus, 1)
                                .orderByDesc(ChatRecord::getCreatedAt)
                                .last("LIMIT 10"));

                // 反转成升序，保证 messages 数组时间正序
                Collections.reverse(historyRecords);

                for (ChatRecord record : historyRecords) {
                    JSONObject userMsg = new JSONObject();
                    userMsg.set("role", "user");
                    userMsg.set("content", record.getQuestion());
                    messages.add(userMsg);

                    JSONObject aiMsg = new JSONObject();
                    aiMsg.set("role", "assistant");
                    aiMsg.set("content", record.getAnswer());
                    messages.add(aiMsg);
                }
            }

            // 添加当前问题
            JSONObject currentMsg = new JSONObject();
            currentMsg.set("role", "user");
            currentMsg.set("content", question);
            messages.add(currentMsg);

            JSONObject body = new JSONObject();
            body.set("model", model);
            body.set("messages", messages);
            body.set("temperature", 0.7);

            log.info("调用 AI API: {}", apiUrl);

            // 发送请求（设置 30 秒超时）
            String response = webClient.post()
                    .uri(apiUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(body.toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            // 解析响应
            if (StrUtil.isNotBlank(response)) {
                JSONObject jsonResponse = JSONUtil.parseObj(response);
                cn.hutool.json.JSONArray choices = jsonResponse.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JSONObject firstChoice = choices.getJSONObject(0);
                    JSONObject message = firstChoice.getJSONObject("message");
                    String content = message != null ? message.getStr("content") : null;
                    if (StrUtil.isNotBlank(content)) {
                        log.info("AI 回答成功");
                        return parseReasoningContent(content);
                    }
                }
                // 如果解析失败，返回原始响应
                log.warn("AI 响应解析失败：{}", response);
            }

            return ChatResponse.builder().answer("抱歉，AI 服务响应异常，请稍后重试。").build();

        } catch (WebClientResponseException e) {
            log.error("AI API 请求失败：{} {}", e.getStatusCode(), e.getResponseBodyAsString());
            log.error("AI API 配置信息 - URL: {}, Model: {}", apiUrl, model);
            return ChatResponse.builder().answer("AI 服务暂时不可用，请稍后重试。").build();
        } catch (Exception e) {
            log.error("调用 AI API 失败", e);
            return ChatResponse.builder().answer("AI 服务暂时不可用，请稍后重试。").build();
        }
    }

    /**
     * 从 AI 原始响应中解析出思考内容和最终回答
     */
    private ChatResponse parseReasoningContent(String rawResponse) {
        Matcher matcher = REASONING_PATTERN.matcher(rawResponse);
        String reasoningContent = null;
        if (matcher.find()) {
            reasoningContent = matcher.group(1) != null ? matcher.group(1).trim() : matcher.group(2).trim();
        }

        // 移除 reasoning/think 标签，得到纯净的回答内容
        String cleanAnswer = REASONING_PATTERN.matcher(rawResponse).replaceAll("").trim();
        if (cleanAnswer.isEmpty()) {
            cleanAnswer = rawResponse;
        }

        return ChatResponse.builder()
                .answer(cleanAnswer)
                .reasoningContent(reasoningContent)
                .build();
    }

    @Override
    public SseEmitter askStream(Long userId, Long sessionId, String message) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");

        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId) || session.getStatus() == 0) {
            throw new BusinessException("会话不存在");
        }

        String systemPrompt;
        try {
            systemPrompt = RoleType.valueOf(session.getRoleType()).getSystemPrompt();
        } catch (IllegalArgumentException e) {
            systemPrompt = null;
        }
        final String sp = systemPrompt;

        SseEmitter emitter = new SseEmitter(300_000L);

        CompletableFuture.runAsync(() -> {
            try {
                // Build messages with history
                cn.hutool.json.JSONArray messages = new cn.hutool.json.JSONArray();
                if (StrUtil.isNotBlank(sp)) {
                    JSONObject systemMsg = new JSONObject();
                    systemMsg.set("role", "system");
                    systemMsg.set("content", sp);
                    messages.add(systemMsg);
                }

                // Load history (up to 10 records)
                List<ChatRecord> history = chatRecordMapper.selectList(
                        new LambdaQueryWrapper<ChatRecord>()
                                .eq(ChatRecord::getSessionId, sessionId)
                                .eq(ChatRecord::getStatus, 1)
                                .orderByAsc(ChatRecord::getCreatedAt)
                                .last("LIMIT 10"));
                for (ChatRecord record : history) {
                    JSONObject um = new JSONObject();
                    um.set("role", "user");
                    um.set("content", record.getQuestion());
                    messages.add(um);
                    JSONObject am = new JSONObject();
                    am.set("role", "assistant");
                    am.set("content", record.getAnswer());
                    messages.add(am);
                }

                // Add current question
                JSONObject currentMsg = new JSONObject();
                currentMsg.set("role", "user");
                currentMsg.set("content", message);
                messages.add(currentMsg);

                StringBuilder fullContent = new StringBuilder();

                if (StrUtil.isBlank(apiUrl)) {
                    // Mock streaming
                    String simulated = "您好！我是 AI 助手。\n\n您问我：" + message;
                    for (int i = 0; i < simulated.length(); i += 5) {
                        int end = Math.min(i + 5, simulated.length());
                        String chunk = simulated.substring(i, end);
                        JSONObject data = new JSONObject();
                        data.set("content", chunk);
                        data.set("type", "text");
                        emitter.send(SseEmitter.event().name("message").data(data.toString()));
                        fullContent.append(chunk);
                        Thread.sleep(50);
                    }
                } else {
                    callAiApiStream(emitter, messages, fullContent);
                }

                // Save chat record after streaming completes
                ChatRecord record = new ChatRecord();
                record.setUserId(userId);
                record.setSessionId(sessionId);
                record.setQuestion(message);
                record.setAnswer(fullContent.toString());
                record.setConversationId(UUID.randomUUID().toString().replace("-", ""));
                record.setStatus(1);
                record.setCreatedAt(LocalDateTime.now());
                chatRecordMapper.insert(record);

                // Auto-title if first message
                if ("新对话".equals(session.getTitle())) {
                    String title = message.length() > 30 ? message.substring(0, 30) + "..." : message;
                    session.setTitle(title);
                    session.setUpdatedAt(LocalDateTime.now());
                    chatSessionMapper.updateById(session);
                }

                emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                emitter.complete();

            } catch (IllegalStateException e) {
                log.debug("SSE 流式处理中断（客户端断开连接）");
                emitter.complete();
            } catch (Exception e) {
                log.error("SSE 流式处理失败", e);
                try {
                    emitter.send(SseEmitter.event().name("error").data("AI 服务暂时不可用"));
                } catch (IOException ex) { /* ignore */ }
                emitter.completeWithError(e);
            }
        });

        emitter.onTimeout(() -> {
            log.warn("SSE 连接超时");
            emitter.complete();
        });

        return emitter;
    }

    private void callAiApiStream(SseEmitter emitter, cn.hutool.json.JSONArray messages, StringBuilder fullContent) {
        try {
            JSONObject body = new JSONObject();
            body.set("model", model);
            body.set("messages", messages);
            body.set("temperature", 0.7);
            body.set("stream", true);

            webClient.post()
                    .uri(apiUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(body.toString())
                    .retrieve()
                    .bodyToFlux(String.class)
                    .subscribe(
                            chunk -> {
                                try {
                                    String line = chunk.trim();
                                    if (line.isEmpty()) return;
                                    if ("[DONE]".equals(line)) return;
                                    if (line.startsWith("data: ")) {
                                        line = line.substring(6).trim();
                                    }
                                    if (line.isEmpty() || "[DONE]".equals(line)) return;
                                    JSONObject json = JSONUtil.parseObj(line);
                                    cn.hutool.json.JSONArray choices = json.getJSONArray("choices");
                                    if (choices != null && !choices.isEmpty()) {
                                        JSONObject delta = choices.getJSONObject(0).getJSONObject("delta");
                                        String content = delta != null ? delta.getStr("content") : null;
                                        if (StrUtil.isNotBlank(content)) {
                                            fullContent.append(content);
                                            JSONObject data = new JSONObject();
                                            data.set("content", content);
                                            data.set("type", "text");
                                            emitter.send(SseEmitter.event().name("message").data(data.toString()));
                                        }
                                    }
                                } catch (IllegalStateException e) {
                                    log.debug("SSE 连接已关闭（客户端断开）");
                                } catch (Exception e) {
                                    log.error("SSE 发送失败", e);
                                }
                            },
                            error -> {
                                log.error("AI 流式 API 错误", error);
                                try {
                                    emitter.send(SseEmitter.event().name("error").data("AI 服务错误"));
                                } catch (IOException e) { /* ignore */ }
                                emitter.completeWithError(error);
                            },
                            () -> emitter.complete()
                    );
        } catch (Exception e) {
            log.error("调用 AI 流式 API 失败", e);
            emitter.completeWithError(e);
        }
    }

    /**
     * 转换为 ChatRecordDTO
     */
    private ChatRecordDTO convertToChatRecordDTO(ChatRecord record) {
        // 从存储的完整回答中解析思考内容（历史记录展示用）
        ChatResponse parsed = parseReasoningContent(record.getAnswer());
        return ChatRecordDTO.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .question(record.getQuestion())
                .answer(parsed.getAnswer())
                .reasoningContent(parsed.getReasoningContent())
                .conversationId(record.getConversationId())
                .status(record.getStatus())
                .createdAt(record.getCreatedAt())
                .build();
    }
}
