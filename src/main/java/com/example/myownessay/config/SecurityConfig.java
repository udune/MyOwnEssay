package com.example.myownessay.config;

import com.example.myownessay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Spring Security 설정 클래스
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // SecurityFilterChain 빈 등록
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // CSRF 비활성화 (API 서버이므로 필요에 따라 설정)
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin()) // H2 Console iframe 허용
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션 사용 안함
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers( // 인증 없이 접근 허용할 엔드포인트 설정
                        "/", // 루트 엔드포인트
                        "/api/health", // 헬스 체크 엔드포인트
                        "/api/info", // 서비스 정보 엔드포인트
                        "/api/test/**", // 테스트용 엔드포인트
                        "/api/auth/register", // 회원가입 엔드포인트
                        "/api/auth/login", // 회원가입 및 로그인 엔드포인트
                        "/actuator/**", // Actuator 엔드포인트
                        "/swagger-ui/**", // Swagger UI 리소스
                        "/swagger-ui.html", // Swagger UI 엔드포인트
                        "/v3/api-docs/**", // OpenAPI 3 관련 엔드포인트
                        "/swagger-resources/**", // Swagger 관련 엔드포인트
                        "/webjars/**", // Swagger UI 관련 엔드포인트
                        "/h2-console/**" // H2 Console (개발 환경용)
                ).permitAll() // 인증 관련 엔드포인트는 모두 허용
                    .anyRequest().authenticated() // 그 외의 요청은 인증 필요
            ).addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 전에 추가

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을수 없습니다: " + username));
    }

}
