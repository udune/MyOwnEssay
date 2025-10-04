package com.example.myownessay.controller.record;

import com.example.myownessay.common.response.ApiResponse;
import com.example.myownessay.dto.record.request.RecordRequest;
import com.example.myownessay.dto.record.response.DailyRecordsResponse;
import com.example.myownessay.dto.record.response.RecordResponse;
import com.example.myownessay.entity.enums.SlotType;
import com.example.myownessay.service.RecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

// 기록 관리 API 컨트롤러
@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
@Slf4j
@Tag(name="기록 관리 API", description="사용자 기록 생성, 조회, 수정 및 삭제를 위한 API")
@SecurityRequirement(name = "bearerAuth")
public class RecordController {

    private final RecordService recordService;

    // 특정 날짜와 슬롯 타입에 해당하는 기록 저장 또는 수정
    @Operation(
            summary = "기록 저장/수정",
            description = "특정 날짜와 슬롯 타입에 해당하는 사용자의 기록을 생성하거나 수정합니다."
    )
    @PutMapping("/{date}/{slotType}")
    public ResponseEntity<ApiResponse<?>> saveRecord(
            @Parameter(description = "날짜 (yyyy-MM-dd)", example = "2024-06-15")
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @Parameter(description = "슬롯 타입", example = "READING")
            @PathVariable
            String slotType,
            @Valid @RequestBody RecordRequest request,
            Authentication authentication
    ) {
        log.info("기록 저장 요청 - 날짜: {}, 슬롯 타입: {}, 사용자: {}", date, slotType, authentication.getName());

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("인증이 필요합니다."));
        }

        try {
            String email = authentication.getName();
            SlotType slot = SlotType.fromString(slotType);

            RecordResponse response = recordService.saveRecord(email, date, slot, request);

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalArgumentException e) {
            log.error("잘못된 슬롯 타입: {}", slotType);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("잘못된 슬롯 타입입니다."));
        } catch (Exception e) {
            log.error("기록 저장 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("기록 저장에 실패했습니다."));
        }
    }

    // 특정 날짜의 일일 기록 조회
    @Operation(
            summary = "일일 기록 조회",
            description = "특정 날짜에 해당하는 사용자의 모든 기록을 조회하고, 완료율을 계산하여 반환합니다."
    )
    @GetMapping("/{date}")
    public ResponseEntity<ApiResponse<?>> getDailyRecords(
            @Parameter(description = "조회할 날짜 (yyyy-MM-dd)", example = "2024-06-15") @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            Authentication authentication
    ) {
        log.info("일일 기록 조회 요청 - 날짜: {}, 사용자: {}", date, authentication.getName());

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("인증이 필요합니다."));
        }

        try {
            String email = authentication.getName();

            DailyRecordsResponse response = recordService.getDailyRecords(email, date);
            log.info("일일 기록 조회 성공 - 날짜: {}, 기록 수: {}, 완료율: {}%", date, response.getRecords().size(), response.getCompletionRate() * 100);

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("일일 기록 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("일일 기록 조회에 실패했습니다."));
        }
    }

    // 특정 주의 시작 날짜를 기준으로 해당 주의 일일 기록들 조회 (월요일 ~ 일요일)
    @Operation(
            summary = "주간 기록 조회",
            description = "특정 주의 시작 날짜를 기준으로 해당 주의 일일 기록들을 조회합니다. (월요일 ~ 일요일)"
    )
    @GetMapping("/week")
    public ResponseEntity<ApiResponse<?>> getWeeklyRecords(
            @Parameter(description = "조회할 주의 시작 날짜 (yyyy-MM-dd)", example = "2024-06-10")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @Parameter(description = "조회할 주의 종료 날짜 (yyyy-MM-dd)", example = "2024-06-16")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,
            Authentication authentication
    ) {
        log.info("주간 기록 조회 요청 - 시작 날짜: {}, 사용자: {}", startDate, authentication.getName());

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("인증이 필요합니다."));
        }

        try {
            String email = authentication.getName();
            List<RecordResponse> records = recordService.getWeeklyRecords(email, startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success(records));
        } catch (Exception e) {
            log.error("주간 기록 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // 특정 기록 삭제
    @Operation(
            summary = "기록 삭제",
            description = "특정 기록을 삭제합니다."
    )
    @DeleteMapping("/{recordId}")
    public ResponseEntity<ApiResponse<?>> deleteRecord(
            @Parameter(description = "삭제할 기록 ID", example = "1")
            @PathVariable Long recordId,
            Authentication authentication
    ) {
        log.info("기록 삭제 요청 - 기록 ID: {}, 사용자: {}", recordId, authentication.getName());

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("인증이 필요합니다."));
        }

        try {
            String email = authentication.getName();
            recordService.deleteRecord(email, recordId);
            return ResponseEntity.ok(ApiResponse.success("기록이 삭제되었습니다."));
        } catch (Exception e) {
            log.error("기록 삭제 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("기록 삭제에 실패했습니다."));
        }
    }
}
