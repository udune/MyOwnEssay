package com.example.myownessay.entity.enums;

public enum EssayTheme {
    RECOVERY,
    GRATITUDE,
    CHALLENGE,
    GROWTH;

    public static EssayTheme fromString(String value) {
        try {
            return EssayTheme.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 테마입니다: " + value);
        }
    }
}
