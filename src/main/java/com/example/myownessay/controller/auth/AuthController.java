package com.example.myownessay.controller.auth;

import com.example.myownessay.common.response.ApiResponse;
import com.example.myownessay.dto.auth.UserInfo;
import com.example.myownessay.dto.auth.request.LoginRequest;
import com.example.myownessay.dto.auth.request.RegisterRequest;
import com.example.myownessay.dto.auth.response.TokenResponse;
import com.example.myownessay.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// 인증 관련 API 컨트롤러
@Tag(name="인증 API", description="회원가입, 로그인 및 사용자 정보 조회를 위한 인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

    // 회원가입
    @Operation(
            summary = "회원가입",
            description = "새로운 사용자를 등록합니다."
    )
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(
            @Valid @RequestBody RegisterRequest request // 회원가입 요청 데이터
    ) {
        log.info("회원가입 요청: {}", request.getEmail());

        try {
            UserInfo userInfo = authService.register(request); // 회원가입 처리
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(userInfo)); // 성공 응답 반환
        } catch (Exception e) {
            log.error("회원가입 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage())); // 실패 응답 반환
        }
    }

    // 로그인
    @Operation(
            summary = "로그인",
            description = "사용자 인증 후 JWT 토큰을 발급합니다."
    )
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(
            @Valid @RequestBody LoginRequest request // 로그인 요청 데이터
    ) {
        log.info("로그인 요청: {}", request.getEmail());

        try {
            TokenResponse tokenResponse = authService.login(request); // 로그인 처리
            return ResponseEntity.ok(ApiResponse.success(tokenResponse)); // 성공 응답 반환
        } catch (Exception e) {
            log.error("로그인 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage())); // 실패 응답 반환
        }
    }

    // 로그아웃
    @Operation(
            summary = "로그아웃",
            description = "사용자 로그아웃 처리 (토큰 무효화는 클라이언트에서 처리)"
    )
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        log.info("로그아웃 요청");

        // 실제 로그아웃 로직은 클라이언트 측에서 토큰을 삭제하는 방식으로 처리
        return ResponseEntity.ok(ApiResponse.success("로그아웃 되었습니다.")); // 성공 응답 반환
    }

    // 현재 로그인한 사용자 프로필 조회
    @Operation(
            summary = "프로필 조회",
            description = "현재 로그인한 사용자의 프로필 정보를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getCurrentUser(Authentication authentication) { // 인증 정보 주입
        log.info("현재 사용자 프로필 조회 요청");

        if (authentication == null) {
            log.error("인증 정보가 없습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("인증이 필요합니다.")); // 인증 정보 없음
        }

        try {
            String email = authentication.getName(); // 인증된 사용자 이메일
            log.info("프로필 조회 요청: {}", email);

            UserInfo userInfo = authService.getCurrentUser(email); // 현재 사용자 정보 조회

            log.info("프로필 조회 성공: {}", userInfo.getNickname());
            return ResponseEntity.ok(ApiResponse.success(userInfo)); // 성공 응답 반환
        } catch (Exception e) {
            log.error("현재 사용자 정보 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage())); // 실패 응답 반환
        }
    }
}
