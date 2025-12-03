package com.example.myownessay.integration;

import com.example.myownessay.dto.auth.request.LoginRequest;
import com.example.myownessay.dto.auth.request.RegisterRequest;
import com.example.myownessay.dto.essay.request.EssayCreateRequest;
import com.example.myownessay.dto.essay.request.EssayPublishRequest;
import com.example.myownessay.dto.essay.request.EssayUpdateRequest;
import com.example.myownessay.entity.enums.EssayTheme;
import com.example.myownessay.entity.enums.PublishStatus;
import com.example.myownessay.repository.EssayRepository;
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

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("에세이 API 통합 테스트")
public class EssayIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EssayRepository essayRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private String accessToken;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        userRepository.deleteAll();
        essayRepository.deleteAll();

        setupTestUserAndToken();
    }

    private void setupTestUserAndToken() throws Exception {
        // 회원가입
        RegisterRequest registerRequest = new RegisterRequest(
                "essay@example.com",
                "password123",
                "에세이테스터"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // 로그인
        LoginRequest loginRequest = new LoginRequest(
                "essay@example.com",
                "password123"
        );

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        accessToken = objectMapper.readTree(loginResponse)
                .get("data")
                .get("accessToken")
                .asText();

        System.out.println("✅ 테스트용 JWT 토큰 획득 완료");
    }

    @Test
    @DisplayName("에세이 생성 - 성공")
    void createEssay_성공() throws Exception {
        // Given
        LocalDate weekStart = LocalDate.now().minusDays(7);
        LocalDate weekEnd = LocalDate.now().minusDays(1);

        EssayCreateRequest request = new EssayCreateRequest(
                "나의 성장 이야기",
                "지난 한 주는 나에게 큰 변화의 시간이었다...",
                EssayTheme.GROWTH,
                null,
                weekStart,
                weekEnd,
                null
        );

        // When & Then
        mockMvc.perform(post("/api/essays")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("나의 성장 이야기"))
                .andExpect(jsonPath("$.data.theme").value("GROWTH"))
                .andExpect(jsonPath("$.data.publishStatus").value("PRIVATE"))
                .andDo(print());

        System.out.println("✅ 에세이 생성 성공");
    }

    @Test
    @DisplayName("에세이 조회 - 성공")
    void getEssay_성공() throws Exception {
        // Given - 먼저 에세이 생성
        LocalDate weekStart = LocalDate.now().minusDays(7);
        LocalDate weekEnd = LocalDate.now().minusDays(1);

        EssayCreateRequest createRequest = new EssayCreateRequest(
                "감사의 한 주",
                "이번 주는 감사한 일이 많았다...",
                EssayTheme.GRATITUDE,
                null,
                weekStart,
                weekEnd,
                null
        );

        MvcResult createResult = mockMvc.perform(post("/api/essays")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        Long essayId = objectMapper.readTree(createResponse)
                .get("data")
                .get("id")
                .asLong();

        // When & Then
        mockMvc.perform(get("/api/essays/{id}", essayId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(essayId))
                .andExpect(jsonPath("$.data.title").value("감사의 한 주"))
                .andExpect(jsonPath("$.data.theme").value("GRATITUDE"))
                .andDo(print());

        System.out.println("✅ 에세이 조회 성공");
    }

    @Test
    @DisplayName("에세이 목록 조회 - 성공")
    void getMyEssays_성공() throws Exception {
        // Given - 2개의 에세이 생성
        LocalDate weekStart1 = LocalDate.now().minusDays(14);
        LocalDate weekEnd1 = LocalDate.now().minusDays(8);
        LocalDate weekStart2 = LocalDate.now().minusDays(7);
        LocalDate weekEnd2 = LocalDate.now().minusDays(1);

        EssayCreateRequest request1 = new EssayCreateRequest(
                "첫 번째 에세이",
                "첫 번째 에세이 내용...",
                EssayTheme.RECOVERY,
                null,
                weekStart1,
                weekEnd1,
                null
        );

        EssayCreateRequest request2 = new EssayCreateRequest(
                "두 번째 에세이",
                "두 번째 에세이 내용...",
                EssayTheme.CHALLENGE,
                null,
                weekStart2,
                weekEnd2,
                null
        );

        mockMvc.perform(post("/api/essays")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)));

        mockMvc.perform(post("/api/essays")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)));

        // When & Then
        mockMvc.perform(get("/api/essays/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andDo(print());

        System.out.println("✅ 에세이 목록 조회 성공");
    }

    @Test
    @DisplayName("에세이 수정 - 성공")
    void updateEssay_성공() throws Exception {
        // Given - 먼저 에세이 생성
        LocalDate weekStart = LocalDate.now().minusDays(7);
        LocalDate weekEnd = LocalDate.now().minusDays(1);

        EssayCreateRequest createRequest = new EssayCreateRequest(
                "원본 제목",
                "원본 내용...",
                EssayTheme.GROWTH,
                null,
                weekStart,
                weekEnd,
                null
        );

        MvcResult createResult = mockMvc.perform(post("/api/essays")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        Long essayId = objectMapper.readTree(createResponse)
                .get("data")
                .get("id")
                .asLong();

        // When - 에세이 수정
        EssayUpdateRequest updateRequest = new EssayUpdateRequest(
                "수정된 제목",
                "수정된 내용...",
                EssayTheme.GRATITUDE,
                null
        );

        // Then
        mockMvc.perform(put("/api/essays/{id}", essayId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("수정된 제목"))
                .andExpect(jsonPath("$.data.finalContent").value("수정된 내용..."))
                .andExpect(jsonPath("$.data.theme").value("GRATITUDE"))
                .andDo(print());

        System.out.println("✅ 에세이 수정 성공");
    }

    @Test
    @DisplayName("에세이 발행 - PUBLIC으로 성공")
    void publishEssay_PUBLIC_성공() throws Exception {
        // Given - 먼저 에세이 생성
        LocalDate weekStart = LocalDate.now().minusDays(7);
        LocalDate weekEnd = LocalDate.now().minusDays(1);

        EssayCreateRequest createRequest = new EssayCreateRequest(
                "공개할 에세이",
                "공개할 내용...",
                EssayTheme.GRATITUDE,
                null,
                weekStart,
                weekEnd,
                null
        );

        MvcResult createResult = mockMvc.perform(post("/api/essays")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        Long essayId = objectMapper.readTree(createResponse)
                .get("data")
                .get("id")
                .asLong();

        // When - 에세이 발행
        EssayPublishRequest publishRequest = new EssayPublishRequest(PublishStatus.PUBLIC);

        // Then
        mockMvc.perform(post("/api/essays/{id}/publish", essayId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(publishRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.publishStatus").value("PUBLIC"))
                .andExpect(jsonPath("$.data.shareSlug").exists())
                .andExpect(jsonPath("$.data.publishedAt").exists())
                .andDo(print());

        System.out.println("✅ 에세이 PUBLIC 발행 성공");
    }

    @Test
    @DisplayName("에세이 삭제 - 성공")
    void deleteEssay_성공() throws Exception {
        // Given - 먼저 에세이 생성
        LocalDate weekStart = LocalDate.now().minusDays(7);
        LocalDate weekEnd = LocalDate.now().minusDays(1);

        EssayCreateRequest createRequest = new EssayCreateRequest(
                "삭제할 에세이",
                "삭제할 내용...",
                EssayTheme.CHALLENGE,
                null,
                weekStart,
                weekEnd,
                null
        );

        MvcResult createResult = mockMvc.perform(post("/api/essays")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        Long essayId = objectMapper.readTree(createResponse)
                .get("data")
                .get("id")
                .asLong();

        // When & Then
        mockMvc.perform(delete("/api/essays/{id}", essayId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(print());

        System.out.println("✅ 에세이 삭제 성공");
    }
}
