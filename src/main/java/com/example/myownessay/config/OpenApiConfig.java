package com.example.myownessay.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:essay-backend}")
    private String appName;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("나만의 에세이 API")
                        .description("""
                                **나만의 에세이** 백엔드 API 문서입니다.
                                
                                ## 주요 기능
                                - 일일 4슬롯 기록 (독서/상담/힐링/일기)
                                - AI 기반 주간 에세이 생성
                                - 에세이 공유 커뮤니티
                                - JWT 기반 인증 시스템
                                
                                ## 사용법
                                1. `/api/test/generate-token`으로 JWT 토큰 생성
                                2. 아이콘을 클릭하여 토큰 인증
                                3. API 테스트 진행
                                
                                ## 개발 환경
                                - Java 17 + Spring Boot 3.5.5
                                - PostgreSQL + JWT 인증
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("김민찬")
                                .email("udune8438@gmail.com")
                                .url("https://github.com/udune/MyOwnEssay.git"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/license/mit/"))
                )
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("로컬 개발 서버"),
                        new Server()
                                .url("https://devmyownessay.p-e.kr")
                                .description("스테이징 서버"),
                        new Server()
                                .url("https://myownessay.p-e.kr")
                                .description("운영 서버")

                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP).scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT 토큰을 사용한 인증 방식입니다.")
                        )
                );
    }
}
