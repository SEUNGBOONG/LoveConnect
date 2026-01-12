package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .servers(List.of( // ✅ 서버 URL 명시 (https)
                        new Server().url("https://api.lovereconnect.co.kr")
                ))
                .info(new Info()
                        .title("Demo API 명세서")
                        .description("Spring Boot + Swagger API 문서")
                        .version("v1.0.0"));
    }
}
