package com.example.myownessay.integration;

import com.example.myownessay.dto.auth.request.LoginRequest;
import com.example.myownessay.dto.auth.request.RegisterRequest;
import com.example.myownessay.dto.record.request.RecordRequest;
import com.example.myownessay.entity.User;
import com.example.myownessay.entity.WeekProgress;
import com.example.myownessay.repository.RecordRepository;
import com.example.myownessay.repository.UserRepository;
import com.example.myownessay.repository.WeekProgressRepository;
import com.example.myownessay.service.WeekProgressService;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 주간 진행도 시스템 통합 테스트
 * WeekProgress 엔티티, 계산 로직, 통계 API, 에세이 생성 자격 검증이 통합적으로 동작하는지 검증
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("US-007: 주간 진행도 시스템 통합 테스트")
public class WeekProgressIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecordRepository recordRepository;

    @Autowired
    private WeekProgressRepository weekProgressRepository;

    @Autowired
    private WeekProgressService weekProgressService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private String accessToken;
    private String userEmail = "weekprogress@example.com";

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        // 데이터 초기화
        weekProgressRepository.deleteAll();
        recordRepository.deleteAll();
        userRepository.deleteAll();

        setupTestUserAndToken();
    }

    private void setupTestUserAndToken() throws Exception {
        // 회원가입
        RegisterRequest registerRequest = new RegisterRequest(
                userEmail,
                "password123",
                "주간진행도테스터"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // 로그인
        LoginRequest loginRequest = new LoginRequest(userEmail, "password123");

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
    @DisplayName("1. WeekProgress 엔티티 생성 및 기본 기능 검증")
    void testWeekProgressEntityCreation() {
        // Given
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        LocalDate weekStart = WeekProgressService.getWeekStart(LocalDate.now());

        // When
        WeekProgress weekProgress = WeekProgress.builder()
                .user(user)
                .weekStart(weekStart)
                .completedDays(3)
                .essayGenerated(false)
                .build();

        WeekProgress saved = weekProgressRepository.save(weekProgress);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getWeekStart()).isEqualTo(weekStart);
        assertThat(saved.getCompletedDays()).isEqualTo(3);
        assertThat(saved.getCompletionRate()).isEqualTo(3.0 / 7.0);
        assertThat(saved.canGenerateEssay()).isTrue(); // 3일 이상 완료
        assertThat(saved.getEssayGenerated()).isFalse();

        System.out.println("✅ WeekProgress 엔티티 생성 및 저장 성공");
        System.out.println("   - 완료율: " + saved.getCompletionRate());
        System.out.println("   - 에세이 생성 가능: " + saved.canGenerateEssay());
    }

    @Test
    @DisplayName("2. 주간 완료 일수 계산 로직 검증 - 3일 완료")
    void testWeekProgressCalculation_ThreeDays() throws Exception {
        // Given: 현재 주의 월요일부터 3일 동안 기록 생성
        LocalDate today = LocalDate.now();
        LocalDate weekStart = WeekProgressService.getWeekStart(today);

        createRecordForDate(weekStart, "READING", true);        // 월요일
        createRecordForDate(weekStart.plusDays(1), "DIARY", true);  // 화요일
        createRecordForDate(weekStart.plusDays(2), "READING", true); // 수요일

        // When: 주간 진행도 계산
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        WeekProgress weekProgress = weekProgressService.calculateAndSaveWeekProgress(user, weekStart);

        // Then
        assertThat(weekProgress.getCompletedDays()).isEqualTo(3);
        assertThat(weekProgress.getCompletionRate()).isEqualTo(3.0 / 7.0);
        assertThat(weekProgress.canGenerateEssay()).isTrue();

        System.out.println("✅ 3일 완료 시나리오 검증 성공");
        System.out.println("   - 완료 일수: " + weekProgress.getCompletedDays());
        System.out.println("   - 에세이 생성 가능: " + weekProgress.canGenerateEssay());
    }

    @Test
    @DisplayName("3. 주간 완료 일수 계산 로직 검증 - 2일 완료 (에세이 생성 불가)")
    void testWeekProgressCalculation_TwoDays_CannotGenerateEssay() throws Exception {
        // Given: 월요일부터 2일만 완료
        LocalDate weekStart = WeekProgressService.getWeekStart(LocalDate.now());

        createRecordForDate(weekStart, "READING", true);        // 월요일
        createRecordForDate(weekStart.plusDays(1), "DIARY", true);  // 화요일

        // When
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        WeekProgress weekProgress = weekProgressService.calculateAndSaveWeekProgress(user, weekStart);

        // Then
        assertThat(weekProgress.getCompletedDays()).isEqualTo(2);
        assertThat(weekProgress.getCompletionRate()).isEqualTo(2.0 / 7.0);
        assertThat(weekProgress.canGenerateEssay()).isFalse(); // 3일 미만이므로 불가

        System.out.println("✅ 2일 완료 시나리오 검증 성공");
        System.out.println("   - 완료 일수: " + weekProgress.getCompletedDays());
        System.out.println("   - 에세이 생성 가능: " + weekProgress.canGenerateEssay());
    }

    @Test
    @DisplayName("4. 주간 완료 일수 계산 로직 검증 - 같은 날 여러 슬롯은 1일로 계산")
    void testWeekProgressCalculation_MultipleRecordsPerDay() throws Exception {
        // Given: 월요일에 여러 슬롯 기록 (하지만 1일로만 계산되어야 함)
        LocalDate weekStart = WeekProgressService.getWeekStart(LocalDate.now());

        createRecordForDate(weekStart, "READING", true);    // 월요일 - 독서
        createRecordForDate(weekStart, "DIARY", true);      // 월요일 - 일기
        createRecordForDate(weekStart, "HEALING", true);    // 월요일 - 힐링

        // When
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        WeekProgress weekProgress = weekProgressService.calculateAndSaveWeekProgress(user, weekStart);

        // Then: 3개 슬롯이지만 같은 날이므로 1일로만 계산
        assertThat(weekProgress.getCompletedDays()).isEqualTo(1);
        assertThat(weekProgress.canGenerateEssay()).isFalse(); // 3일 미만

        System.out.println("✅ 같은 날 여러 슬롯 검증 성공");
        System.out.println("   - 완료 일수: " + weekProgress.getCompletedDays() + " (3개 슬롯이지만 1일)");
    }

    @Test
    @DisplayName("5. 주간 통계 API 조회 성공")
    void testWeekProgressAPI_Success() throws Exception {
        // Given: 5일 완료
        LocalDate weekStart = WeekProgressService.getWeekStart(LocalDate.now());

        for (int i = 0; i < 5; i++) {
            createRecordForDate(weekStart.plusDays(i), "READING", true);
        }

        // When & Then
        mockMvc.perform(get("/api/week-progress/" + weekStart)
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.weekStart").value(weekStart.toString()))
                .andExpect(jsonPath("$.data.completedDays").value(5))
                .andExpect(jsonPath("$.data.completionRate").value(5.0 / 7.0))
                .andExpect(jsonPath("$.data.canGenerateEssay").value(true));

        System.out.println("✅ 주간 통계 API 조회 성공");
    }

    @Test
    @DisplayName("6. 현재 주 진행도 API 조회 성공")
    void testCurrentWeekProgressAPI_Success() throws Exception {
        // Given: 오늘 기록 생성
        LocalDate today = LocalDate.now();
        createRecordForDate(today, "READING", true);

        // When & Then
        mockMvc.perform(get("/api/week-progress/current")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.completedDays").value(1));

        System.out.println("✅ 현재 주 진행도 API 조회 성공");
    }

    @Test
    @DisplayName("7. 에세이 생성 자격 검증 - 3일 미만 실패")
    void testEssayGenerationEligibility_InsufficientDays() throws Exception {
        // Given: 2일만 완료
        LocalDate weekStart = WeekProgressService.getWeekStart(LocalDate.now());
        createRecordForDate(weekStart, "READING", true);
        createRecordForDate(weekStart.plusDays(1), "DIARY", true);

        // When
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        WeekProgress weekProgress = weekProgressService.calculateAndSaveWeekProgress(user, weekStart);

        // Then
        assertThat(weekProgress.canGenerateEssay()).isFalse();
        System.out.println("✅ 에세이 생성 자격 검증 (부족) 성공");
    }

    @Test
    @DisplayName("8. 에세이 생성 자격 검증 - 3일 이상 성공")
    void testEssayGenerationEligibility_SufficientDays() throws Exception {
        // Given: 4일 완료
        LocalDate weekStart = WeekProgressService.getWeekStart(LocalDate.now());
        for (int i = 0; i < 4; i++) {
            createRecordForDate(weekStart.plusDays(i), "READING", true);
        }

        // When
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        WeekProgress weekProgress = weekProgressService.calculateAndSaveWeekProgress(user, weekStart);

        // Then
        assertThat(weekProgress.canGenerateEssay()).isTrue();
        System.out.println("✅ 에세이 생성 자격 검증 (충분) 성공");
    }

    @Test
    @DisplayName("9. 에세이 생성 완료 표시 기능 검증")
    void testMarkEssayGenerated() throws Exception {
        // Given: 3일 완료
        LocalDate weekStart = WeekProgressService.getWeekStart(LocalDate.now());
        for (int i = 0; i < 3; i++) {
            createRecordForDate(weekStart.plusDays(i), "READING", true);
        }

        User user = userRepository.findByEmail(userEmail).orElseThrow();
        weekProgressService.calculateAndSaveWeekProgress(user, weekStart);

        // When: 에세이 생성 완료 표시
        weekProgressService.markEssayGenerated(userEmail, weekStart);

        // Then
        WeekProgress updated = weekProgressRepository.findByUserAndWeekStart(user, weekStart)
                .orElseThrow();
        assertThat(updated.getEssayGenerated()).isTrue();
        assertThat(updated.canGenerateEssay()).isFalse(); // 이미 생성했으므로 불가

        System.out.println("✅ 에세이 생성 완료 표시 검증 성공");
    }

    @Test
    @DisplayName("10. 통합 시나리오 - 전체 주간 진행도 흐름 검증")
    void testFullWeekProgressFlow() throws Exception {
        // Given: 일주일간 점진적으로 기록 생성
        LocalDate weekStart = WeekProgressService.getWeekStart(LocalDate.now());

        // 월요일
        createRecordForDate(weekStart, "READING", true);
        verifyWeekProgress(weekStart, 1, false);

        // 화요일
        createRecordForDate(weekStart.plusDays(1), "DIARY", true);
        verifyWeekProgress(weekStart, 2, false);

        // 수요일 - 3일 달성, 에세이 생성 가능
        createRecordForDate(weekStart.plusDays(2), "READING", true);
        verifyWeekProgress(weekStart, 3, true);

        // 목요일
        createRecordForDate(weekStart.plusDays(3), "HEALING", true);
        verifyWeekProgress(weekStart, 4, true);

        System.out.println("✅ 전체 주간 진행도 흐름 검증 성공");
    }

    @Test
    @DisplayName("11. 삭제된 기록은 완료 일수에서 제외")
    void testDeletedRecordsExcluded() throws Exception {
        // Given: 3일 기록 생성 후 1일 삭제
        LocalDate weekStart = WeekProgressService.getWeekStart(LocalDate.now());

        createRecordForDate(weekStart, "READING", true);
        createRecordForDate(weekStart.plusDays(1), "DIARY", true);
        Long recordIdToDelete = createRecordForDate(weekStart.plusDays(2), "READING", true);

        // 기록 삭제
        mockMvc.perform(delete("/api/records/" + recordIdToDelete)
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // When: 주간 진행도 재계산
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        WeekProgress weekProgress = weekProgressService.calculateAndSaveWeekProgress(user, weekStart);

        // Then: 삭제된 기록은 제외되므로 2일만 계산
        assertThat(weekProgress.getCompletedDays()).isEqualTo(2);
        assertThat(weekProgress.canGenerateEssay()).isFalse();

        System.out.println("✅ 삭제된 기록 제외 검증 성공");
    }

    // === Helper Methods ===

    /**
     * 특정 날짜에 기록 생성
     */
    private Long createRecordForDate(LocalDate date, String slotType, boolean completed) throws Exception {
        Map<String, Object> content = new HashMap<>();

        // 슬롯 타입에 따라 적절한 컨텐츠 생성
        switch (slotType) {
            case "READING":
                content.put("quote", "Test Quote");
                content.put("author", "Test Author");
                content.put("thought", "Test Thought");
                break;
            case "DIARY":
                content.put("question", "Test Question");
                content.put("content", "Test Content");
                content.put("emotion", "happy");
                break;
            case "CONSULTING":
                content.put("question", "Test Question");
                content.put("choice", "Test Choice");
                content.put("result", "Test Result");
                break;
            case "HEALING":
                content.put("activity", "meditation");
                content.put("duration", 300);
                content.put("result", "Test Result");
                break;
            default:
                content.put("test", "test");
        }

        RecordRequest request = new RecordRequest();
        request.setContent(content);
        request.setCompleted(completed);

        MvcResult result = mockMvc.perform(put("/api/records/{date}/{slotType}", date, slotType)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return objectMapper.readTree(response)
                .get("data")
                .get("id")
                .asLong();
    }

    /**
     * 주간 진행도 검증
     */
    private void verifyWeekProgress(LocalDate weekStart, int expectedDays, boolean canGenerate) {
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        WeekProgress weekProgress = weekProgressService.calculateAndSaveWeekProgress(user, weekStart);

        assertThat(weekProgress.getCompletedDays()).isEqualTo(expectedDays);
        assertThat(weekProgress.canGenerateEssay()).isEqualTo(canGenerate);

        System.out.println("   ✓ " + expectedDays + "일 완료 검증 - 에세이 생성 가능: " + canGenerate);
    }
}
