package com.example.myownessay.validator;

import org.springframework.stereotype.Component;

import java.util.Map;

// 독서 슬롯의 콘텐츠 유효성을 검사하는 클래스
@Component
public class ReadingSlotValidator implements SlotContentValidator {
    @Override
    public void validate(Map<String, Object> content) {
        if (content == null || content.isEmpty()) { // null 또는 비어있는 경우 예외 발생
            throw new IllegalArgumentException("독서 기록은 비어있을 수 없습니다.");
        }

        String quote = getString(content, "quote"); // "quote" 키에 해당하는 값 조회
        String author = getString(content, "author"); // "author" 키에 해당하는 값 조회
        String thought = getString(content, "thought"); // "thought" 키에 해당하는 값 조회

        if (quote == null || quote.trim().isEmpty()) { // 인용문이 null 또는 빈 문자열인 경우 예외 발생
            throw new IllegalArgumentException("명언을 입력해주세요.");
        }

        if (author == null || author.trim().isEmpty()) { // 저자가 null 또는 빈 문자열인 경우 예외 발생
            throw new IllegalArgumentException("저자를 입력해주세요.");
        }

        if (thought == null || thought.trim().isEmpty()) { // 생각이 null 또는 빈 문자열인 경우 예외 발생
            throw new IllegalArgumentException("생각을 입력해주세요.");
        }

        if (quote.length() > 500) { // 명언이 500자 초과인 경우 예외 발생
            throw new IllegalArgumentException("명언은 500자 이내로 작성해주세요.");
        }

        if (thought.length() > 1000) { // 생각이 1000자 초과인 경우 예외 발생
            throw new IllegalArgumentException("내 생각은 1000자 이내로 작성해주세요.");
        }
    }

    // Map 에서 특정 키에 해당하는 값을 문자열로 변환하여 반환하는 헬퍼 메서드
    private String getString(Map<String, Object> content, String key) {
        Object value = content.get(key); // 키에 해당하는 값 조회
        return value != null ? value.toString() : null; // null이 아니면 문자열로 변환하여 반환, null 이면 null 반환
    }
}
