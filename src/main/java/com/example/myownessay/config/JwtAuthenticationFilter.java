package com.example.myownessay.config;

import com.example.myownessay.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// JWT 인증을 처리하는 필터
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    // 각 요청마다 한 번씩 실행되는 필터 메서드
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        log.debug("JwtAuthenticationFilter 실행 - URI: {}", request.getRequestURI());

        final String authHeader = request.getHeader("Authorization"); // Authorization 헤더에서 토큰 추출
        final String jwt; // JWT 토큰

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("JWT 토큰이 없거나 형식이 잘못됨 - header: {}", authHeader);
            filterChain.doFilter(request, response); // 토큰이 없거나 잘못된 형식이면 다음 필터로 이동
            return;
        }

        jwt = authHeader.substring(7); // "Bearer " 접두사를 제거하여 토큰만 추출

        try {
            String userEmail = jwtService.extractUsername(jwt); // 토큰에서 사용자 이메일 추출
            log.debug("Extracted userEmail: {}", userEmail);

            // SecurityContext에 인증 정보가 없고, 토큰에서 이메일을 성공적으로 추출한 경우
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail); // 사용자 정보 로드

                if (jwtService.isTokenValid(jwt, userEmail)) {
                    UsernamePasswordAuthenticationToken authToken =  // 인증 토큰 생성
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, // 사용자 정보
                                    null, // 비밀번호는 필요하지 않음
                                    userDetails.getAuthorities() // 사용자 권한
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); // 요청 세부 정보 설정

                    SecurityContextHolder.getContext().setAuthentication(authToken); // SecurityContext에 인증 정보 설정
                    log.debug("Authenticated user '{}', setting security context", userEmail);
                }
            }
        } catch (Exception e) {
            log.error("JWT 토큰 처리 중 오류: {}", e.getMessage());
            // 예외가 발생해도 다음 필터로 전달하여 Spring Security가 처리하도록 함
        }

        filterChain.doFilter(request, response); // 다음 필터로 이동
    }
}
