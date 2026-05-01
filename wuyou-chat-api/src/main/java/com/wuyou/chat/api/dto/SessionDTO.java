package com.wuyou.chat.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话 ID
     */
    private Long id;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 角色类型
     */
    private String roleType;

    /**
     * 角色显示名称
     */
    private String roleDisplayName;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
