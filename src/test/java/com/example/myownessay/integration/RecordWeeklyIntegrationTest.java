package com.example.myownessay.integration;

import com.example.myownessay.dto.auth.request.LoginRequest;
import com.example.myownessay.dto.auth.request.RegisterRequest;
import com.example.myownessay.dto.record.request.RecordRequest;
import com.example.myownessay.repository.RecordRepository;
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
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("주간 기록 조회 API 통합 테스트")
class RecordWeeklyIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecordRepository recordRepository;

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
        recordRepository.deleteAll();

        // 테스트 사용자 생성 및 로그인
        RegisterRequest registerRequest = new RegisterRequest(
                "weekly@example.com",
                "password123",
                "주간테스터"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        LoginRequest loginRequest = new LoginRequest("weekly@example.com", "password123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        accessToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("data")
                .get("accessToken")
                .asText();
    }

    @Test
    @DisplayName("주간 기록 조회 - 성공 (7일간 기록)")
    void getWeeklyRecords_7일_성공() throws Exception {
        // Given - 7일간 기록 생성
        LocalDate startDate = LocalDate.now().minusDays(6);

        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            createRecord(date, "READING");
            createRecord(date, "DIARY");
        }

        LocalDate endDate = LocalDate.now();

        // When & Then
        mockMvc.perform(get("/api/records/week")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(14)) // 7일 × 2슬롯
                .andDo(print());
    }

    @Test
    @DisplayName("주간 기록 조회 - 빈 기록 (기록 없음)")
    void getWeeklyRecords_빈기록() throws Exception {
        // Given - 기록 없음
        LocalDate startDate = LocalDate.now().minusDays(6);
        LocalDate endDate = LocalDate.now();

        // When & Then
        mockMvc.perform(get("/api/records/week")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andDo(print());
    }

    @Test
    @DisplayName("주간 기록 조회 - 날짜 역순 오류")
    void getWeeklyRecords_날짜역순_실패() throws Exception {
        // Given
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().minusDays(6); // 역순

        // When & Then
        mockMvc.perform(get("/api/records/week")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andDo(print());
    }

    @Test
    @DisplayName("주간 기록 조회 - 31일 초과 오류")
    void getWeeklyRecords_31일초과_실패() throws Exception {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(40);
        LocalDate endDate = LocalDate.now();

        // When & Then
        mockMvc.perform(get("/api/records/week")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value("조회 기간은 최대 31일을 초과할 수 없습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("주간 기록 조회 - JWT 없이 실패")
    void getWeeklyRecords_JWT없이_실패() throws Exception {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(6);
        LocalDate endDate = LocalDate.now();

        // When & Then
        mockMvc.perform(get("/api/records/week")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @DisplayName("주간 기록 조회 - 날짜별 그룹핑 확인")
    void getWeeklyRecords_날짜별그룹핑() throws Exception {
        // Given - 특정 날짜에만 기록
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        createRecord(today, "READING");
        createRecord(today, "DIARY");
        createRecord(yesterday, "HEALING");

        LocalDate startDate = today.minusDays(6);
        LocalDate endDate = today;

        // When
        MvcResult result = mockMvc.perform(get("/api/records/week")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String response = result.getResponse().getContentAsString();
        System.out.println("주간 기록 응답: " + response);

        mockMvc.perform(get("/api/records/week")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andDo(print());
    }

    // Helper method - 기록 생성
    private void createRecord(LocalDate date, String slotType) throws Exception {
        Map<String, Object> content = new HashMap<>();
        content.put("test", "테스트 내용");

        RecordRequest request = new RecordRequest();
        request.setContent(content);
        request.setCompleted(true);

        mockMvc.perform(put("/api/records/{date}/{slotType}", date, slotType)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}