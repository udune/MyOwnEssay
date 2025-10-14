package com.example.myownessay.validator;

import org.springframework.stereotype.Component;

import java.util.Map;

// 상담 슬롯의 콘텐츠 유효성을 검사하는 클래스
@Component
public class ConsultingSlotValidator implements SlotContentValidator {
    @Override
    public void validate(Map<String, Object> content) {
        if (content == null || content.isEmpty()) { // null 또는 비어있는 경우 예외 발생
            throw new IllegalArgumentException("상담 기록은 비어있을 수 없습니다.");
        }

        String question = getString(content, "question"); // "question" 키에 해당하는 값 조회
        String choice = getString(content, "choice"); // "choice" 키에 해당하는 값 조회
        String result = getString(content, "result"); // "result" 키에 해당하는 값 조회

        if (question == null || question.trim().isEmpty()) { // 질문이 null 또는 빈 문자열인 경우 예외 발생
            throw new IllegalArgumentException("질문을 입력해주세요.");
        }

        if (choice == null || choice.trim().isEmpty()) { // 선택지가 null 또는 빈 문자열인 경우 예외 발생
            throw new IllegalArgumentException("선택지를 입력해주세요.");
        }

        if (result == null || result.trim().isEmpty()) { // 결과가 null 또는 빈 문자열인 경우 예외 발생
            throw new IllegalArgumentException("결과를 입력해주세요.");
        }

        if (question.length() > 200) { // 질문이 200자 초과인 경우 예외 발생
            throw new IllegalArgumentException("질문은 200자 이내로 작성해주세요.");
        }

        if (result.length() > 500) { // 결과가 500자 초과인 경우 예외 발생
            throw new IllegalArgumentException("결과는 500자 이내로 작성해주세요.");
        }
    }

    // Map 에서 특정 키에 해당하는 값을 문자열로 변환하여 반환하는 헬퍼 메서드
    private String getString(Map<String, Object> content, String key) {
        Object value = content.get(key); // 키에 해당하는 값 조회
        return value != null ? value.toString() : null; // null이 아니면 문자열로 변환하여 반환, null 이면 null 반환
    }
}
