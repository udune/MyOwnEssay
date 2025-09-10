package com.example.myownessay.controller.test;

import com.example.myownessay.common.response.ApiResponse;
import com.example.myownessay.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class JwtTestController {
    private final JwtService jwtService;

    // JWT 토큰 생성 테스트
    @PostMapping("/generate-token")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateToken(
            @RequestParam String username
    ) {
        String token = jwtService.generateToken(username);
        String refreshToken = jwtService.generateRefreshToken(username);

        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("accessToken", token);
        tokenData.put("refreshToken", refreshToken);
        tokenData.put("tokenType", "Bearer");
        tokenData.put("message", "JWT 토큰이 성공적으로 생성되었습니다");

        return ResponseEntity.ok(ApiResponse.success(tokenData));
    }

    // JWT 토큰 검증 테스트
    @PostMapping("/validate-token")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateToken(
            @RequestParam String token,
            @RequestParam String username
    ) {
        boolean isValid = jwtService.isTokenValid(token, username);
        String extractedUsername = jwtService.extractUsername(token);
        boolean isExpired = jwtService.isTokenExpired(token);

        Map<String, Object> validationData = new HashMap<>();
        validationData.put("valid", isValid);
        validationData.put("extractedUsername", extractedUsername);
        validationData.put("expired", isExpired);
        validationData.put("message", isValid ? "토큰이 유효합니다" : "토큰이 유효하지 않습니다");

        return ResponseEntity.ok(ApiResponse.success(validationData));
    }

    // JWT 설정 정보 확인 테스트
    @GetMapping("/jwt-info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> jwtInfo() {
        Map<String, Object> infoData = new HashMap<>();
        infoData.put("message", "JWT 서비스가 정상적으로 작동하고 있습니다.");
        infoData.put("status", "OK");

        return ResponseEntity.ok(ApiResponse.success(infoData));
    }
}
