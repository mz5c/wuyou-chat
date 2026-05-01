package com.wuyou.chat.api.service;

import com.wuyou.chat.api.dto.SessionDTO;

import java.util.List;

/**
 * 会话服务接口
 */
public interface SessionService {

    /**
     * 创建会话
     *
     * @param userId   用户 ID
     * @param title    会话标题
     * @param roleType 角色类型
     * @return 会话信息
     */
    SessionDTO createSession(Long userId, String title, String roleType);

    /**
     * 获取会话详情
     *
     * @param userId    用户 ID
     * @param sessionId 会话 ID
     * @return 会话信息
     */
    SessionDTO getSession(Long userId, Long sessionId);

    /**
     * 获取用户的会话列表
     *
     * @param userId 用户 ID
     * @return 会话列表
     */
    List<SessionDTO> listSessions(Long userId);

    /**
     * 重命名会话
     *
     * @param userId    用户 ID
     * @param sessionId 会话 ID
     * @param title     新标题
     * @return 会话信息
     */
    SessionDTO renameSession(Long userId, Long sessionId, String title);

    /**
     * 更新会话角色
     *
     * @param userId    用户 ID
     * @param sessionId 会话 ID
     * @param roleType  角色类型
     * @return 会话信息
     */
    SessionDTO updateRole(Long userId, Long sessionId, String roleType);

    /**
     * 删除会话（软删除）
     *
     * @param userId    用户 ID
     * @param sessionId 会话 ID
     */
    void deleteSession(Long userId, Long sessionId);
}
