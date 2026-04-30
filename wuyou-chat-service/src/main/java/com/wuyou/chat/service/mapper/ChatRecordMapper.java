package com.wuyou.chat.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wuyou.chat.service.entity.ChatRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 对话记录 Mapper
 */
@Mapper
public interface ChatRecordMapper extends BaseMapper<ChatRecord> {
}
