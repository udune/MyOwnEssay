package com.example.myownessay.controller.streak;

import com.example.myownessay.common.response.ApiResponse;
import com.example.myownessay.dto.streak.response.StreakResponse;
import com.example.myownessay.service.StreakService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/streak")
@RequiredArgsConstructor
@Slf4j
@Tag(name="Streak API", description="사용자의 연속 기록(스트릭) 관련 API")
public class StreakController {
    private final StreakService streakService;

    @Operation(summary = "연속 기록 조회", description = "사용자의 현재 연속 기록 정보를 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getStreak(Authentication authentication) {
        log.info("연속 기록 조회 요청 - 사용자: {}", authentication.getName());

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("인증이 필요합니다."));
        }

        try {
            // 인증된 사용자 이메일 가져오기
            String email = authentication.getName();

            // 현재 연속 기록 및 최대 연속 기록 조회
            int currentStreak = streakService.getCurrentStreak(email);
            int maxStreak = streakService.getMaxStreak(email);

            // 응답 생성
            StreakResponse response = StreakResponse.of(currentStreak, maxStreak);
            log.info("연속 기록 조회 성공 - 사용자: {}, 현재 연속 기록: {}, 최대 연속 기록: {}", email, currentStreak, maxStreak);

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            log.error("연속 기록 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("연속 기록 조회에 실패했습니다."));
        }
    }
}
