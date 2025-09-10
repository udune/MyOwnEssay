package com.example.myownessay.controller.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

// Docker 컨테이너 모니터링
// 로드밸런서 헬스체크
// 서비스 가용성 확인
// API 서버 상태 점검
@RestController
public class HealthController {

    // 루트 엔드포인트
    // http://localhost:8080/ 접속 시 첫 화면
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
    @GetMapping("/api/info")
    public Map<String, Object> info() {
        return Map.of(
                "app", "MyOwnEssay",
                "description", "An AI-powered essay writing assistant.",
                "features", Map.of(
                        "daily-records", "Track your daily writing activity.",
                        "ai-assistance", "Get AI-generated essay suggestions.",
                        "community", "Engage with other writers."
                ),
                "endpoints", Map.of(
                        "health", "/api/health - Check API health status",
                        "info", "/api/info - Get application information"
                )
        );
    }
}
