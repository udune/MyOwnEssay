package com.example.myownessay.validator;

import org.springframework.stereotype.Component;

import java.util.Map;

// 일기 슬롯의 콘텐츠 유효성을 검사하는 클래스
@Component
public class DiarySlotValidator implements SlotContentValidator {
    @Override
    public void validate(Map<String, Object> content) {
        if (content == null || content.isEmpty()) { // null 또는 비어있는 경우 예외 발생
            throw new IllegalArgumentException("일기 기록은 비어있을 수 없습니다.");
        }

        String question = getString(content, "question"); // "question" 키에 해당하는 값 조회
        String diaryContent  = getString(content, "content"); // "content" 키에 해당하는
        String emotion  = getString(content, "emotion "); // "emotion" 키에 해당하는 값 조회

        if (question == null || question.trim().isEmpty()) { // 질문이 null 또는 빈 문자열인 경우 예외 발생
            throw new IllegalArgumentException("질문을 입력해주세요.");
        }

        if (diaryContent == null || diaryContent.trim().isEmpty()) { // 내용이 null 또는 빈 문자열인 경우 예외 발생
            throw new IllegalArgumentException("내용을 입력해주세요.");
        }

        if (emotion == null || emotion.trim().isEmpty()) { // 감정이 null 또는 빈 문자열인 경우 예외 발생
            throw new IllegalArgumentException("감정을 입력해주세요.");
        }

        if (question.length() > 200) { // 질문이 200자 초과인 경우 예외 발생
            throw new IllegalArgumentException("질문은 200자 이내로 작성해주세요.");
        }

        if (diaryContent.length() > 2000) { // 내용이 2000자 초과인 경우 예외 발생
            throw new IllegalArgumentException("내용은 2000자 이내로 작성해주세요.");
        }
    }

    private String getString(Map<String, Object> content, String key) { // Map 에서 특정 키에 해당하는 값을 문자열로 변환하여 반환하는 헬퍼 메서드
        Object value = content.get(key); // 키에 해당하는 값 조회
        return value != null ? value.toString() : null; // null이 아니면 문자열로 변환하여 반환, null 이면 null 반환
    }
}
