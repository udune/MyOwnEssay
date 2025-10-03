package com.example.myownessay.controller.record;

import com.example.myownessay.common.response.ApiResponse;
import com.example.myownessay.dto.record.response.DailyRecordsResponse;
import com.example.myownessay.service.RecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

// 기록 관리 API 컨트롤러
@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
@Slf4j
@Tag(name="기록 관리 API", description="사용자 기록 생성, 조회, 수정 및 삭제를 위한 API")
@SecurityRequirement(name = "bearerAuth")
public class RecordController {

    private final RecordService recordService;

    // 특정 날짜의 일일 기록 조회
    @Operation(
            summary = "일일 기록 조회",
            description = "특정 날짜에 해당하는 사용자의 모든 기록을 조회하고, 완료율을 계산하여 반환합니다."
    )
    @GetMapping("/{date}")
    public ResponseEntity<ApiResponse<?>> getDailyRecords(
            @Parameter(description = "조회할 날짜 (yyyy-MM-dd)", example = "2024-06-15")
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            Authentication authentication
    ) {
        log.info("일일 기록 조회 요청 - 날짜: {}, 사용자: {}", date, authentication.getName());

        if (authentication == null) {
            log.error("인증되지 않은 사용자 요청");
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("인증이 필요합니다."));
        }

        try {
            String email = authentication.getName();
            log.info("인증된 사용자 이메일: {}", email);

            DailyRecordsResponse response = recordService.getDailyRecords(email, date);
            log.info("일일 기록 조회 성공 - 날짜: {}, 기록 수: {}, 완료율: {}%", date, response.getRecords().size(), response.getCompletionRate() * 100);

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("일일 기록 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("일일 기록 조회에 실패했습니다."));
        }
    }
}
