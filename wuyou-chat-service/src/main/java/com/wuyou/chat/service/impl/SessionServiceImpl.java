package com.wuyou.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wuyou.chat.api.dto.SessionDTO;
import com.wuyou.chat.api.enums.RoleType;
import com.wuyou.chat.api.service.SessionService;
import com.wuyou.chat.service.entity.ChatSession;
import com.wuyou.chat.service.entity.User;
import com.wuyou.chat.service.exception.BusinessException;
import com.wuyou.chat.service.mapper.ChatSessionMapper;
import com.wuyou.chat.service.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 会话服务实现
 */
@Slf4j
@Service
public class SessionServiceImpl implements SessionService {

    private final ChatSessionMapper sessionMapper;
    private final UserMapper userMapper;

    public SessionServiceImpl(ChatSessionMapper sessionMapper, UserMapper userMapper) {
        this.sessionMapper = sessionMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public SessionDTO createSession(Long userId, String title, String roleType, Long modelId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        String role = (roleType != null && !roleType.isBlank()) ? roleType.toUpperCase() : RoleType.GENERAL.name();
        String sessionTitle = (title != null && !title.isBlank()) ? title : "新对话";

        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setTitle(sessionTitle);
        session.setRoleType(role);
        session.setModelId(modelId);
        session.setStatus(1);
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        sessionMapper.insert(session);

        log.info("创建会话成功，用户：{}，会话：{}", userId, session.getId());
        return toDTO(session);
    }

    @Override
    public SessionDTO getSession(Long userId, Long sessionId) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId) || session.getStatus() == 0) {
            throw new BusinessException("会话不存在");
        }
        return toDTO(session);
    }

    @Override
    public List<SessionDTO> listSessions(Long userId) {
        List<ChatSession> sessions = sessionMapper.selectList(
                new LambdaQueryWrapper<ChatSession>()
                        .eq(ChatSession::getUserId, userId)
                        .eq(ChatSession::getStatus, 1)
                        .orderByDesc(ChatSession::getUpdatedAt));
        return sessions.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SessionDTO renameSession(Long userId, Long sessionId, String title) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId) || session.getStatus() == 0) {
            throw new BusinessException("会话不存在");
        }
        session.setTitle(title);
        session.setUpdatedAt(LocalDateTime.now());
        sessionMapper.updateById(session);
        return toDTO(session);
    }

    @Override
    @Transactional
    public SessionDTO updateRole(Long userId, Long sessionId, String roleType) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId) || session.getStatus() == 0) {
            throw new BusinessException("会话不存在");
        }
        session.setRoleType(roleType.toUpperCase());
        session.setUpdatedAt(LocalDateTime.now());
        sessionMapper.updateById(session);
        return toDTO(session);
    }

    @Override
    @Transactional
    public void deleteSession(Long userId, Long sessionId) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new BusinessException("会话不存在");
        }
        session.setStatus(0);
        sessionMapper.updateById(session);
        log.info("删除会话成功，用户：{}，会话：{}", userId, sessionId);
    }

    /**
     * 转换为会话 DTO
     */
    private SessionDTO toDTO(ChatSession session) {
        RoleType roleType;
        try {
            roleType = RoleType.valueOf(session.getRoleType());
        } catch (IllegalArgumentException e) {
            roleType = RoleType.GENERAL;
        }
        return SessionDTO.builder()
                .id(session.getId())
                .title(session.getTitle())
                .roleType(session.getRoleType())
                .roleDisplayName(roleType.getDisplayName())
                .modelId(session.getModelId())
                .status(session.getStatus())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }
}
