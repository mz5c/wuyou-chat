package com.wuyou.chat.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wuyou.chat.service.entity.AiModelConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 模型配置 Mapper
 */
@Mapper
public interface AiModelConfigMapper extends BaseMapper<AiModelConfig> {
}
