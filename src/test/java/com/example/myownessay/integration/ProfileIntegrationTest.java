package com.example.myownessay.integration;

import com.example.myownessay.dto.auth.request.LoginRequest;
import com.example.myownessay.dto.auth.request.RegisterRequest;
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
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@DisplayName("프로필 조회 API 통합 테스트")
class ProfileIntegrationTest {

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
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        userRepository.deleteAll();
    }

    @Test
    @DisplayName("프로필 조회 - 성공")
    void getCurrentUser_성공() throws Exception {
        // 1. 회원가입
        RegisterRequest registerRequest = new RegisterRequest(
                "profile@example.com",
                "password123",
                "프로필테스터"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // 2. 로그인해서 JWT 토큰 받기
        LoginRequest loginRequest = new LoginRequest(
                "profile@example.com",
                "password123"
        );

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // 3. 응답에서 JWT 토큰 추출
        String loginResponse = loginResult.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(loginResponse)
                .get("data")
                .get("accessToken")
                .asText();

        // 4. JWT 토큰으로 프로필 조회
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("profile@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("프로필테스터"))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("프로필 조회 - JWT 토큰 없이 요청 시 실패")
    void getCurrentUser_토큰없음_실패() throws Exception {
        // JWT 토큰 없이 프로필 조회 시도
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isForbidden()) // 403 상태 코드
                .andDo(print());
    }

    @Test
    @DisplayName("프로필 조회 - 잘못된 JWT 토큰으로 요청 시 실패")
    void getCurrentUser_잘못된토큰_실패() throws Exception {
        // 잘못된 JWT 토큰으로 프로필 조회 시도
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer 잘못된토큰"))
                .andExpect(status().isForbidden()) // 403 상태 코드
                .andDo(print());
    }

    @Test
    @DisplayName("프로필 조회 - Bearer 없는 토큰 헤더로 요청 시 실패")
    void getCurrentUser_Bearer없는헤더_실패() throws Exception {
        // Bearer 없이 토큰만 전송
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "some-token"))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @DisplayName("전체 시나리오 - 회원가입 → 로그인 → 프로필 조회")
    void 전체시나리오_회원가입부터프로필조회까지() throws Exception {
        String email = "fullscenario@example.com";
        String password = "testpassword123";
        String nickname = "풀시나리오테스터";

        // 1. 회원가입
        RegisterRequest registerRequest = new RegisterRequest(email, password, nickname);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // 2. 로그인
        LoginRequest loginRequest = new LoginRequest(email, password);
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // 3. JWT 토큰 추출
        String loginResponse = loginResult.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(loginResponse)
                .get("data")
                .get("accessToken")
                .asText();

        // 4. 프로필 조회
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.nickname").value(nickname));

        System.out.println("✅ 전체 시나리오 성공: 회원가입 → 로그인 → 프로필 조회");
    }
}