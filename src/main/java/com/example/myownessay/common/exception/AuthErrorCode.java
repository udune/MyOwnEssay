package com.example.myownessay.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode {
    EMAIL_ALREADY_EXISTS("AUTH001", "이미 존재하는 이메일입니다."),
    NICKNAME_ALREADY_EXISTS("AUTH002", "이미 존재하는 닉네임입니다."),
    USER_NOT_FOUND("AUTH003", "사용자를 찾을 수 없습니다."),
    INVALID_PASSWORD("AUTH004", "비밀번호가 올바르지 않습니다."),
    ACCOUNT_DISABLED("AUTH005", "비활성화된 계정입니다."),
    INVALID_TOKEN("AUTH006", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN("AUTH007", "만료된 토큰입니다."),
    TOKEN_NOT_FOUND("AUTH008", "토큰이 존재하지 않습니다."),
    MALFORMED_TOKEN("AUTH009", "잘못된 형식의 토큰입니다."),
    AUTHENTICATION_FAILED("AUTH010", "인증에 실패했습니다."),
    ACCESSED_DENIED("AUTH011", "접근 권한이 없습니다."),
    INVALID_EMAIL_FORMAT("AUTH012", "유효하지 않은 이메일 형식입니다."),
    PASSWORD_TOO_SHORT("AUTH013", "비밀번호가 너무 짧습니다."),
    NICKNAME_TOO_SHORT("AUTH014", "닉네임이 너무 짧습니다."),
    TOO_MANY_LOGIN_ATTEMPTS("AUTH015", "너무 많은 로그인 시도입니다. 잠시 후 다시 시도해주세요."),
    SUSPICIOUS_ACTIVITY("AUTH016", "의심스러운 활동이 감지되었습니다. 추가 인증이 필요합니다.");

    private final String code;
    private final String message;
}
