package com.example.myownessay.service;

import com.example.myownessay.common.exception.AuthErrorCode;
import com.example.myownessay.common.exception.AuthException;
import com.example.myownessay.dto.auth.UserInfo;
import com.example.myownessay.dto.auth.request.LoginRequest;
import com.example.myownessay.dto.auth.request.RegisterRequest;
import com.example.myownessay.dto.auth.response.TokenResponse;
import com.example.myownessay.entity.User;
import com.example.myownessay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;

    // 회원가입 처리
    @Transactional
    public UserInfo register(RegisterRequest request) {
        log.info("회원가입 시도: {}", request.getEmail());

        // 이메일 중복 검사
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("이미 사용 중인 이메일로 회원가입 시도: {}", request.getEmail());
            throw new AuthException(AuthErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 닉네임 중복 검사
        if (userRepository.existsByNickname(request.getNickname())) {
            log.warn("이미 사용 중인 닉네임으로 회원가입 시도: {}", request.getNickname());
            throw new AuthException(AuthErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        try {
            // 사용자 생성
            User user = new User();
            user.setEmail(request.getEmail());
            user.setNickname(request.getNickname());
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

            // 데이터베이스에 저장
            User savedUser = userRepository.save(user);
            log.info("회원가입 성공: {}", savedUser.getId());

            return mapToUserInfo(savedUser);
        } catch (Exception e) {
            log.warn("회원가입 실패: {} - {}", request.getEmail(), e.getMessage());
            throw new AuthException(AuthErrorCode.AUTHENTICATION_FAILED, "회원가입 중 오류가 발생했습니다.");
        }

    }

    // 로그인 처리
    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        log.info("로그인 시도: {}", request.getEmail());

        try {
            // 이메일로 사용자 조회
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() ->{
                        log.warn("존재하지 않는 이메일로 로그인 시도: {}", request.getEmail());
                        return new AuthException(AuthErrorCode.USER_NOT_FOUND);
                    });

            // 계정 활성화 여부 확인
            if (!user.getIsActive()) {
                throw new AuthException(AuthErrorCode.ACCOUNT_DISABLED);
            }

            // 비밀번호 검증
            if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                throw new AuthException(AuthErrorCode.INVALID_PASSWORD);
            }

            // JWT 토큰 생성
            String accessToken = jwtService.generateToken(user.getEmail());
            String refreshToken = jwtService.generateRefreshToken(user.getEmail());

            log.info("로그인 성공: {}", user.getId());

            return new TokenResponse(
                    accessToken,
                    refreshToken,
                    "Bearer",
                    86400,
                    mapToUserInfo(user)
            );
        } catch (Exception e) {
            log.error("로그인 중 오류 발생: {} - {}", request.getEmail(), e.getMessage());
            throw new AuthException(AuthErrorCode.AUTHENTICATION_FAILED, "로그인 중 오류가 발생했습니다.");
        }
    }

    // 현재 사용자 정보 조회
    @Transactional(readOnly = true)
    public UserInfo getCurrentUser(String email) {
        log.debug("현재 사용자 정보 조회: {}", email);

        try {
            // 이메일로 사용자 조회
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("존재하지 않는 이메일로 사용자 정보 조회 시도: {}", email);
                        return new AuthException(AuthErrorCode.USER_NOT_FOUND);
                    });

            if (!user.getIsActive()) {
                log.warn("비활성화된 계정으로 사용자 정보 조회 시도: {}", email);
                throw new AuthException(AuthErrorCode.ACCOUNT_DISABLED);
            }

            return mapToUserInfo(user);
        } catch (Exception e) {
            log.error("사용자 정보 조회 중 오류 발생: {} - {}", email, e.getMessage());
            throw new AuthException(AuthErrorCode.AUTHENTICATION_FAILED, "사용자 정보 조회 중 오류가 발생했습니다.");
        }
    }

    // User 엔티티를 UserInfo DTO로 매핑
    private UserInfo mapToUserInfo(User user) {
        return new UserInfo(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getCreatedAt()
        );
    }
}
