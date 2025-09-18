package com.example.myownessay.service;

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

    @Transactional
    public UserInfo register(RegisterRequest request) {
        log.info("회원가입 시도: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }

        if (userRepository.existsByNickname(request.getNickname())) {
            throw new RuntimeException("이미 사용 중인 닉네임입니다.");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);
        log.info("회원가입 성공: {}", savedUser.getId());

        return mapToUserInfo(savedUser);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        log.info("로그인 시도: {}", request.getEmail());

        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("잘못된 비밀번호입니다.");
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
    }

    @Transactional(readOnly = true)
    public UserInfo getCurrentUser(String email) {
        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return mapToUserInfo(user);
    }

    private UserInfo mapToUserInfo(User user) {
        return new UserInfo(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getCreatedAt()
        );
    }
}
