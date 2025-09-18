package com.example.myownessay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JWT 서비스 단위 테스트")
class JwtServiceTest {

    private JwtService jwtService;
    private final String testSecret = "test-jwt-secret-key-for-unit-test-must-be-256-bits-long";
    private final long jwtExpiration = 86400000L; // 24시간
    private final long refreshExpiration = 604800000L; // 7일

    @BeforeEach
    void setUp() {
        // JwtService 생성자에 필요한 값들을 직접 주입
        jwtService = new JwtService(testSecret, jwtExpiration, refreshExpiration);
    }

    @Test
    @DisplayName("JWT 토큰 생성 - 성공")
    void generateToken_성공() {
        // Given
        String username = "test@example.com";

        // When
        String token = jwtService.generateToken(username);

        // Then
        assertNotNull(token, "토큰이 생성되어야 함");
        assertFalse(token.isEmpty(), "토큰이 비어있으면 안됨");
        assertTrue(token.split("\\.").length == 3, "JWT는 3개 부분(header.payload.signature)으로 구성되어야 함");
    }

    @Test
    @DisplayName("JWT 토큰에서 사용자명 추출 - 성공")
    void extractUsername_성공() {
        // Given
        String username = "test@example.com";
        String token = jwtService.generateToken(username);

        // When
        String extractedUsername = jwtService.extractUsername(token);

        // Then
        assertEquals(username, extractedUsername, "토큰에서 추출한 사용자명이 일치해야 함");
    }

    @Test
    @DisplayName("JWT 토큰 유효성 검사 - 유효한 토큰")
    void isTokenValid_유효한토큰() {
        // Given
        String username = "test@example.com";
        String token = jwtService.generateToken(username);

        // When
        boolean isValid = jwtService.isTokenValid(token, username);

        // Then
        assertTrue(isValid, "유효한 토큰은 true를 반환해야 함");
    }

    @Test
    @DisplayName("JWT 토큰 유효성 검사 - 잘못된 사용자명")
    void isTokenValid_잘못된사용자명() {
        // Given
        String originalUsername = "test@example.com";
        String differentUsername = "different@example.com";
        String token = jwtService.generateToken(originalUsername);

        // When
        boolean isValid = jwtService.isTokenValid(token, differentUsername);

        // Then
        assertFalse(isValid, "다른 사용자명으로 검증하면 false를 반환해야 함");
    }

    @Test
    @DisplayName("JWT 토큰 만료 확인 - 새로 생성된 토큰은 만료되지 않음")
    void isTokenExpired_새토큰은만료안됨() {
        // Given
        String username = "test@example.com";
        String token = jwtService.generateToken(username);

        // When
        boolean isExpired = jwtService.isTokenExpired(token);

        // Then
        assertFalse(isExpired, "새로 생성된 토큰은 만료되지 않아야 함");
    }

    @Test
    @DisplayName("리프레시 토큰 생성 - 성공")
    void generateRefreshToken_성공() {
        // Given
        String username = "test@example.com";

        // When
        String refreshToken = jwtService.generateRefreshToken(username);

        // Then
        assertNotNull(refreshToken, "리프레시 토큰이 생성되어야 함");
        assertFalse(refreshToken.isEmpty(), "리프레시 토큰이 비어있으면 안됨");

        String extractedUsername = jwtService.extractUsername(refreshToken);
        assertEquals(username, extractedUsername, "리프레시 토큰에서도 올바른 사용자명을 추출할 수 있어야 함");
    }

    @Test
    @DisplayName("액세스 토큰과 리프레시 토큰 비교 - 서로 다른 토큰")
    void accessToken과RefreshToken_다름() {
        // Given
        String username = "test@example.com";

        // When
        String accessToken = jwtService.generateToken(username);
        String refreshToken = jwtService.generateRefreshToken(username);

        // Then
        assertNotEquals(accessToken, refreshToken, "액세스 토큰과 리프레시 토큰은 달라야 함");

        // 둘 다 같은 사용자명을 가져야 함
        String userFromAccess = jwtService.extractUsername(accessToken);
        String userFromRefresh = jwtService.extractUsername(refreshToken);
        assertEquals(userFromAccess, userFromRefresh, "두 토큰 모두 같은 사용자명을 가져야 함");
    }

    @Test
    @DisplayName("잘못된 토큰으로 사용자명 추출 시도 - 예외 발생")
    void extractUsername_잘못된토큰_예외발생() {
        // Given
        String invalidToken = "잘못된.토큰.문자열";

        // When & Then
        assertThrows(Exception.class, () -> {
            jwtService.extractUsername(invalidToken);
        }, "잘못된 토큰으로 사용자명 추출 시 예외가 발생해야 함");
    }
}