package com.example.myownessay.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
                .requestMatchers(
                        "/",
                        "/api/health",
                        "/api/info",
                        "/api/test/**",
                        "/actuator/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**"
                ).permitAll() // 인증 관련 엔드포인트는 모두 허용
                    .anyRequest().permitAll() // 그 외의 요청도 모두 허용 (추후 인증 필요 시 변경)
            );

        return http.build();
    }

    // PasswordEncoder 빈 등록 (비밀번호 암호화를 위해 사용)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
