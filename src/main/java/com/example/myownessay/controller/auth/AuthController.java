package com.example.myownessay.controller.auth;

import com.example.myownessay.common.response.ApiResponse;
import com.example.myownessay.dto.auth.UserInfo;
import com.example.myownessay.dto.auth.request.LoginRequest;
import com.example.myownessay.dto.auth.request.RegisterRequest;
import com.example.myownessay.dto.auth.response.TokenResponse;
import com.example.myownessay.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        try {
            UserInfo userInfo = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(userInfo));
        } catch (Exception e) {
            log.error("회원가입 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        try {
            TokenResponse tokenResponse = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success(tokenResponse));
        } catch (Exception e) {
            log.error("로그인 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // 현재 사용자 정보 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<String>> getCurrentUser() {
        return ResponseEntity.ok(ApiResponse.success("현재 사용자 정보 조회는 추후 구현 예정입니다."));
    }
}
