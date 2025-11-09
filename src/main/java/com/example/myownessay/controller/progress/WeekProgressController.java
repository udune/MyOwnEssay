package com.example.myownessay.controller.progress;

import com.example.myownessay.common.response.ApiResponse;
import com.example.myownessay.dto.WeekProgressResponse;
import com.example.myownessay.service.WeekProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 주간 진행도 API 컨트롤러
 * 주간 기록 완료 현황 조회 및 통계를 제공합니다.
 */
@RestController
@RequestMapping("/api/week-progress")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "주간 진행도 API", description = "주간 기록 완료 현황 조회 및 통계 API")
@SecurityRequirement(name = "bearerAuth")
public class WeekProgressController {

    private final WeekProgressService weekProgressService;

    /**
     * 특정 주의 진행도 조회
     * GET /api/week-progress/{weekStart}
     */
    @Operation(
            summary = "주간 진행도 조회",
            description = "특정 주(월요일 기준)의 기록 완료 현황을 조회합니다. 완료된 일수, 완료율, 에세이 생성 가능 여부를 반환합니다."
    )
    @GetMapping("/{weekStart}")
    public ResponseEntity<ApiResponse<?>> getWeekProgress(
            @Parameter(description = "주 시작 날짜 (월요일, yyyy-MM-dd)", example = "2025-09-01")
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate weekStart,
            Authentication authentication
    ) {
        log.info("주간 진행도 조회 요청 - 주 시작: {}, 사용자: {}", weekStart, authentication.getName());

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("인증이 필요합니다."));
        }

        try {
            String email = authentication.getName();
            WeekProgressResponse response = weekProgressService.getWeekProgress(email, weekStart);

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalArgumentException e) {
            log.error("잘못된 요청: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("주간 진행도 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("주간 진행도 조회에 실패했습니다."));
        }
    }

    /**
     * 현재 주의 진행도 조회
     * GET /api/week-progress/current
     */
    @Operation(
            summary = "현재 주 진행도 조회",
            description = "현재 주(이번 주 월요일 기준)의 기록 완료 현황을 조회합니다."
    )
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<?>> getCurrentWeekProgress(
            Authentication authentication
    ) {
        log.info("현재 주 진행도 조회 요청 - 사용자: {}", authentication.getName());

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("인증이 필요합니다."));
        }

        try {
            String email = authentication.getName();
            WeekProgressResponse response = weekProgressService.getCurrentWeekProgress(email);

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("현재 주 진행도 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("주간 진행도 조회에 실패했습니다."));
        }
    }

    /**
     * 모든 주간 진행도 조회 (최신순)
     * GET /api/week-progress/all
     */
    @Operation(
            summary = "모든 주간 진행도 조회",
            description = "사용자의 모든 주간 진행도를 최신순으로 조회합니다."
    )
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<?>> getAllWeekProgress(
            Authentication authentication
    ) {
        log.info("모든 주간 진행도 조회 요청 - 사용자: {}", authentication.getName());

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("인증이 필요합니다."));
        }

        try {
            String email = authentication.getName();
            List<WeekProgressResponse> responses = weekProgressService.getAllWeekProgress(email);

            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception e) {
            log.error("모든 주간 진행도 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("주간 진행도 조회에 실패했습니다."));
        }
    }
}
