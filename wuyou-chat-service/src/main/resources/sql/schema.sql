-- 创建数据库
CREATE DATABASE IF NOT EXISTS wuyou_chat DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE wuyou_chat;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
                                        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户 ID',
                                        username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码 (加密)',
    email VARCHAR(100) COMMENT '邮箱',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像 URL',
    status TINYINT DEFAULT 1 COMMENT '账户状态：1-正常，0-禁用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_email (email)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 会话表
CREATE TABLE IF NOT EXISTS chat_session (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '会话 ID',
    user_id     BIGINT NOT NULL COMMENT '用户 ID',
    title       VARCHAR(200) DEFAULT '新对话' COMMENT '会话标题',
    role_type   VARCHAR(20) NOT NULL DEFAULT 'GENERAL' COMMENT '角色类型',
    status      TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-删除',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话表';

-- 对话记录表
CREATE TABLE IF NOT EXISTS chat_record (
                                           id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录 ID',
                                           user_id BIGINT NOT NULL COMMENT '用户 ID',
                                           question TEXT NOT NULL COMMENT '问题内容',
                                           answer TEXT COMMENT 'AI 回答',
                                           conversation_id VARCHAR(64) COMMENT '会话 ID',
    status TINYINT DEFAULT 1 COMMENT '记录状态：1-正常，0-删除',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_conversation_id (conversation_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对话记录表';

-- chat_record 表新增 session_id 字段
-- ALTER TABLE chat_record ADD COLUMN session_id BIGINT COMMENT '会话 ID' AFTER conversation_id;
-- ALTER TABLE chat_record ADD INDEX idx_session_id (session_id);
