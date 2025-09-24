package com.example.myownessay.controller.user;

import com.example.myownessay.common.response.ApiResponse;
import com.example.myownessay.dto.auth.request.DeleteAccountRequest;
import com.example.myownessay.dto.auth.request.UpdateProfileRequest;
import com.example.myownessay.dto.auth.response.ProfileResponse;
import com.example.myownessay.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name="사용자 관리 API", description="사용자 정보 조회 및 관리를 위한 API")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final AuthService authService;

    // 내 프로필 조회
    @Operation(
            summary = "내 프로필 조회",
            description = "현재 인증된 사용자의 프로필 정보를 조회합니다."
    )
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getMyProfile() {
        try {
            String email = "";
            ProfileResponse profile = authService.getProfile(email);
            return ResponseEntity.ok(ApiResponse.success(profile));
        } catch (Exception e) {
            log.error("사용자 정보 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("사용자 정보 조회에 실패했습니다."));
        }
    }

    // 내 프로필 업데이트
    @Operation(
            summary = "내 프로필 업데이트",
            description = "현재 인증된 사용자의 프로필 정보를 업데이트합니다."
    )
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<?>> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request
            ) {
        try {
            String email = "";
            ProfileResponse profile = authService.updateProfile(email, request);
            return ResponseEntity.ok(ApiResponse.success(profile));
        } catch (Exception e) {
            log.error("프로필 업데이트 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("프로필 업데이트에 실패했습니다."));
        }
    }

    // 내 계정 삭제
    @Operation(
            summary = "내 계정 삭제",
            description = "현재 인증된 사용자의 계정을 삭제합니다. (추후 구현 예정)"
    )
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<?>> deleteMyAccount(
            @Valid @RequestBody DeleteAccountRequest request
            ) {
        try {
            String email = "";
            authService.deleteAccount(email, request);
            return ResponseEntity.ok(ApiResponse.success("계정이 성공적으로 삭제되었습니다."));
        } catch (Exception e) {
            log.error("계정 삭제 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("계정 삭제에 실패했습니다."));
        }
    }
}
