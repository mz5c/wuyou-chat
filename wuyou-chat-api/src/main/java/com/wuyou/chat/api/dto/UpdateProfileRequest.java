package com.wuyou.chat.api.dto;

import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 更新用户资料请求
 */
@Data
public class UpdateProfileRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 昵称
     */
    @Size(max = 50, message = "昵称长度不能超过 50 个字符")
    private String nickname;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 头像 URL
     */
    @Size(max = 255, message = "头像 URL 长度不能超过 255 个字符")
    private String avatar;
}
