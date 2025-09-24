package com.example.myownessay.integration;

import com.example.myownessay.dto.auth.request.DeleteAccountRequest;
import com.example.myownessay.dto.auth.request.LoginRequest;
import com.example.myownessay.dto.auth.request.RegisterRequest;
import com.example.myownessay.dto.auth.request.UpdateProfileRequest;
import com.example.myownessay.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("사용자 관리 통합 테스트 - JWT 인증 포함")
class UserManagementIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private String accessToken; // JWT 토큰을 저장할 변수

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity()) // Spring Security 설정 적용
                .build();

        // 테스트 데이터 정리
        userRepository.deleteAll();

        // 테스트용 사용자 생성 및 로그인하여 JWT 토큰 획득
        setupTestUserAndToken();
    }

    private void setupTestUserAndToken() throws Exception {
        // 1. 테스트용 사용자 회원가입
        RegisterRequest registerRequest = new RegisterRequest(
                "test@example.com",
                "password123",
                "테스터"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // 2. 로그인하여 JWT 토큰 획득
        LoginRequest loginRequest = new LoginRequest(
                "test@example.com",
                "password123"
        );

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // 3. 응답에서 JWT 토큰 추출
        String loginResponse = loginResult.getResponse().getContentAsString();
        accessToken = objectMapper.readTree(loginResponse)
                .get("data")
                .get("accessToken")
                .asText();

        System.out.println("✅ 테스트용 JWT 토큰 획득 완료: " + accessToken.substring(0, 20) + "...");
    }

    @Test
    @DisplayName("프로필 조회 - JWT 토큰으로 성공")
    void getProfile_JWT토큰으로_성공() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken)) // JWT 토큰 포함
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("테스터"))
                .andExpect(jsonPath("$.data.timezone").value("Asia/Seoul"))
                .andExpect(jsonPath("$.data.id").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("프로필 조회 - JWT 토큰 없이 실패")
    void getProfile_JWT토큰없이_실패() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden()) // JWT 필터에서 403 반환
                .andDo(print());
    }

    @Test
    @DisplayName("프로필 수정 - 닉네임 변경 성공")
    void updateProfile_닉네임변경_성공() throws Exception {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setNickname("새로운닉네임");

        // When & Then
        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken) // JWT 토큰 포함
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nickname").value("새로운닉네임"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andDo(print());
    }

    @Test
    @DisplayName("프로필 수정 - 타임존 변경 성공")
    void updateProfile_타임존변경_성공() throws Exception {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setTimezone("America/New_York");

        // When & Then
        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken) // JWT 토큰 포함
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.timezone").value("America/New_York"))
                .andExpect(jsonPath("$.data.nickname").value("테스터")) // 기존 닉네임 유지
                .andDo(print());
    }

    @Test
    @DisplayName("프로필 수정 - 잘못된 JWT 토큰으로 실패")
    void updateProfile_잘못된JWT토큰_실패() throws Exception {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setNickname("새닉네임");

        // When & Then
        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", "Bearer 잘못된토큰")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden()) // JWT 인증 실패
                .andDo(print());
    }

    @Test
    @DisplayName("회원 탈퇴 - 성공")
    void deleteAccount_성공() throws Exception {
        // Given
        DeleteAccountRequest request = new DeleteAccountRequest();
        request.setPassword("password123");

        // When & Then
        mockMvc.perform(delete("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken) // JWT 토큰 포함
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("계정이 성공적으로 삭제되었습니다."))
                .andDo(print());

        // 사용자가 정말 삭제되었는지 확인
        assertTrue(userRepository.findByEmail("test@example.com").isEmpty(),
                "회원 탈퇴 후 사용자가 데이터베이스에서 삭제되어야 함");
    }

    @Test
    @DisplayName("회원 탈퇴 - 잘못된 비밀번호")
    void deleteAccount_잘못된비밀번호_실패() throws Exception {
        // Given
        DeleteAccountRequest request = new DeleteAccountRequest();
        request.setPassword("wrongpassword");

        // When & Then
        mockMvc.perform(delete("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken) // JWT 토큰 포함
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()) // 비밀번호 불일치
                .andExpect(jsonPath("$.success").value(false))
                .andDo(print());

        // 사용자가 삭제되지 않았는지 확인
        assertTrue(userRepository.findByEmail("test@example.com").isPresent(),
                "잘못된 비밀번호로는 회원 탈퇴되지 않아야 함");
    }

    @Test
    @DisplayName("전체 시나리오 - JWT 인증 포함")
    void 전체시나리오_JWT인증포함() throws Exception {
        // 1. 프로필 조회
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("test@example.com"));

        // 2. 프로필 수정
        UpdateProfileRequest updateRequest = new UpdateProfileRequest();
        updateRequest.setNickname("수정된닉네임");

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("수정된닉네임"));

        // 3. 수정된 프로필 다시 조회
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("수정된닉네임"));

        System.out.println("✅ 전체 시나리오 성공: JWT 인증 → 프로필 조회 → 수정 → 재조회");
    }
}