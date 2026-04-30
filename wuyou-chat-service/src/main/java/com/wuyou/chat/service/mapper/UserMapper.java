package com.wuyou.chat.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wuyou.chat.service.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
