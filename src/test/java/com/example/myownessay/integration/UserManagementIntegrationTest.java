package com.example.myownessay.integration;

import com.example.myownessay.dto.auth.request.DeleteAccountRequest;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("사용자 관리 통합 테스트")
class UserManagementIntegrationTest {

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

        // 테스트 데이터 정리
        userRepository.deleteAll();

        // 테스트용 사용자 생성
        RegisterRequest registerRequest = new RegisterRequest(
                "test@example.com",
                "password123",
                "테스터"
        );

        try {
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerRequest)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("프로필 조회 - 성공")
    void getProfile_성공() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("테스터"))
                .andExpect(jsonPath("$.data.timezone").value("Asia/Seoul"))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.createdAt").exists())
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.timezone").value("America/New_York"))
                .andExpect(jsonPath("$.data.nickname").value("테스터"))
                .andDo(print());
    }

    @Test
    @DisplayName("프로필 수정 - 잘못된 입력값")
    void updateProfile_잘못된입력값_실패() throws Exception {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setNickname("A"); // 너무 짧은 닉네임

        // When & Then
        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("회원 탈퇴가 완료되었습니다."))
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value(containsString("계정 삭제에 실패했습니다.")))
                .andDo(print());

        // 사용자가 삭제되지 않았는지 확인
        assertTrue(userRepository.findByEmail("test@example.com").isPresent(),
                "잘못된 비밀번호로는 회원 탈퇴되지 않아야 함");
    }

    @Test
    @DisplayName("회원 탈퇴 - 비밀번호 누락")
    void deleteAccount_비밀번호누락_실패() throws Exception {
        // Given
        DeleteAccountRequest request = new DeleteAccountRequest();
        // 비밀번호를 설정하지 않음

        // When & Then
        mockMvc.perform(delete("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("전체 시나리오 - 회원가입부터 탈퇴까지")
    void 전체시나리오_회원가입부터탈퇴까지() throws Exception {
        String email = "fulltest@example.com";
        String password = "testpassword123";
        String nickname = "풀테스터";

        // 1. 새로운 사용자 회원가입
        RegisterRequest registerRequest = new RegisterRequest(email, password, nickname);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        // 2. 프로필 조회 (TODO: 실제로는 JWT 토큰 필요)
        // 현재는 하드코딩된 이메일로 테스트하므로 생략

        // 3. 프로필 수정 (TODO: 실제로는 JWT 토큰 필요)
        // 현재는 하드코딩된 이메일로 테스트하므로 생략

        // 4. 회원 탈퇴 (TODO: 실제로는 JWT 토큰 필요)
        // 현재는 하드코딩된 이메일로 테스트하므로 생략

        System.out.println("✅ 전체 시나리오 테스트는 JWT 인증 구현 후 완성 예정");
    }
}