package com.wuyou.chat.api.service;

import com.wuyou.chat.api.dto.ChatRequest;
import com.wuyou.chat.api.dto.ChatResponse;
import com.wuyou.chat.api.dto.ChatRecordDTO;

import java.util.List;

/**
 * AI 聊天服务接口
 */
public interface AiChatService {

    /**
     * AI 问答
     *
     * @param userId 用户 ID
     * @param request 问答请求
     * @return 问答响应
     */
    ChatResponse ask(Long userId, ChatRequest request);

    /**
     * 获取对话历史
     *
     * @param userId 用户 ID
     * @param page 页码
     * @param size 每页大小
     * @return 对话记录列表
     */
    List<ChatRecordDTO> getHistory(Long userId, Integer page, Integer size);

    /**
     * 获取单条对话
     *
     * @param userId 用户 ID
     * @param recordId 记录 ID
     * @return 对话记录
     */
    ChatRecordDTO getRecord(Long userId, Long recordId);

    /**
     * 删除对话记录
     *
     * @param userId 用户 ID
     * @param recordId 记录 ID
     */
    void deleteRecord(Long userId, Long recordId);
}
