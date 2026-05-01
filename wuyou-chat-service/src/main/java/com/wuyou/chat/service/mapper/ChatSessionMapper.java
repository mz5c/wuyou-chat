package com.wuyou.chat.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wuyou.chat.service.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会话 Mapper
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}
