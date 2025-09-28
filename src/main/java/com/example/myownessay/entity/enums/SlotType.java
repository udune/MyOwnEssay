package com.example.myownessay.entity.enums;

// 슬롯 타입을 나타내는 열거형
// READING: 독서, CONSULTING: 상담, HEALING: 힐링, DIARY: 일기
public enum SlotType {
    READING("독서"),
    CONSULTING("상담"),
    HEALING("힐링"),
    DIARY("일기");

    private final String description;

    SlotType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static SlotType fromString(String value) {
        for (SlotType type : SlotType.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }

        throw new IllegalArgumentException("잘못된 슬롯 타입: " + value);
    }
}
