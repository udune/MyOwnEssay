package com.example.myownessay.controller.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

// Docker 컨테이너 모니터링
// 로드밸런서 헬스체크
// 서비스 가용성 확인
// API 서버 상태 점검
@Tag(name="헬스 체크 API", description="API 서버 상태 점검 및 서비스 정보 제공")
@RestController
public class HealthController {

    // 루트 엔드포인트
    // http://localhost:8080/ 접속 시 첫 화면
    @Operation(
            summary = "API 서버 상태 확인",
            description = "API 서버가 정상 동작 중인지 확인하는 엔드포인트입니다."
    )
    @GetMapping("/")
    public Map<String, Object> home() {
        return Map.of(
                "message", "MyOwnEssay API 서버가 정상 동작중입니다.",
                "service", "essay-backend",
                "version", "1.0.0",
                "profile", "dev",
                "timestamp", LocalDateTime.now()
        );
    }

    // 헬스 체크
    // 30초마다 호출
    @Operation(
            summary = "헬스 체크",
            description = "API 서버의 상태를 점검하는 헬스 체크 엔드포인트입니다."
    )
    @GetMapping("/api/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "database", "PostgreSQL",
                "framework", "Spring Boot 3.5.5",
                "timestamp", LocalDateTime.now()
        );
    }

    // 서비스 정보
    @Operation(
            summary = "서비스 정보",
            description = "애플리케이션의 주요 기능과 사용된 기술 스택에 대한 정보를 제공합니다."
    )
    @GetMapping("/api/info")
    public Map<String, Object> info() {
        return Map.of(
                "app", "MyOwnEssay",
                "description", "일상의 기록이 나만의 책이 되는 AI 에세이 서비스",
                "features", Map.of(
                        "daily-records", "일일 4슬롯 기록 (독서/상담/힐링/일기)",
                        "ai-essays", "AI 기반 주간 에세이 자동 생성",
                        "community", "에세이 공유 및 커뮤니티 기능",
                        "authentication", "JWT 기반 보안 인증"
                ),
                "endpoints", Map.of(
                        "swagger", "/swagger-ui/index.html - API 문서",
                        "health", "/api/health - 서버 상태 확인",
                        "info", "/api/info - 서비스 정보"
                ),
                "contact", Map.of(
                        "github", "https://github.com/udune/MyOwnEssay.git",
                        "email", "udune8438@gmail.com"
                )
        );
    }
}
