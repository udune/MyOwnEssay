package com.example.myownessay.validator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("슬롯 컨텐츠 검증 테스트")
class SlotValidatorTest {

    @Test
    @DisplayName("독서 슬롯 - 정상 검증")
    void 독서슬롯_정상() {
        // Given
        ReadingSlotValidator validator = new ReadingSlotValidator();
        Map<String, Object> content = new HashMap<>();
        content.put("quote", "완벽보다 계속하기");
        content.put("author", "제임스 클리어");
        content.put("thought", "멈추지 않는 것이 중요하다");

        // When & Then
        assertDoesNotThrow(() -> validator.validate(content));
    }

    @Test
    @DisplayName("독서 슬롯 - 명언 누락 실패")
    void 독서슬롯_명언누락() {
        // Given
        ReadingSlotValidator validator = new ReadingSlotValidator();
        Map<String, Object> content = new HashMap<>();
        content.put("author", "제임스 클리어");
        content.put("thought", "멈추지 않는 것이 중요하다");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(content));
        assertEquals("명언을 입력해주세요.", exception.getMessage());
    }

    @Test
    @DisplayName("독서 슬롯 - 저자 누락 실패")
    void 독서슬롯_저자누락() {
        // Given
        ReadingSlotValidator validator = new ReadingSlotValidator();
        Map<String, Object> content = new HashMap<>();
        content.put("quote", "완벽보다 계속하기");
        content.put("thought", "멈추지 않는 것이 중요하다");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(content));
        assertEquals("저자를 입력해주세요.", exception.getMessage());
    }

    @Test
    @DisplayName("독서 슬롯 - 생각 누락 실패")
    void 독서슬롯_생각누락() {
        // Given
        ReadingSlotValidator validator = new ReadingSlotValidator();
        Map<String, Object> content = new HashMap<>();
        content.put("quote", "완벽보다 계속하기");
        content.put("author", "제임스 클리어");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(content));
        assertEquals("생각을 입력해주세요.", exception.getMessage());
    }

    @Test
    @DisplayName("독서 슬롯 - 명언 500자 초과 실패")
    void 독서슬롯_명언초과() {
        // Given
        ReadingSlotValidator validator = new ReadingSlotValidator();
        Map<String, Object> content = new HashMap<>();
        content.put("quote", "가".repeat(501));
        content.put("author", "제임스 클리어");
        content.put("thought", "멈추지 않는 것이 중요하다");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(content));
        assertEquals("명언은 500자 이내로 작성해주세요.", exception.getMessage());
    }

    @Test
    @DisplayName("독서 슬롯 - 생각 1000자 초과 실패")
    void 독서슬롯_생각초과() {
        // Given
        ReadingSlotValidator validator = new ReadingSlotValidator();
        Map<String, Object> content = new HashMap<>();
        content.put("quote", "완벽보다 계속하기");
        content.put("author", "제임스 클리어");
        content.put("thought", "가".repeat(1001));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(content));
        assertEquals("내 생각은 1000자 이내로 작성해주세요.", exception.getMessage());
    }

    @Test
    @DisplayName("상담 슬롯 - 정상 검증")
    void 상담슬롯_정상() {
        // Given
        ConsultingSlotValidator validator = new ConsultingSlotValidator();
        Map<String, Object> content = new HashMap<>();
        content.put("question", "오늘 하루는 어땠어?");
        content.put("choice", "괜찮았어");
        content.put("result", "쉬어가기도 여정의 일부야");

        // When & Then
        assertDoesNotThrow(() -> validator.validate(content));
    }

    @Test
    @DisplayName("상담 슬롯 - 질문 누락 실패")
    void 상담슬롯_질문누락() {
        // Given
        ConsultingSlotValidator validator = new ConsultingSlotValidator();
        Map<String, Object> content = new HashMap<>();
        content.put("choice", "괜찮았어");
        content.put("result", "쉬어가기도 여정의 일부야");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(content));
        assertEquals("질문을 입력해주세요.", exception.getMessage());
    }

    @Test
    @DisplayName("상담 슬롯 - 선택지 누락 실패")
    void 상담슬롯_선택지누락() {
        // Given
        ConsultingSlotValidator validator = new ConsultingSlotValidator();
        Map<String, Object> content = new HashMap<>();
        content.put("question", "오늘 하루는 어땠어?");
        content.put("result", "쉬어가기도 여정의 일부야");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(content));
        assertEquals("선택지를 입력해주세요.", exception.getMessage());
    }

    @Test
    @DisplayName("상담 슬롯 - 결과 누락 실패")
    void 상담슬롯_결과누락() {
        // Given
        ConsultingSlotValidator validator = new ConsultingSlotValidator();
        Map<String, Object> content = new HashMap<>();
        content.put("question", "오늘 하루는 어땠어?");
        content.put("choice", "괜찮았어");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(content));
        assertEquals("결과를 입력해주세요.", exception.getMessage());
    }

    @Test
    @DisplayName("상담 슬롯 - 질문 200자 초과 실패")
    void 상담슬롯_질문초과() {
        // Given
        ConsultingSlotValidator validator = new ConsultingSlotValidator();
        Map<String, Object> content = new HashMap<>();
        content.put("question", "가".repeat(201));
        content.put("choice", "괜찮았어");
        content.put("result", "쉬어가기도 여정의 일부야");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(content));
        assertEquals("질문은 200자 이내로 작성해주세요.", exception.getMessage());
    }

    @Test
    @DisplayName("상담 슬롯 - 결과 500자 초과 실패")
    void 상담슬롯_결과초과() {
        // Given
        ConsultingSlotValidator validator = new ConsultingSlotValidator();
        Map<String, Object> content = new HashMap<>();
        content.put("question", "오늘 하루는 어땠어?");
        content.put("choice", "괜찮았어");
        content.put("result", "가".repeat(501));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(content));
        assertEquals("결과는 500자 이내로 작성해주세요.", exception.getMessage());
    }

    @Test
    @DisplayName("힐링 슬롯 - 정상 검증")
    void 힐링슬롯_정상() {
        // Given
        HealingSlotValidator validator = new HealingSlotValidator();
        Map<String, Object> content = new HashMap<>();
        content.put("activity", "breathing");
        content.put("duration", 300);
        content.put("result", "호흡 후 어깨 힘이 풀렸다");

        // When & Then
        assertDoesNotThrow(() -> validator.validate(content));
    }

    @Test
    @DisplayName("힐링 슬롯 - 활동 누락 실패")
    void 힐링슬롯_활동누락() {
        // Given
        HealingSlotValidator validator = new HealingSlotValidator();
        Map<String, Object> content = new HashMap<>();
        content.put("duration", 300);
        content.put("result", "호흡 후 어깨 힘이 풀렸다");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(content));
        assertEquals("활동을 입력해주세요.", exception.getMessage());
    }

    @Test
    @DisplayName("힐링 슬롯 - 시간 누락 실패")
    void 힐링슬롯_시간누락() {
        // Given
        HealingSlotValidator validator = new HealingSlotValidator();
        Map<String, Object> content = new HashMap<>();
        content.put("activity", "breathing");
        content.put("result", "호흡 후 어깨 힘이 풀렸다");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(content));
        assertEquals("지속 시간을 입력해주세요.", exception.getMessage());
    }

    @Test
    @DisplayName("힐링 슬롯 - 결과 누락 실패")
    void 힐링슬롯_결과누락() {
        // Given
        HealingSlotValidator validator = new HealingSlotValidator();
        Map<String, Object> content = new HashMap<>();
        content.put("activity", "breathing");
        content.put("duration", 300);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(content));
        assertEquals("활동 결과를 입력해주세요.", exception.getMessage());
    }

    @Test
    @DisplayName("힐링 슬롯 - 시간이 0 이하인 경우 실패")
    void 힐링슬롯_시간0이하() {
        // Given
        HealingSlotValidator validator = new HealingSlotValidator();
        Map<String, Object> content = new HashMap<>();
        content.put("activity", "breathing");
        content.put("duration", 0);
        content.put("result", "호흡 후 어깨 힘이 풀렸다");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(content));
        assertEquals("활동 시간은 0보다 커야 합니다.", exception.getMessage());
    }

    @Test
    @DisplayName("힐링 슬롯 - 결과 500자 초과 실패")
    void 힐링슬롯_결과초과() {
        // Given
        HealingSlotValidator validator = new HealingSlotValidator();
        Map<String, Object> content = new HashMap<>();
        content.put("activity", "breathing");
        content.put("duration", 300);
        content.put("result", "가".repeat(501));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(content));
        assertEquals("활동 결과는 500자 이내로 작성해주세요.", exception.getMessage());
    }

    @Test
    @DisplayName("힐링 슬롯 - 시간이 2시간 초과 실패")
    void 힐링슬롯_시간초과() {
        // Given
        HealingSlotValidator validator = new HealingSlotValidator();
        Map<String, Object> content = new HashMap<>();
        content.put("activity", "breathing");
        content.put("duration", 7201);
        content.put("result", "호흡 후 어깨 힘이 풀렸다");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(content));
        assertEquals("활동 시간은 7200분(50일) 이내로 작성해주세요.", exception.getMessage());
    }

    @Test
    @DisplayName("일기 슬롯 - 정상 검증")
    void 일기슬롯_정상() {
        // Given
        DiarySlotValidator validator = new DiarySlotValidator();
        Map<String, Object> content = new HashMap<>();
        content.put("question", "오늘 나를 웃게 한 일은?");
        content.put("content", "친구와 함께 맛있는 저녁을 먹었다.");
        content.put("emotion", "happy");

        // When & Then
        assertDoesNotThrow(() -> validator.validate(content));
    }

    @Test
    @DisplayName("일기 슬롯 - 질문 누락 실패")
    void 일기슬롯_질문누락() {
        // Given
        DiarySlotValidator validator = new DiarySlotValidator();
        Map<String, Object> content = new HashMap<>();
        content.put("content", "친구와 함께 맛있는 저녁을 먹었다.");
        content.put("emotion", "happy");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(content));
        assertEquals("질문을 입력해주세요.", exception.getMessage());
    }

    @Test
    @DisplayName("일기 슬롯 - 내용 누락 실패")
    void 일기슬롯_내용누락() {
        // Given
        DiarySlotValidator validator = new DiarySlotValidator();
        Map<String, Object> content = new HashMap<>();
        content.put("question", "오늘 나를 웃게 한 일은?");
        content.put("emotion", "happy");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(content));
        assertEquals("일기 내용을 입력해주세요.", exception.getMessage());
    }

    @Test
    @DisplayName("일기 슬롯 - 감정 누락 실패")
    void 일기슬롯_감정누락() {
        // Given
        DiarySlotValidator validator = new DiarySlotValidator();
        Map<String, Object> content = new HashMap<>();
        content.put("question", "오늘 나를 웃게 한 일은?");
        content.put("content", "친구와 함께 맛있는 저녁을 먹었다.");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(content));
        assertEquals("감정을 입력해주세요.", exception.getMessage());
    }

    @Test
    @DisplayName("일기 슬롯 - 질문 200자 초과 실패")
    void 일기슬롯_질문초과() {
        // Given
        DiarySlotValidator validator = new DiarySlotValidator();
        Map<String, Object> content = new HashMap<>();
        content.put("question", "가".repeat(201));
        content.put("content", "친구와 함께 맛있는 저녁을 먹었다.");
        content.put("emotion", "happy");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(content));
        assertEquals("질문은 200자 이내로 작성해주세요.", exception.getMessage());
    }

    @Test
    @DisplayName("일기 슬롯 - 내용이 2000자 초과 실패")
    void 일기슬롯_내용초과() {
        // Given
        DiarySlotValidator validator = new DiarySlotValidator();
        Map<String, Object> content = new HashMap<>();
        content.put("question", "오늘 나를 웃게 한 일은?");
        content.put("content", "가".repeat(2001));
        content.put("emotion", "happy");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(content));
        assertEquals("내용은 2000자 이내로 작성해주세요.", exception.getMessage());
    }

    @Test
    @DisplayName("빈 컨텐츠 검증 실패")
    void 빈컨텐츠_실패() {
        // Given
        ReadingSlotValidator validator = new ReadingSlotValidator();
        Map<String, Object> content = new HashMap<>();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(content));
        assertEquals("독서 기록은 비어있을 수 없습니다.", exception.getMessage());
    }
}