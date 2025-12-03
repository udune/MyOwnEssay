package com.example.myownessay.controller.essay;

import com.example.myownessay.common.response.ApiResponse;
import com.example.myownessay.dto.essay.request.EssayCreateRequest;
import com.example.myownessay.dto.essay.request.EssayPublishRequest;
import com.example.myownessay.dto.essay.request.EssayUpdateRequest;
import com.example.myownessay.dto.essay.response.EssayResponse;
import com.example.myownessay.service.EssayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/essays")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "에세이 관리 API", description = "에세이 생성, 조회, 수정, 발행 및 삭제를 위한 API")
@SecurityRequirement(name = "bearerAuth")
public class EssayController {

    private final EssayService essayService;

    /**
     * 에세이 생성
     */
    @Operation(
            summary = "에세이 생성",
            description = "새로운 에세이를 생성합니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createEssay(
            @Valid @RequestBody EssayCreateRequest request,
            Authentication authentication
    ) {
        log.info("에세이 생성 요청 - 사용자: {}", authentication.getName());

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("인증이 필요합니다."));
        }

        try {
            String email = authentication.getName();
            EssayResponse response = essayService.createEssay(email, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("에세이 생성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("에세이 생성에 실패했습니다."));
        }
    }

    /**
     * 에세이 조회
     */
    @Operation(
            summary = "에세이 조회",
            description = "특정 에세이를 조회합니다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getEssay(
            @Parameter(description = "에세이 ID", example = "1")
            @PathVariable Long id,
            Authentication authentication
    ) {
        log.info("에세이 조회 요청 - ID: {}, 사용자: {}", id, authentication.getName());

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("인증이 필요합니다."));
        }

        try {
            String email = authentication.getName();
            EssayResponse response = essayService.getEssay(email, id);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (RuntimeException e) {
            log.error("에세이 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("에세이 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("에세이 조회에 실패했습니다."));
        }
    }

    /**
     * 내 에세이 목록 조회
     */
    @Operation(
            summary = "내 에세이 목록 조회",
            description = "로그인한 사용자의 모든 에세이를 조회합니다."
    )
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getMyEssays(
            Authentication authentication
    ) {
        log.info("내 에세이 목록 조회 요청 - 사용자: {}", authentication.getName());

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("인증이 필요합니다."));
        }

        try {
            String email = authentication.getName();
            List<EssayResponse> responses = essayService.getMyEssays(email);
            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception e) {
            log.error("내 에세이 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("에세이 목록 조회에 실패했습니다."));
        }
    }

    /**
     * 에세이 수정
     */
    @Operation(
            summary = "에세이 수정",
            description = "기존 에세이를 수정합니다."
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateEssay(
            @Parameter(description = "에세이 ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody EssayUpdateRequest request,
            Authentication authentication
    ) {
        log.info("에세이 수정 요청 - ID: {}, 사용자: {}", id, authentication.getName());

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("인증이 필요합니다."));
        }

        try {
            String email = authentication.getName();
            EssayResponse response = essayService.updateEssay(email, id, request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (RuntimeException e) {
            log.error("에세이 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("에세이 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("에세이 수정에 실패했습니다."));
        }
    }

    /**
     * 에세이 발행/공개 상태 변경
     */
    @Operation(
            summary = "에세이 발행",
            description = "에세이의 공개 상태를 변경합니다."
    )
    @PostMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<?>> publishEssay(
            @Parameter(description = "에세이 ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody EssayPublishRequest request,
            Authentication authentication
    ) {
        log.info("에세이 발행 요청 - ID: {}, 사용자: {}, 상태: {}", id, authentication.getName(), request.getStatus());

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("인증이 필요합니다."));
        }

        try {
            String email = authentication.getName();
            EssayResponse response = essayService.publishEssay(email, id, request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalArgumentException e) {
            log.error("에세이 발행 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
            log.error("에세이 발행 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("에세이 발행 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("에세이 발행에 실패했습니다."));
        }
    }

    /**
     * 에세이 삭제
     */
    @Operation(
            summary = "에세이 삭제",
            description = "특정 에세이를 삭제합니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteEssay(
            @Parameter(description = "에세이 ID", example = "1")
            @PathVariable Long id,
            Authentication authentication
    ) {
        log.info("에세이 삭제 요청 - ID: {}, 사용자: {}", id, authentication.getName());

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("인증이 필요합니다."));
        }

        try {
            String email = authentication.getName();
            essayService.deleteEssay(email, id);
            return ResponseEntity.ok(ApiResponse.success("에세이가 삭제되었습니다."));
        } catch (RuntimeException e) {
            log.error("에세이 삭제 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("에세이 삭제 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("에세이 삭제에 실패했습니다."));
        }
    }
}
