package com.example.myownessay.entity.enums;

public enum PublishStatus {
    PRIVATE,
    SHARED,
    PUBLIC;

    public static PublishStatus fromString(String value) {
        try {
            return PublishStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 공개 상태입니다: " + value);
        }
    }
}
