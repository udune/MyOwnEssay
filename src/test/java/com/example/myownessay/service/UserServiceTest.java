package com.example.myownessay.service;

import com.example.myownessay.dto.auth.request.DeleteAccountRequest;
import com.example.myownessay.dto.auth.request.UpdateProfileRequest;
import com.example.myownessay.dto.auth.response.ProfileResponse;
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
@DisplayName("사용자 관리 서비스 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setNickname("테스터");
        testUser.setPasswordHash("hashedPassword123");
        testUser.setTimezone("Asia/Seoul");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("프로필 조회 - 성공")
    void getProfile_성공() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        ProfileResponse result = authService.getProfile("test@example.com");

        // Then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("테스터", result.getNickname());
        assertEquals("Asia/Seoul", result.getTimezone());
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("프로필 수정 - 닉네임 변경 성공")
    void updateProfile_닉네임변경_성공() {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setNickname("새닉네임");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByNickname("새닉네임")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        ProfileResponse result = authService.updateProfile("test@example.com", request);

        // Then
        assertNotNull(result);
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(userRepository, times(1)).existsByNickname("새닉네임");
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("프로필 수정 - 닉네임 중복 실패")
    void updateProfile_닉네임중복_실패() {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setNickname("중복닉네임");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByNickname("중복닉네임")).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.updateProfile("test@example.com", request);
        });

        assertEquals("이미 존재하는 닉네임입니다.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("회원 탈퇴 - 성공")
    void deleteAccount_성공() {
        // Given
        DeleteAccountRequest request = new DeleteAccountRequest();
        request.setPassword("password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "hashedPassword123")).thenReturn(true);

        // When
        authService.deleteAccount("test@example.com", request);

        // Then
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(passwordEncoder, times(1)).matches("password123", "hashedPassword123");
        verify(userRepository, times(1)).delete(testUser);
    }

    @Test
    @DisplayName("회원 탈퇴 - 잘못된 비밀번호")
    void deleteAccount_잘못된비밀번호_실패() {
        // Given
        DeleteAccountRequest request = new DeleteAccountRequest();
        request.setPassword("wrongpassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "hashedPassword123")).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.deleteAccount("test@example.com", request);
        });

        assertEquals("비밀번호가 올바르지 않습니다.", exception.getMessage());
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    @DisplayName("프로필 조회 - 존재하지 않는 사용자")
    void getProfile_존재하지않는사용자_실패() {
        // Given
        when(userRepository.findByEmail("notexist@example.com")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.getProfile("notexist@example.com");
        });

        assertEquals("사용자를 찾을 수 없습니다.", exception.getMessage());
    }
}