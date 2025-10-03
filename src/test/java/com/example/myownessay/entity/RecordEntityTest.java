package com.example.myownessay.entity;

import com.example.myownessay.entity.enums.SlotType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Record 엔티티 테스트")
class RecordEntityTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Record 엔티티 생성 및 저장 - 독서 슬롯")
    void record생성_독서슬롯() {
        // Given - 사용자 생성
        User user = new User();
        user.setEmail("test@example.com");
        user.setNickname("테스터");
        user.setPasswordHash("hashedPassword");
        entityManager.persist(user);

        // Given - 독서 기록 내용 (JSON)
        Map<String, Object> content = new HashMap<>();
        content.put("quote", "완벽보다 계속하기");
        content.put("author", "제임스 클리어");
        content.put("thought", "멈추지 않기가 중요하다");

        // When - Record 생성
        Record record = new Record();
        record.setUser(user);
        record.setRecordDate(LocalDate.now());
        record.setSlotType(SlotType.READING);
        record.setContent(content);
        record.markAsCompleted();

        entityManager.persist(record);
        entityManager.flush();

        // Then - 저장 확인
        assertNotNull(record.getId());
        assertEquals(SlotType.READING, record.getSlotType());
        assertEquals("완벽보다 계속하기", record.getContentValue("quote"));
        assertTrue(record.isCompleted());
    }

    @Test
    @DisplayName("Record 엔티티 생성 및 저장 - 상담 슬롯")
    void record생성_상담슬롯() {
        // Given - 사용자 생성
        User user = new User();
        user.setEmail("counseling@example.com");
        user.setNickname("상담자");
        user.setPasswordHash("hashedPassword");
        entityManager.persist(user);

        // Given - 상담 기록 내용 (JSON)
        Map<String, Object> content = new HashMap<>();
        content.put("question", "오늘 하루는 어땠어?");
        content.put("choice", "괜찮았어");
        content.put("result", "쉬어가기도 여정의 일부야");

        // When - Record 생성
        Record record = new Record();
        record.setUser(user);
        record.setRecordDate(LocalDate.now());
        record.setSlotType(SlotType.CONSULTING);
        record.setContent(content);
        record.markAsCompleted();

        entityManager.persist(record);
        entityManager.flush();

        // Then - 저장 확인
        assertNotNull(record.getId());
        assertEquals(SlotType.CONSULTING, record.getSlotType());
        assertEquals("쉬어가기도 여정의 일부야", record.getContentValue("result"));
        assertTrue(record.isCompleted());
    }

    @Test
    @DisplayName("Record 엔티티 생성 및 저장 - 힐링 슬롯")
    void record생성_힐링슬롯() {
        // Given - 사용자 생성
        User user = new User();
        user.setEmail("healing@example.com");
        user.setNickname("힐링자");
        user.setPasswordHash("hashedPassword");
        entityManager.persist(user);

        // Given - 힐링 기록 내용 (JSON)
        Map<String, Object> content = new HashMap<>();
        content.put("activity", "breathing");
        content.put("duration", 300);
        content.put("result", "호흡 후 어깨 힘이 풀렸다");

        // When - Record 생성
        Record record = new Record();
        record.setUser(user);
        record.setRecordDate(LocalDate.now());
        record.setSlotType(SlotType.HEALING);
        record.setContent(content);
        record.markAsCompleted();

        entityManager.persist(record);
        entityManager.flush();

        // Then - 저장 확인
        assertNotNull(record.getId());
        assertEquals(SlotType.HEALING, record.getSlotType());
        assertEquals(300, record.getContentValue("duration"));
        assertEquals("호흡 후 어깨 힘이 풀렸다", record.getContentValue("result"));
        assertTrue(record.isCompleted());
    }

    @Test
    @DisplayName("Record 엔티티 생성 및 저장 - 일기 슬롯")
    void record생성_일기슬롯() {
        // Given - 사용자 생성
        User user = new User();
        user.setEmail("diary@example.com");
        user.setNickname("일기작성자");
        user.setPasswordHash("hashedPassword");
        entityManager.persist(user);

        // Given - 일기 기록 내용 (JSON)
        Map<String, Object> content = new HashMap<>();
        content.put("question", "오늘 나를 웃게 한 일은?");
        content.put("content", "점심이 즐거웠다");
        content.put("emotion", "happy");

        // When - Record 생성
        Record record = new Record();
        record.setUser(user);
        record.setRecordDate(LocalDate.now());
        record.setSlotType(SlotType.DIARY);
        record.setContent(content);
        record.markAsCompleted();

        entityManager.persist(record);
        entityManager.flush();

        // Then - 저장 확인
        assertNotNull(record.getId());
        assertEquals(SlotType.DIARY, record.getSlotType());
        assertEquals("점심이 즐거웠다", record.getContentValue("content"));
        assertEquals("happy", record.getContentValue("emotion"));
        assertTrue(record.isCompleted());
    }

    @Test
    @DisplayName("JSON 컨텐츠 수정 테스트")
    void json컨텐츠_수정() {
        // Given - 사용자 및 기록 생성
        User user = new User();
        user.setEmail("update@example.com");
        user.setNickname("수정자");
        user.setPasswordHash("hashedPassword");
        entityManager.persist(user);

        Map<String, Object> content = new HashMap<>();
        content.put("key1", "value1");

        Record record = new Record();
        record.setUser(user);
        record.setRecordDate(LocalDate.now());
        record.setSlotType(SlotType.READING);
        record.setContent(content);

        entityManager.persist(record);
        entityManager.flush();
        entityManager.clear();

        // When - JSON 컨텐츠 수정
        Record foundRecord = entityManager.find(Record.class, record.getId());
        foundRecord.setContentValue("key1", "updatedValue");
        foundRecord.setContentValue("key2", "value2");

        entityManager.flush();
        entityManager.clear();

        // Then - 수정 확인
        Record updatedRecord = entityManager.find(Record.class, record.getId());
        assertEquals("updatedValue", updatedRecord.getContentValue("key1"));
        assertEquals("value2", updatedRecord.getContentValue("key2"));
    }

    @Test
    @DisplayName("완료 상태 변경 테스트")
    void 완료상태_변경() {
        // Given - 사용자 및 기록 생성
        User user = new User();
        user.setEmail("status@example.com");
        user.setNickname("상태변경자");
        user.setPasswordHash("hashedPassword");
        entityManager.persist(user);

        Record record = new Record();
        record.setUser(user);
        record.setRecordDate(LocalDate.now());
        record.setSlotType(SlotType.DIARY);
        record.setContent(new HashMap<>());

        entityManager.persist(record);
        entityManager.flush();

        // When & Then - 초기 상태 확인
        assertFalse(record.isCompleted());

        // When - 완료 처리
        record.markAsCompleted();
        entityManager.flush();

        // Then - 완료 상태 확인
        assertTrue(record.isCompleted());

        // When - 미완료 처리
        record.markAsUncompleted();
        entityManager.flush();

        // Then - 미완료 상태 확인
        assertFalse(record.isCompleted());
    }

    @Test
    @DisplayName("SlotType Enum 변환 테스트")
    void slotType변환() {
        // When & Then - 각 슬롯 타입 확인
        assertEquals(SlotType.READING, SlotType.fromString("READING"));
        assertEquals(SlotType.CONSULTING, SlotType.fromString("consulting"));
        assertEquals(SlotType.HEALING, SlotType.fromString("HeAlInG"));
        assertEquals(SlotType.DIARY, SlotType.fromString("diary"));

        // Then - 잘못된 타입은 예외 발생
        assertThrows(IllegalArgumentException.class, () -> {
            SlotType.fromString("INVALID");
        });
    }

    @Test
    @DisplayName("SlotType 설명 확인")
    void slotType설명() {
        assertEquals("독서", SlotType.READING.getDescription());
        assertEquals("상담", SlotType.CONSULTING.getDescription());
        assertEquals("힐링", SlotType.HEALING.getDescription());
        assertEquals("일기", SlotType.DIARY.getDescription());
    }

    @Test
    @DisplayName("복합 JSON 데이터 저장 테스트")
    void 복합json데이터_저장() {
        // Given - 사용자 생성
        User user = new User();
        user.setEmail("complex@example.com");
        user.setNickname("복합데이터");
        user.setPasswordHash("hashedPassword");
        entityManager.persist(user);

        // Given - 복합 JSON 데이터
        Map<String, Object> content = new HashMap<>();
        content.put("text", "복합 데이터 테스트");
        content.put("number", 123);
        content.put("decimal", 45.67);
        content.put("boolean", true);

        Map<String, Object> nested = new HashMap<>();
        nested.put("nestedKey", "nestedValue");
        content.put("nested", nested);

        // When - Record 생성
        Record record = new Record();
        record.setUser(user);
        record.setRecordDate(LocalDate.now());
        record.setSlotType(SlotType.READING);
        record.setContent(content);

        entityManager.persist(record);
        entityManager.flush();
        entityManager.clear();

        // Then - 저장 및 조회 확인
        Record foundRecord = entityManager.find(Record.class, record.getId());
        assertNotNull(foundRecord);
        assertEquals("복합 데이터 테스트", foundRecord.getContentValue("text"));
        assertEquals(123, foundRecord.getContentValue("number"));
        assertEquals(45.67, foundRecord.getContentValue("decimal"));
        assertEquals(true, foundRecord.getContentValue("boolean"));

        @SuppressWarnings("unchecked")
        Map<String, Object> nestedMap = (Map<String, Object>) foundRecord.getContentValue("nested");
        assertEquals("nestedValue", nestedMap.get("nestedKey"));
    }
}