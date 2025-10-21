package com.example.myownessay.validator;

import org.springframework.stereotype.Component;

import java.util.Map;

// 힐링 슬롯의 콘텐츠 유효성을 검사하는 클래스
@Component
public class HealingSlotValidator implements SlotContentValidator {
    @Override
    public void validate(Map<String, Object> content) {
        if (content == null || content.isEmpty()) { // null 또는 비어있는 경우 예외 발생
            throw new IllegalArgumentException("힐링 기록은 비어있을 수 없습니다.");
        }

        String activity = getString(content, "activity"); // "activity" 키에 해당하는 값 조회
        Object durationObj = content.get("duration"); // "duration" 키에 해당하는 값 조회
        String result = getString(content, "result"); // "result" 키에 해당하는 값 조회

        if (activity == null || activity.trim().isEmpty()) { // 활동이 null 또는 빈 문자열인 경우 예외 발생
            throw new IllegalArgumentException("활동을 입력해주세요.");
        }

        if (durationObj == null) { // 지속 시간이 null인 경우 예외 발생
            throw new IllegalArgumentException("지속 시간을 입력해주세요.");
        }

        int duration = getInteger(durationObj);
        if (duration <= 0) { // 지속 시간이 0 이하인 경우 예외 발생
            throw new IllegalArgumentException("활동 시간은 0보다 커야 합니다.");
        }

        if (duration > 7200) {
            throw new IllegalArgumentException("활동 시간은 7200분(50일) 이내로 작성해주세요.");
        }

        if (result == null || result.trim().isEmpty()) { // 결과가 null 또는 빈 문자열인 경우 예외 발생
            throw new IllegalArgumentException("활동 결과를 입력해주세요.");
        }

        if (result.length() > 500) { // 활동 결과가 500자 초과인 경우 예외 발생
            throw new IllegalArgumentException("활동 결과는 500자 이내로 작성해주세요.");
        }
    }

    private String getString(Map<String, Object> content, String key) { // Map 에서 특정 키에 해당하는 값을 문자열로 변환하여 반환하는 헬퍼 메서드
        Object value = content.get(key); // 키에 해당하는 값 조회
        return value != null ? value.toString() : null; // null이 아니면 문자열로 변환하여 반환, null 이면 null 반환
    }

    private int getInteger(Object value) { // Object 를 Integer 로 변환하는 헬퍼 메서드
        if (value instanceof Integer) { // 이미 Integer 인 경우 그대로 반환
            return (Integer) value; // Integer 로 캐스팅하여 반환
        }
        if (value instanceof String) { // String 인 경우 파싱 시도
            try {
                return Integer.parseInt((String) value); // String 을 Integer 로 파싱하여 반환
            } catch (NumberFormatException e) { // 파싱 실패 시 예외 발생
                throw new IllegalArgumentException("활동 시간은 숫자여야 합니다."); // 숫자가 아닌 경우 예외 메시지
            }
        }
        throw new IllegalArgumentException("활동 시간 형식이 올바르지 않습니다."); // 그 외의 타입인 경우 예외 발생
    }
}
