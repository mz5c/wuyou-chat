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
