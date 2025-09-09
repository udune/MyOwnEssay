package com.example.myownessay.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // CSRF 비활성화 (API 서버이므로 필요에 따라 설정)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션 사용 안함
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/api/health", "/api/info", "/actuator/**").permitAll() // 인증 관련 엔드포인트는 모두 허용
                    .anyRequest().permitAll() // 그 외의 요청도 모두 허용 (추후 인증 필요 시 변경)
            );

        return http.build();
    }

}
