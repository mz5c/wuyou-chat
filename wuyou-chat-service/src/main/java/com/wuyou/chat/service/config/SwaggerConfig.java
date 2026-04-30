package com.wuyou.chat.service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI 配置
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("友聊 AI 问答服务 API")
                        .description("提供 AI 问答功能的 RESTful API")
                        .version("1.0.0")
                        .contact(new Contact().name("wuyou-chat")));
    }
}
