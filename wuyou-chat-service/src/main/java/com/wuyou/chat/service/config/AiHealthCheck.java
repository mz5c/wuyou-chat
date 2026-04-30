package com.wuyou.chat.service.config;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * AI 服务健康检查 - 应用启动时检查 AI 配置
 */
@Slf4j
@Component
public class AiHealthCheck implements CommandLineRunner {

    @Value("${ai.provider.api-url:}")
    private String apiUrl;

    @Value("${ai.provider.api-key:}")
    private String apiKey;

    @Value("${ai.provider.model:gpt-3.5-turbo}")
    private String model;

    @Override
    public void run(String... args) {
        log.info("========================================");
        log.info("AI 服务配置检查");
        log.info("========================================");

        if (StrUtil.isBlank(apiUrl)) {
            log.warn("⚠️  AI API 地址未配置！");
            log.warn("   请设置 ai.provider.api-url 配置项");
            log.info("   当前 AI 服务将返回模拟响应");
        } else {
            log.info("✅ AI API 地址：{}", apiUrl);
        }

        if (StrUtil.isBlank(apiKey) || "sk-".equals(apiKey) || apiKey.contains("你的")) {
            log.warn("⚠️  AI API Key 未配置或为默认值！");
            log.warn("   请设置 ai.provider.api-key 配置项");
        } else {
            String maskedKey = apiKey.substring(0, Math.min(8, apiKey.length())) + "***";
            log.info("✅ API Key: {}", maskedKey);
        }

        log.info("✅ 模型配置：{}", model);
        log.info("========================================");

        if (StrUtil.isBlank(apiUrl)) {
            log.warn("");
            log.warn("📝 配置示例 (application.yml):");
            log.warn("   ai:");
            log.warn("     provider:");
            log.warn("       api-url: https://api.your-provider.com/v1/chat/completions");
            log.warn("       api-key: your-api-key-here");
            log.warn("       model: qwen3.5-122b");
            log.warn("");
        }

        log.info("========================================");
    }
}
