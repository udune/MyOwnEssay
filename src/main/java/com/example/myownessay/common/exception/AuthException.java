package com.example.myownessay.common.exception;

import lombok.Getter;

@Getter
public class AuthException extends RuntimeException {

    private final AuthErrorCode errorCode;

    // 여러 생성자 오버로드
    public AuthException(AuthErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    // 추가 메시지를 포함하는 생성자
    public AuthException(AuthErrorCode errorCode, String message) {
        super(errorCode.getMessage() + " " + message);
        this.errorCode = errorCode;
    }

    // 원인 예외를 포함하는 생성자
    public AuthException(AuthErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public String getCode() {
        return errorCode.getCode();
    }

}
