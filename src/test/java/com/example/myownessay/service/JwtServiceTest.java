package com.example.myownessay.service;

import com.example.myownessay.common.exception.AuthException;
import com.example.myownessay.common.exception.AuthErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JWT 서비스 예외 처리 테스트")
class JwtServiceTest {

    private JwtService jwtService;
    private final String testSecret = "test-jwt-secret-key-for-unit-test-must-be-256-bits-long";
    private final long jwtExpiration = 1000L; // 1초 (테스트용 짧은 만료 시간)
    private final long refreshExpiration = 2000L; // 2초

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(testSecret, jwtExpiration, refreshExpiration);
    }

    @Test
    @DisplayName("정상적인 토큰 생성 및 검증")
    void 정상토큰_생성및검증() {
        // Given
        String username = "test@example.com";

        // When
        String token = jwtService.generateToken(username);
        String extractedUsername = jwtService.extractUsername(token);
        boolean isValid = jwtService.isTokenValid(token, username);

        // Then
        assertNotNull(token);
        assertEquals(username, extractedUsername);
        assertTrue(isValid);
    }

    @Test
    @DisplayName("만료된 토큰 예외 처리")
    void 만료된토큰_예외처리() throws InterruptedException {
        // Given
        String username = "test@example.com";
        String token = jwtService.generateToken(username);

        // 토큰 만료까지 대기 (1초 + 여유시간)
        Thread.sleep(1500);

        // When & Then - 만료된 토큰에서 사용자명 추출 시도
        AuthException exception = assertThrows(AuthException.class, () -> {
            jwtService.extractUsername(token);
        });

        assertEquals(AuthErrorCode.INVALID_TOKEN, exception.getErrorCode());
        assertEquals("AUTH006", exception.getCode());
        assertFalse(exception.getMessage().contains("만료된 토큰"));
    }

    @Test
    @DisplayName("잘못된 형식의 토큰 예외 처리")
    void 잘못된형식토큰_예외처리() {
        // Given
        String malformedToken = "잘못된.토큰.형식";

        // When & Then
        AuthException exception = assertThrows(AuthException.class, () -> {
            jwtService.extractUsername(malformedToken);
        });

        assertEquals(AuthErrorCode.INVALID_TOKEN, exception.getErrorCode());
        assertEquals("AUTH006", exception.getCode());
    }

    @Test
    @DisplayName("null 토큰 예외 처리")
    void null토큰_예외처리() {
        // Given
        String nullToken = null;

        // When & Then
        AuthException exception = assertThrows(AuthException.class, () -> {
            jwtService.extractUsername(nullToken);
        });

        assertEquals(AuthErrorCode.INVALID_TOKEN, exception.getErrorCode());
        assertEquals("AUTH006", exception.getCode());
    }

    @Test
    @DisplayName("빈 문자열 토큰 예외 처리")
    void 빈문자열토큰_예외처리() {
        // Given
        String emptyToken = "";

        // When & Then
        AuthException exception = assertThrows(AuthException.class, () -> {
            jwtService.extractUsername(emptyToken);
        });

        assertEquals(AuthErrorCode.INVALID_TOKEN, exception.getErrorCode());
    }

    @Test
    @DisplayName("잘못된 서명 토큰 예외 처리")
    void 잘못된서명토큰_예외처리() {
        // Given - 다른 시크릿으로 서명된 토큰
        JwtService otherJwtService = new JwtService(
                "different-secret-key-for-test-must-be-256-bits-long-too",
                jwtExpiration,
                refreshExpiration
        );
        String username = "test@example.com";
        String tokenWithDifferentSignature = otherJwtService.generateToken(username);

        // When & Then
        AuthException exception = assertThrows(AuthException.class, () -> {
            jwtService.extractUsername(tokenWithDifferentSignature);
        });

        assertEquals(AuthErrorCode.INVALID_TOKEN, exception.getErrorCode());
        assertEquals("AUTH006", exception.getCode());
    }

    @Test
    @DisplayName("토큰 검증 - 다른 사용자명으로 검증 실패")
    void 토큰검증_다른사용자명_실패() {
        // Given
        String originalUsername = "test@example.com";
        String differentUsername = "different@example.com";
        String token = jwtService.generateToken(originalUsername);

        // When & Then
        boolean isValid = jwtService.isTokenValid(token, differentUsername);
        assertFalse(isValid, "다른 사용자명으로 검증하면 실패해야 함");
    }

    @Test
    @DisplayName("토큰 검증 - 잘못된 토큰으로 검증 시 예외")
    void 토큰검증_잘못된토큰_예외() {
        // Given
        String username = "test@example.com";
        String invalidToken = "invalid.token.format";

        // When & Then
        AuthException exception = assertThrows(AuthException.class, () -> {
            jwtService.isTokenValid(invalidToken, username);
        });

        assertEquals(AuthErrorCode.INVALID_TOKEN, exception.getErrorCode());
    }

    @Test
    @DisplayName("Silent 토큰 검증 - 예외 없이 false 반환")
    void silent토큰검증_예외없이false반환() {
        // Given
        String username = "test@example.com";
        String invalidToken = "invalid.token.format";

        // When
        boolean result = jwtService.isTokenValidSilently(invalidToken, username);

        // Then
        assertFalse(result, "Silent 검증은 예외를 던지지 않고 false를 반환해야 함");
    }

    @Test
    @DisplayName("토큰 만료 확인")
    void 토큰만료확인() throws InterruptedException {
        // Given
        String username = "test@example.com";
        String token = jwtService.generateToken(username);

        // When - 토큰이 아직 만료되지 않음
        boolean isExpiredBefore = jwtService.isTokenExpired(token);
        assertFalse(isExpiredBefore, "새로 생성된 토큰은 만료되지 않아야 함");

        // 토큰 만료까지 대기
        Thread.sleep(1500);

        // Then - 토큰이 만료됨
        boolean isExpiredAfter = jwtService.isTokenExpired(token);
        assertTrue(isExpiredAfter, "시간이 지나면 토큰이 만료되어야 함");
    }

    @Test
    @DisplayName("리프레시 토큰 생성 및 검증")
    void 리프레시토큰_생성및검증() {
        // Given
        String username = "test@example.com";

        // When
        String refreshToken = jwtService.generateRefreshToken(username);
        String extractedUsername = jwtService.extractUsername(refreshToken);

        // Then
        assertNotNull(refreshToken);
        assertEquals(username, extractedUsername);

        // 리프레시 토큰도 같은 방식으로 검증 가능
        boolean isValid = jwtService.isTokenValid(refreshToken, username);
        assertTrue(isValid);
    }

    @Test
    @DisplayName("전체 JWT 예외 처리 시나리오")
    void 전체JWT예외처리시나리오() {
        String username = "scenario@example.com";

        // 1. 정상 토큰 생성
        String validToken = jwtService.generateToken(username);
        assertDoesNotThrow(() -> {
            String extracted = jwtService.extractUsername(validToken);
            assertEquals(username, extracted);
        });

        // 2. 다양한 잘못된 토큰들 테스트
        String[] invalidTokens = {
                null,
                "",
                "invalid",
                "invalid.token",
                "invalid.token.format",
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.invalid.signature"
        };

        for (String invalidToken : invalidTokens) {
            assertThrows(AuthException.class, () -> {
                jwtService.extractUsername(invalidToken);
            }, "잘못된 토큰: " + invalidToken);
        }

        System.out.println("✅ JWT 예외 처리 시스템이 모든 경우를 올바르게 처리합니다!");
    }
}