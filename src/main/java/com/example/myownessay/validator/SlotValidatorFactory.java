package com.example.myownessay.validator;

import com.example.myownessay.entity.enums.SlotType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 슬롯 타입에 따라 적절한 슬롯 콘텐츠 유효성 검사기를 반환하는 팩토리 클래스
@Component
@RequiredArgsConstructor
public class SlotValidatorFactory {

    private final ReadingSlotValidator readingSlotValidator; // 독서 슬롯 유효성 검사기
    private final ConsultingSlotValidator consultingSlotValidator; // 상담 슬롯 유효성 검사기
    private final HealingSlotValidator healingSlotValidator; // 힐링 슬롯 유효성 검사기
    private final DiarySlotValidator diarySlotValidator; // 일기 슬롯 유효성 검사기

    // 슬롯 타입에 따라 해당하는 유효성 검사기를 반환하는 메서드
    public SlotContentValidator getValidator(SlotType slotType) {
        return switch (slotType) {
            case READING -> readingSlotValidator;
            case CONSULTING -> consultingSlotValidator;
            case HEALING -> healingSlotValidator;
            case DIARY -> diarySlotValidator;
        };
    }
}
