package com.example.myownessay.controller.test;

import com.example.myownessay.common.response.ApiResponse;
import com.example.myownessay.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Tag(name="JWT 테스트 API", description="JWT 토큰 생성, 검증, 파싱 및 정보 확인을 위한 테스트용 API")
@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class JwtTestController {
    private final JwtService jwtService;

    // JWT 토큰 생성 테스트
    @Operation(summary = "JWT 토큰 생성", description = "주어진 사용자 이름으로 JWT 액세스 토큰과 리프레시 토큰을 생성합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "토큰 생성 실패")
    })
    @PostMapping("/generate-token")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateToken(
            @Parameter(description = "토큰을 생성할 사용자 이름", required = true)
            @RequestParam String username
    ) {
        log.info("Generating JWT token for user '{}'", username);

        try {
            String token = jwtService.generateToken(username);
            String refreshToken = jwtService.generateRefreshToken(username);

            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("accessToken", token);
            tokenData.put("refreshToken", refreshToken);
            tokenData.put("tokenType", "Bearer");
            tokenData.put("username", username);
            tokenData.put("generatedAt", LocalDateTime.now());
            tokenData.put("message", "JWT 토큰이 성공적으로 생성되었습니다");

            log.info("Generated JWT token: {}", token);
            return ResponseEntity.ok(ApiResponse.success(tokenData));
        } catch (Exception e) {
            log.error("Error generating JWT token: {}", e.getMessage());

            Map<String, Object> errorData = new HashMap<>();
            errorData.put("error", "토큰 생성 실패");
            errorData.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(ApiResponse.error(errorData));
        }
    }

    // JWT 설정 정보 확인 테스트
    @Operation(summary = "JWT 서비스 정보", description = "JWT 서비스의 상태 및 주요 기능 정보를 확인합니다.")
    @GetMapping("/jwt-info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> jwtInfo() {
        log.info("JWT 서비스 정보 요청");

        Map<String, Object> infoData = new HashMap<>();
        infoData.put("serviceName", "JWT Service");
        infoData.put("status", "ACTIVE");
        infoData.put("message", "JWT 서비스가 정상적으로 작동하고 있습니다.");
        infoData.put("checkedAt", LocalDateTime.now());
        infoData.put("features", Map.of(
                "generateToken", "액세스 토큰 생성",
                "generateRefreshToken", "리프레시 토큰 생성",
                "validateToken", "토큰 검증",
                "extractUsername", "토큰에서 사용자 이름 추출"
        ));

        return ResponseEntity.ok(ApiResponse.success(infoData));
    }

    // JWT 토큰 검증 테스트
    @Operation(summary = "JWT 토큰 검증", description = "주어진 JWT 토큰이 유효한지, 만료되었는지, 그리고 토큰에서 추출된 사용자 이름을 확인합니다.")
    @PostMapping("/validate-token")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateToken(
            @Parameter(description = "검증할 JWT 토큰", required = true)
            @RequestParam String token,
            @Parameter(description = "토큰의 소유자 사용자 이름", required = true)
            @RequestParam String username
    ) {
        log.info("Validating JWT token for user '{}'", username);

        try {
            boolean isValid = jwtService.isTokenValid(token, username);
            String extractedUsername = jwtService.extractUsername(token);
            boolean isExpired = jwtService.isTokenExpired(token);

            Map<String, Object> validationData = new HashMap<>();
            validationData.put("valid", isValid);
            validationData.put("extractedUsername", extractedUsername);
            validationData.put("requestedUsername", username);
            validationData.put("expired", isExpired);
            validationData.put("checkedAt", LocalDateTime.now());
            validationData.put("message", isValid ? "토큰이 유효합니다" : "토큰이 유효하지 않습니다");

            log.info("Token validation result: {}", isValid);
            return ResponseEntity.ok(ApiResponse.success(validationData));
        } catch (Exception e) {
            log.error("Error validating JWT token: {}", e.getMessage());

            Map<String, Object> errorData = new HashMap<>();
            errorData.put("valid", false);
            errorData.put("error", "토큰 검증 실패");
            errorData.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(ApiResponse.error(errorData));
        }
    }

    // JWT 토큰 파싱 테스트
    @Operation(summary = "JWT 토큰 파싱", description = "주어진 JWT 토큰에서 사용자 이름을 추출하고, 토큰의 만료 여부를 확인합니다.")
    @PostMapping("/parse-token")
    public ResponseEntity<ApiResponse<Map<String, Object>>> parseToken(
            @Parameter(description = "파싱할 JWT 토큰", required = true)
            @RequestParam String token
    ) {
        log.info("JWT 토큰 파싱 요청");

        try {
            String username = jwtService.extractUsername(token);
            boolean isExpired = jwtService.isTokenExpired(token);

            Map<String, Object> parseData = new HashMap<>();
            parseData.put("extractedUsername", username);
            parseData.put("expired", isExpired);
            parseData.put("parsedAt", LocalDateTime.now());
            parseData.put("message", "토큰에서 사용자 이름을 성공적으로 추출했습니다");

            log.info("Extracted username from token: {}", username);
            return ResponseEntity.ok(ApiResponse.success(parseData));
        } catch (Exception e) {
            log.error("Error parsing JWT token: {}", e.getMessage());

            Map<String, Object> errorData = new HashMap<>();
            errorData.put("error", "토큰 파싱 실패");
            errorData.put("message", e.getMessage());
            errorData.put("suggestion", "토큰이 올바른지 확인하세요");

            return ResponseEntity.badRequest().body(ApiResponse.error(errorData));
        }
    }
}
