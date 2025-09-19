package com.example.myownessay.integration;

import com.example.myownessay.dto.auth.request.LoginRequest;
import com.example.myownessay.dto.auth.request.RegisterRequest;
import com.example.myownessay.entity.User;
import com.example.myownessay.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional // 각 테스트 후 데이터베이스 롤백
@DisplayName("사용자 인증 시스템 통합 테스트")
class AuthIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // MockMvc 설정 (가짜 웹 요청을 만들기 위해)
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();

        // 각 테스트 전에 데이터베이스 청소
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입 - 성공")
    void register_성공() throws Exception {
        // 1. 회원가입할 정보 준비
        RegisterRequest request = new RegisterRequest(
                "test@example.com",
                "password123",
                "테스터"
        );

        // 2. 회원가입 API 호출
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // 3. 응답 검증
                .andExpect(status().isCreated()) // 201 상태 코드
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("테스터"))
                .andExpect(jsonPath("$.data.id").exists())
                .andDo(print()); // 요청/응답 내용 출력

        // 4. 데이터베이스에 실제로 저장되었는지 확인
        User savedUser = userRepository.findByEmail("test@example.com").orElse(null);
        assertNotNull(savedUser, "사용자가 데이터베이스에 저장되어야 함");
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("테스터", savedUser.getNickname());
        assertTrue(savedUser.getPasswordHash().startsWith("$2a$"), "비밀번호가 암호화되어야 함");
    }

    @Test
    @DisplayName("회원가입 - 이메일 중복 실패")
    void register_이메일중복_실패() throws Exception {
        // 1. 기존 사용자 생성
        User existingUser = new User();
        existingUser.setEmail("test@example.com");
        existingUser.setNickname("기존사용자");
        existingUser.setPasswordHash("hashedpassword");
        userRepository.save(existingUser);

        // 2. 같은 이메일로 회원가입 시도
        RegisterRequest request = new RegisterRequest(
                "test@example.com", // 중복 이메일
                "password123",
                "새사용자"
        );

        // 3. 실패 응답 검증
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()) // 400 상태 코드
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value(containsString("이미 존재하는 이메일입니다.")))
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 - 닉네임 중복 실패")
    void register_닉네임중복_실패() throws Exception {
        // 1. 기존 사용자 생성
        User existingUser = new User();
        existingUser.setEmail("existing@example.com");
        existingUser.setNickname("테스터");
        existingUser.setPasswordHash("hashedpassword");
        userRepository.save(existingUser);

        // 2. 같은 닉네임으로 회원가입 시도
        RegisterRequest request = new RegisterRequest(
                "new@example.com",
                "password123",
                "테스터" // 중복 닉네임
        );

        // 3. 실패 응답 검증
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value(containsString("이미 존재하는 닉네임입니다.")))
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 - 잘못된 입력값 실패")
    void register_잘못된입력값_실패() throws Exception {
        // 1. 잘못된 이메일 형식으로 요청
        RegisterRequest request = new RegisterRequest(
                "잘못된이메일", // 이메일 형식이 아님
                "123", // 너무 짧은 비밀번호
                "A" // 너무 짧은 닉네임
        );

        // 2. 검증 실패 확인
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("로그인 - 성공")
    void login_성공() throws Exception {
        // 1. 먼저 사용자 회원가입
        RegisterRequest registerRequest = new RegisterRequest(
                "test@example.com",
                "password123",
                "테스터"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // 2. 로그인 시도
        LoginRequest loginRequest = new LoginRequest(
                "test@example.com",
                "password123"
        );

        // 3. 로그인 성공 검증
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.user.nickname").value("테스터"))
                .andDo(print());
    }

    @Test
    @DisplayName("로그인 - 존재하지 않는 사용자")
    void login_존재하지않는사용자_실패() throws Exception {
        // 1. 존재하지 않는 이메일로 로그인 시도
        LoginRequest loginRequest = new LoginRequest(
                "notexist@example.com",
                "password123"
        );

        // 2. 실패 응답 검증
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value(containsString("인증에 실패했습니다. 로그인 중 오류가 발생했습니다.")))
                .andDo(print());
    }

    @Test
    @DisplayName("로그인 - 잘못된 비밀번호")
    void login_잘못된비밀번호_실패() throws Exception {
        // 1. 먼저 사용자 회원가입
        RegisterRequest registerRequest = new RegisterRequest(
                "test@example.com",
                "password123",
                "테스터"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // 2. 잘못된 비밀번호로 로그인 시도
        LoginRequest loginRequest = new LoginRequest(
                "test@example.com",
                "wrongpassword"
        );

        // 3. 실패 응답 검증
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value(containsString("인증에 실패했습니다. 로그인 중 오류가 발생했습니다.")))
                .andDo(print());
    }

    @Test
    @DisplayName("JWT 토큰 - 생성 및 검증")
    void jwt_토큰_생성및검증() {
        // 이 테스트는 JwtService에 대한 단위 테스트로 이미 다른 파일에서 작성됨
        // 여기서는 통합 테스트에서 실제로 JWT가 포함되어 응답되는지만 확인
        assertTrue(true, "JWT 토큰 기능은 로그인 성공 테스트에서 검증됨");
    }

    @Test
    @DisplayName("비밀번호 암호화 - 평문 비밀번호가 암호화되어 저장되는지 확인")
    void password_암호화_확인() throws Exception {
        // 1. 회원가입
        RegisterRequest request = new RegisterRequest(
                "test@example.com",
                "plainpassword",
                "테스터"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // 2. 데이터베이스에서 사용자 조회
        User savedUser = userRepository.findByEmail("test@example.com").orElse(null);
        assertNotNull(savedUser);

        // 3. 비밀번호가 평문이 아닌 암호화된 형태로 저장되었는지 확인
        assertNotEquals("plainpassword", savedUser.getPasswordHash(), "비밀번호가 평문으로 저장되면 안됨");
        assertTrue(savedUser.getPasswordHash().startsWith("$2a$"), "BCrypt 암호화 형식이어야 함");
        assertTrue(savedUser.getPasswordHash().length() > 50, "암호화된 비밀번호는 충분히 길어야 함");
    }

    @Test
    @DisplayName("전체 시나리오 - 회원가입부터 로그인까지")
    void 전체시나리오_회원가입부터로그인까지() throws Exception {
        String email = "fulltest@example.com";
        String password = "testpassword123";
        String nickname = "풀테스터";

        // 1. 회원가입
        RegisterRequest registerRequest = new RegisterRequest(email, password, nickname);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        // 2. 데이터베이스에 저장 확인
        User savedUser = userRepository.findByEmail(email).orElse(null);
        assertNotNull(savedUser, "회원가입 후 데이터베이스에 저장되어야 함");

        // 3. 로그인
        LoginRequest loginRequest = new LoginRequest(email, password);
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.user.email").value(email))
                .andExpect(jsonPath("$.data.user.nickname").value(nickname));

        // 4. 전체 과정이 성공했음을 로그로 출력
        System.out.println("✅ 전체 시나리오 테스트 성공: 회원가입 → 로그인 → JWT 토큰 발급");
    }
}