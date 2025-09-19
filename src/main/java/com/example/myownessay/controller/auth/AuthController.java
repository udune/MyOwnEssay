package com.example.myownessay.controller.auth;

import com.example.myownessay.common.response.ApiResponse;
import com.example.myownessay.dto.auth.UserInfo;
import com.example.myownessay.dto.auth.request.LoginRequest;
import com.example.myownessay.dto.auth.request.RegisterRequest;
import com.example.myownessay.dto.auth.response.TokenResponse;
import com.example.myownessay.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "회원가입 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "회원가입 실패 (이메일 또는 닉네임 중복 등)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (잘못된 요청 등)"
            )
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        log.info("회원가입 요청: {}", request.getEmail());

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
    @Operation(
            summary = "로그인",
            description = "사용자 인증 후 JWT 토큰을 발급합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "로그인 실패 (잘못된 자격 증명 등)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (잘못된 요청 등)"
            )
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        log.info("로그인 요청: {}", request.getEmail());

        try {
            TokenResponse tokenResponse = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success(tokenResponse));
        } catch (Exception e) {
            log.error("로그인 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
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
        return ResponseEntity.ok(ApiResponse.success("로그아웃 되었습니다."));
    }

    // 현재 사용자 정보 조회
    @Operation(
            summary = "현재 사용자 정보 조회",
            description = "인증된 사용자의 정보를 조회합니다. (추후 구현 예정)"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "사용자 정보 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (로그인 필요)"
            )
    })
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<String>> getCurrentUser() {
        log.info("현재 사용자 정보 조회 요청");

        return ResponseEntity.ok(ApiResponse.success("현재 사용자 정보 조회는 추후 구현 예정입니다."));
    }
}
