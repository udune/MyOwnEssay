package com.example.myownessay.service;

import com.example.myownessay.dto.auth.request.LoginRequest;
import com.example.myownessay.dto.auth.request.RegisterRequest;
import com.example.myownessay.dto.auth.response.TokenResponse;
import com.example.myownessay.dto.auth.UserInfo;
import com.example.myownessay.entity.User;
import com.example.myownessay.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 데이터 준비
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setNickname("테스터");
        testUser.setPasswordHash("hashedPassword123");
        testUser.setCreatedAt(LocalDateTime.now());

        registerRequest = new RegisterRequest("test@example.com", "password123", "테스터");
        loginRequest = new LoginRequest("test@example.com", "password123");
    }

    @Test
    @DisplayName("회원가입 - 성공")
    void register_성공() {
        // Given - 모킹 설정
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByNickname("테스터")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When - 실제 메서드 호출
        UserInfo result = authService.register(registerRequest);

        // Then - 결과 검증
        assertNotNull(result, "결과가 null이면 안됨");
        assertEquals("test@example.com", result.getEmail(), "이메일이 일치해야 함");
        assertEquals("테스터", result.getNickname(), "닉네임이 일치해야 함");

        // 메서드 호출 검증
        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(userRepository, times(1)).existsByNickname("테스터");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 - 이메일 중복 실패")
    void register_이메일중복_실패() {
        // Given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("이미 사용 중인 이메일입니다.", exception.getMessage());

        // 이메일 중복 체크 후 더 이상 진행되지 않음을 확인
        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(userRepository, never()).existsByNickname(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 - 닉네임 중복 실패")
    void register_닉네임중복_실패() {
        // Given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByNickname("테스터")).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("이미 사용 중인 닉네임입니다.", exception.getMessage());

        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(userRepository, times(1)).existsByNickname("테스터");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("로그인 - 성공")
    void login_성공() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "hashedPassword123")).thenReturn(true);
        when(jwtService.generateToken("test@example.com")).thenReturn("accessToken123");
        when(jwtService.generateRefreshToken("test@example.com")).thenReturn("refreshToken123");

        // When
        TokenResponse result = authService.login(loginRequest);

        // Then
        assertNotNull(result, "결과가 null이면 안됨");
        assertEquals("accessToken123", result.getAccessToken(), "액세스 토큰이 일치해야 함");
        assertEquals("refreshToken123", result.getRefreshToken(), "리프레시 토큰이 일치해야 함");
        assertEquals("Bearer", result.getTokenType(), "토큰 타입이 Bearer여야 함");
        assertEquals(86400, result.getExpiresIn(), "만료 시간이 일치해야 함");
        assertEquals("test@example.com", result.getUser().getEmail(), "사용자 이메일이 일치해야 함");

        // 메서드 호출 검증
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(passwordEncoder, times(1)).matches("password123", "hashedPassword123");
        verify(jwtService, times(1)).generateToken("test@example.com");
        verify(jwtService, times(1)).generateRefreshToken("test@example.com");
    }

    @Test
    @DisplayName("로그인 - 존재하지 않는 사용자")
    void login_존재하지않는사용자_실패() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("사용자를 찾을 수 없습니다.", exception.getMessage());

        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("로그인 - 잘못된 비밀번호")
    void login_잘못된비밀번호_실패() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "hashedPassword123")).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("잘못된 비밀번호입니다.", exception.getMessage());

        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(passwordEncoder, times(1)).matches("password123", "hashedPassword123");
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("현재 사용자 정보 조회 - 성공")
    void getCurrentUser_성공() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        UserInfo result = authService.getCurrentUser("test@example.com");

        // Then
        assertNotNull(result, "결과가 null이면 안됨");
        assertEquals(testUser.getId(), result.getId(), "사용자 ID가 일치해야 함");
        assertEquals("test@example.com", result.getEmail(), "이메일이 일치해야 함");
        assertEquals("테스터", result.getNickname(), "닉네임이 일치해야 함");

        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("현재 사용자 정보 조회 - 존재하지 않는 사용자")
    void getCurrentUser_존재하지않는사용자_실패() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.getCurrentUser("test@example.com");
        });

        assertEquals("사용자를 찾을 수 없습니다.", exception.getMessage());
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }
}