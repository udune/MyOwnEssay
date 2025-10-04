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
@DisplayName("기록 API 통합 테스트 - 멱등성 포함")
public class RecordIntegrationTest {

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

        setupTestUserAndToken();
    }

    private void setupTestUserAndToken() throws Exception {
        // 회원가입
        RegisterRequest registerRequest = new RegisterRequest(
                "record@example.com",
                "password123",
                "기록테스터"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // 로그인
        LoginRequest loginRequest = new LoginRequest(
                "record@example.com",
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
    @DisplayName("기록 저장 - 독서 슬롯 성공")
    void saveRecord_독서슬롯_성공() throws Exception {
        // Given
        LocalDate today = LocalDate.now();
        Map<String, Object> content = new HashMap<>();
        content.put("quote", "완벽보다 계속하기");
        content.put("author", "제임스 클리어");
        content.put("thought", "멈추지 않기가 중요하다");

        RecordRequest request = new RecordRequest(content, true);

        // When & Then
        mockMvc.perform(put("/api/records/{date}/{slotType}", today, "READING")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.slotType").value("READING"))
                .andExpect(jsonPath("$.data.isCompleted").value(true))
                .andExpect(jsonPath("$.data.content.quote").value("완벽보다 계속하기"))
                .andDo(print());

        System.out.println("✅ 독서 기록 저장 성공");
    }

    @Test
    @DisplayName("기록 저장 - 멱등성 테스트 (같은 요청 2번)")
    void saveRecord_멱등성_테스트() throws Exception {
        // Given
        LocalDate today = LocalDate.now();
        Map<String, Object> content = new HashMap<>();
        content.put("question", "오늘 어땠어?");
        content.put("choice", "괜찮았어");
        content.put("result", "쉬어가기도 여정의 일부");

        RecordRequest request = new RecordRequest(content, true);

        // When - 첫 번째 요청
        MvcResult firstResult = mockMvc.perform(put("/api/records/{date}/{slotType}", today, "CONSULTING")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String firstResponse = firstResult.getResponse().getContentAsString();
        Long firstId = objectMapper.readTree(firstResponse)
                .get("data")
                .get("id")
                .asLong();

        // When - 두 번째 요청 (같은 내용)
        MvcResult secondResult = mockMvc.perform(put("/api/records/{date}/{slotType}", today, "CONSULTING")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String secondResponse = secondResult.getResponse().getContentAsString();
        Long secondId = objectMapper.readTree(secondResponse)
                .get("data")
                .get("id")
                .asLong();

        // Then - 같은 ID여야 함 (새로 생성하지 않고 업데이트)
        assert firstId.equals(secondId) : "멱등성 실패: 같은 기록이 중복 생성됨";

        System.out.println("✅ 멱등성 테스트 성공!");
        System.out.println("   - 첫 번째 요청 ID: " + firstId);
        System.out.println("   - 두 번째 요청 ID: " + secondId);
        System.out.println("   - 결과: 같은 기록이 업데이트됨 (ID 동일)");
    }

    @Test
    @DisplayName("기록 저장 - 4개 슬롯 모두 저장")
    void saveRecord_4개슬롯_모두저장() throws Exception {
        LocalDate today = LocalDate.now();

        // 1. 독서
        Map<String, Object> readingContent = new HashMap<>();
        readingContent.put("quote", "테스트 명언");
        readingContent.put("author", "저자");
        mockMvc.perform(put("/api/records/{date}/{slotType}", today, "READING")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RecordRequest(readingContent, true))))
                .andExpect(status().isOk());

        // 2. 상담
        Map<String, Object> consultingContent = new HashMap<>();
        consultingContent.put("result", "상담 결과");
        mockMvc.perform(put("/api/records/{date}/{slotType}", today, "CONSULTING")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RecordRequest(consultingContent, true))))
                .andExpect(status().isOk());

        // 3. 힐링
        Map<String, Object> healingContent = new HashMap<>();
        healingContent.put("activity", "breathing");
        healingContent.put("duration", 300);
        mockMvc.perform(put("/api/records/{date}/{slotType}", today, "HEALING")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RecordRequest(healingContent, true))))
                .andExpect(status().isOk());

        // 4. 일기
        Map<String, Object> diaryContent = new HashMap<>();
        diaryContent.put("content", "오늘의 일기");
        diaryContent.put("emotion", "happy");
        mockMvc.perform(put("/api/records/{date}/{slotType}", today, "DIARY")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RecordRequest(diaryContent, true))))
                .andExpect(status().isOk());

        System.out.println("✅ 4개 슬롯 모두 저장 성공");
    }

    @Test
    @DisplayName("일일 기록 조회 - 완료율 포함")
    void getDailyRecords_완료율포함_성공() throws Exception {
        // Given - 먼저 기록 2개 저장
        LocalDate today = LocalDate.now();

        // 독서 기록 (완료)
        Map<String, Object> readingContent = new HashMap<>();
        readingContent.put("quote", "테스트 명언");
        mockMvc.perform(put("/api/records/{date}/{slotType}", today, "READING")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RecordRequest(readingContent, true))));

        // 일기 기록 (미완료)
        Map<String, Object> diaryContent = new HashMap<>();
        diaryContent.put("content", "오늘의 일기");
        mockMvc.perform(put("/api/records/{date}/{slotType}", today, "DIARY")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RecordRequest(diaryContent, false))));

        // When & Then - 일일 기록 조회
        mockMvc.perform(get("/api/records/{date}", today)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.date").value(today.toString()))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records.length()").value(2))
                .andExpect(jsonPath("$.data.completedCount").value(1))
                .andExpect(jsonPath("$.data.totalSlots").value(4))
                .andExpect(jsonPath("$.data.completionRate").value(0.25))
                .andExpect(jsonPath("$.data.isAllCompleted").value(false))
                .andDo(print());

        System.out.println("✅ 일일 기록 조회 성공 (완료율 포함)");
    }

    @Test
    @DisplayName("주간 기록 조회 - 성공")
    void getWeeklyRecords_성공() throws Exception {
        // Given - 3일치 기록 저장
        LocalDate today = LocalDate.now();

        for (int i = 0; i < 3; i++) {
            LocalDate date = today.minusDays(i);
            Map<String, Object> content = new HashMap<>();
            content.put("quote", "명언 " + i);
            RecordRequest request = new RecordRequest(content, true);

            mockMvc.perform(put("/api/records/{date}/{slotType}", date, "READING")
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        }

        // When & Then - 주간 기록 조회
        LocalDate startDate = today.minusDays(6);
        LocalDate endDate = today;

        mockMvc.perform(get("/api/records/week")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andDo(print());

        System.out.println("✅ 주간 기록 조회 성공");
    }

    @Test
    @DisplayName("기록 삭제 - 성공")
    void deleteRecord_성공() throws Exception {
        // Given - 먼저 기록 저장
        LocalDate today = LocalDate.now();
        Map<String, Object> content = new HashMap<>();
        content.put("quote", "삭제될 명언");
        RecordRequest request = new RecordRequest(content, true);

        MvcResult saveResult = mockMvc.perform(put("/api/records/{date}/{slotType}", today, "READING")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        String saveResponse = saveResult.getResponse().getContentAsString();
        Long recordId = objectMapper.readTree(saveResponse)
                .get("data")
                .get("id")
                .asLong();

        // When & Then - 기록 삭제
        mockMvc.perform(delete("/api/records/{recordId}", recordId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("기록이 삭제되었습니다."))
                .andDo(print());

        System.out.println("✅ 기록 삭제 성공");
    }

    @Test
    @DisplayName("기록 저장 - 잘못된 슬롯 타입")
    void saveRecord_잘못된슬롯타입_실패() throws Exception {
        // Given
        LocalDate today = LocalDate.now();
        Map<String, Object> content = new HashMap<>();
        content.put("test", "테스트");
        RecordRequest request = new RecordRequest(content, true);

        // When & Then
        mockMvc.perform(put("/api/records/{date}/{slotType}", today, "INVALID_SLOT")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andDo(print());

        System.out.println("✅ 잘못된 슬롯 타입 검증 성공");
    }

    @Test
    @DisplayName("기록 조회 - JWT 토큰 없이 실패")
    void getRecords_토큰없음_실패() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/records/{date}", LocalDate.now()))
                .andExpect(status().isForbidden())
                .andDo(print());

        System.out.println("✅ JWT 토큰 검증 성공");
    }

    @Test
    @DisplayName("전체 시나리오 - 저장 → 조회 → 수정 → 삭제")
    void 전체시나리오_테스트() throws Exception {
        LocalDate today = LocalDate.now();

        // 1. 기록 저장 (미완료)
        Map<String, Object> content = new HashMap<>();
        content.put("quote", "시작 명언");
        RecordRequest saveRequest = new RecordRequest(content, false);

        MvcResult saveResult = mockMvc.perform(put("/api/records/{date}/{slotType}", today, "READING")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isCompleted").value(false))
                .andReturn();

        Long recordId = objectMapper.readTree(saveResult.getResponse().getContentAsString())
                .get("data")
                .get("id")
                .asLong();

        System.out.println("1️⃣ 기록 저장 완료 (ID: " + recordId + ")");

        // 2. 일일 기록 조회
        mockMvc.perform(get("/api/records/{date}", today)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].id").value(recordId))
                .andExpect(jsonPath("$.data.completedCount").value(0));

        System.out.println("2️⃣ 일일 기록 조회 완료");

        // 3. 기록 수정 (멱등성 - 같은 엔드포인트로 업데이트)
        Map<String, Object> updatedContent = new HashMap<>();
        updatedContent.put("quote", "수정된 명언");
        updatedContent.put("author", "새 저자");
        RecordRequest updateRequest = new RecordRequest(updatedContent, true);

        mockMvc.perform(put("/api/records/{date}/{slotType}", today, "READING")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(recordId))
                .andExpect(jsonPath("$.data.content.quote").value("수정된 명언"))
                .andExpect(jsonPath("$.data.isCompleted").value(true));

        System.out.println("3️⃣ 기록 수정 완료 (멱등성: ID 동일 " + recordId + ")");

        // 4. 완료율 확인
        mockMvc.perform(get("/api/records/{date}", today)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(jsonPath("$.data.completedCount").value(1))
                .andExpect(jsonPath("$.data.completionRate").value(0.25));

        System.out.println("4️⃣ 완료율 확인 완료 (1/4 = 25%)");

        // 5. 기록 삭제
        mockMvc.perform(delete("/api/records/{recordId}", recordId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        System.out.println("5️⃣ 기록 삭제 완료");

        System.out.println("\n✅ 전체 시나리오 성공: 저장 → 조회 → 수정 → 삭제");
    }
}
