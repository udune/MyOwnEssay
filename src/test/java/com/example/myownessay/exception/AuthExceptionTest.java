package com.example.myownessay.exception;

import com.example.myownessay.common.exception.AuthException;
import com.example.myownessay.common.exception.AuthErrorCode;
import com.example.myownessay.dto.auth.request.LoginRequest;
import com.example.myownessay.dto.auth.request.RegisterRequest;
import com.example.myownessay.entity.User;
import com.example.myownessay.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("인증 예외 처리 시스템 테스트")
class AuthExceptionTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("AuthException 기본 생성자 테스트")
    void authException_기본생성자() {
        // Given
        AuthErrorCode errorCode = AuthErrorCode.EMAIL_ALREADY_EXISTS;

        // When
        AuthException exception = new AuthException(errorCode);

        // Then
        assertEquals(errorCode.getCode(), exception.getCode());
        assertEquals(errorCode.getMessage(), exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
    }

    @Test
    @DisplayName("AuthException 추가 메시지 생성자 테스트")
    void authException_추가메시지생성자() {
        // Given
        AuthErrorCode errorCode = AuthErrorCode.AUTHENTICATION_FAILED;
        String additionalMessage = "네트워크 오류가 발생했습니다.";

        // When
        AuthException exception = new AuthException(errorCode, additionalMessage);

        // Then
        assertEquals(errorCode.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains(errorCode.getMessage()));
        assertTrue(exception.getMessage().contains(additionalMessage));
    }

    @Test
    @DisplayName("이메일 중복 예외 처리 - API 레벨")
    void 이메일중복_예외처리_API() throws Exception {
        // Given - 기존 사용자 생성
        User existingUser = new User();
        existingUser.setEmail("duplicate@example.com");
        existingUser.setNickname("기존사용자");
        existingUser.setPasswordHash("hashedpassword");
        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest(
                "duplicate@example.com", // 중복 이메일
                "password123",
                "새사용자"
        );

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()) // 400 상태 (AuthException)
                .andExpect(jsonPath("$.success").value(false))
                .andDo(print());
    }

    @Test
    @DisplayName("닉네임 중복 예외 처리 - API 레벨")
    void 닉네임중복_예외처리_API() throws Exception {
        // Given
        User existingUser = new User();
        existingUser.setEmail("existing@example.com");
        existingUser.setNickname("중복닉네임");
        existingUser.setPasswordHash("hashedpassword");
        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest(
                "new@example.com",
                "password123",
                "중복닉네임" // 중복 닉네임
        );

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andDo(print());
    }

    @Test
    @DisplayName("존재하지 않는 사용자 로그인 예외 처리")
    void 존재하지않는사용자_로그인_예외처리() throws Exception {
        // Given
        LoginRequest request = new LoginRequest(
                "notexist@example.com",
                "password123"
        );

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andDo(print());
    }

    @Test
    @DisplayName("잘못된 비밀번호 로그인 예외 처리")
    void 잘못된비밀번호_로그인_예외처리() throws Exception {
        // Given - 먼저 사용자 생성
        RegisterRequest registerRequest = new RegisterRequest(
                "test@example.com",
                "correctpassword",
                "테스터"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // 잘못된 비밀번호로 로그인 시도
        LoginRequest loginRequest = new LoginRequest(
                "test@example.com",
                "wrongpassword" // 잘못된 비밀번호
        );

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andDo(print());
    }

    @Test
    @DisplayName("입력값 검증 실패 예외 처리")
    void 입력값검증실패_예외처리() throws Exception {
        // Given - 잘못된 형식의 요청
        RegisterRequest request = new RegisterRequest(
                "잘못된이메일형식", // 이메일 형식 아님
                "123", // 너무 짧은 비밀번호
                "A" // 너무 짧은 닉네임
        );

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()) // 400 상태 (입력값 검증 실패)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.data.message").value("입력 값이 유효하지 않습니다"))
                .andExpect(jsonPath("$.data.details").exists()) // 필드별 에러 메시지
                .andDo(print());
    }

    @Test
    @DisplayName("JSON 형식 오류 예외 처리")
    void JSON형식오류_예외처리() throws Exception {
        // Given - 잘못된 JSON
        String invalidJson = "{\"email\": \"test@example.com\", \"password\": }"; // 잘못된 JSON

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest()) // JSON 파싱 오류
                .andDo(print());
    }

    @Test
    @DisplayName("전체 예외 처리 흐름 테스트")
    void 전체예외처리흐름_테스트() {
        // 1. AuthErrorCode enum 테스트
        AuthErrorCode errorCode = AuthErrorCode.EMAIL_ALREADY_EXISTS;
        assertEquals("AUTH001", errorCode.getCode());
        assertEquals("이미 존재하는 이메일입니다.", errorCode.getMessage());

        // 2. AuthException 생성 테스트
        AuthException exception = new AuthException(errorCode);
        assertEquals("AUTH001", exception.getCode());
        assertEquals("이미 존재하는 이메일입니다.", exception.getMessage());

        // 3. 예외 체인 테스트
        RuntimeException cause = new RuntimeException("원인 예외");
        AuthException chainedException = new AuthException(errorCode, cause);
        assertEquals(cause, chainedException.getCause());

        System.out.println("✅ 전체 예외 처리 시스템이 정상적으로 작동합니다!");
    }
}