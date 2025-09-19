package com.example.myownessay.common.exception;

import com.example.myownessay.common.response.ApiResponse;
import com.example.myownessay.common.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ExceptionHandler {

    // 인증 관련 예외 처리
    @org.springframework.web.bind.annotation.ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleAuthException(AuthException e) {
        log.warn("인증 예외 발생: {} - {}", e.getCode(), e.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                e.getCode(),
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED) // 401 상태 코드 반환
                .body(ApiResponse.error(errorResponse));
    }

    // 유효성 검사 실패 예외 처리
    @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("유효성 검사 예외 발생: {}", e.getMessage());

        Map<String, String> errors = new HashMap<>();

        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        ErrorResponse errorResponse = new ErrorResponse(
                "VALIDATION_ERROR",
                "입력 값이 유효하지 않습니다",
                errors
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // 400 상태 코드 반환
                .body(ApiResponse.error(errorResponse));
    }

    // 바인딩 실패 예외 처리
    @org.springframework.web.bind.annotation.ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleBindException(BindException e) {
        log.warn("바인딩 예외 발생: {}", e.getMessage());

        Map<String, String> errors = new HashMap<>();

        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        ErrorResponse errorResponse = new ErrorResponse(
                "BIND_ERROR",
                "요청 데이터 바인딩에 실패했습니다",
                errors
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // 400 상태 코드 반환
                .body(ApiResponse.error(errorResponse));
    }

    // 기타 모든 런타임 예외 처리
    @org.springframework.web.bind.annotation.ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleRuntimeException(RuntimeException e) {
        log.error("서버 예외 발생: {}", e.getMessage(), e);

        ErrorResponse errorResponse = new ErrorResponse(
                "RUNTIME_ERROR",
                "요청 처리 중 서버 오류가 발생했습니다"
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // 400 상태 코드 반환
                .body(ApiResponse.error(errorResponse));
    }

    // 기타 모든 예외 처리
    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleGeneralException(Exception e) {
        log.error("일반 예외 발생: {}", e.getMessage(), e);

        ErrorResponse errorResponse = new ErrorResponse(
                "INTERNAL_ERROR",
                "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR) // 500 상태 코드 반환
                .body(ApiResponse.error(errorResponse));
    }

}
