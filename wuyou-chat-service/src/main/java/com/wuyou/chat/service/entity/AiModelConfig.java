package com.wuyou.chat.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI 模型配置实体
 */
@Data
@TableName("ai_model_config")
public class AiModelConfig {

    /**
     * 配置 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 配置名称
     */
    private String name;

    /**
     * 供应商
     */
    private String provider;

    /**
     * API 地址
     */
    private String apiUrl;

    /**
     * API 密钥
     */
    private String apiKey;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 是否启用：1-启用，0-禁用
     */
    private Integer isEnabled;

    /**
     * 排序序号
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
