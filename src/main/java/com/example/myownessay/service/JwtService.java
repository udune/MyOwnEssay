package com.example.myownessay.service;

import com.example.myownessay.common.exception.AuthErrorCode;
import com.example.myownessay.common.exception.AuthException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

// JWT 관련 기능을 제공하는 서비스 클래스
@Service
@Slf4j
public class JwtService {
    private final SecretKey secretKey;
    private final long jwtExpiration;
    private final long refreshExpiration;

    // 생성자 주입을 통한 설정 값 초기화
    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long jwtExpiration,
            @Value("${jwt.refresh-expiration}") long refreshExpiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.jwtExpiration = jwtExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    // JWT에서 사용자 이름(Subject) 추출
    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (ExpiredJwtException e) {
            log.warn("만료된 토큰에서 사용자명 추출 시도");
            throw new AuthException(AuthErrorCode.EXPIRED_TOKEN);
        } catch (MalformedJwtException e) {
            log.warn("잘못된 형식의 토큰");
            throw new AuthException(AuthErrorCode.MALFORMED_TOKEN);
        } catch (SecurityException e) {
            log.warn("토큰 서명 검증 실패");
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 토큰 형식");
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        } catch (IllegalArgumentException e) {
            log.warn("빈 토큰 또는 null 토큰");
            throw new AuthException(AuthErrorCode.TOKEN_NOT_FOUND);
        } catch (Exception e) {
            log.error("토큰에서 사용자명 추출 중 예상치 못한 오류: {}", e.getMessage());
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }
    }

    // JWT에서 특정 클레임 추출
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 액세스 토큰 생성
    public String generateToken(String username) {
        return generateToken(new HashMap<>(), username);
    }

    // 추가 클레임을 포함한 액세스 토큰 생성
    public String generateToken(Map<String, Object> extraClaims, String username) {
        try {
            return buildToken(extraClaims, username, jwtExpiration);
        } catch (Exception e) {
            log.error("토큰 생성 중 오류 발생: {}", e.getMessage());
            throw new AuthException(AuthErrorCode.AUTHENTICATION_FAILED, "토큰 생성 중 오류가 발생했습니다.");
        }
    }

    // 리프레시 토큰 생성
    public String generateRefreshToken(String username) {
        try {
            return buildToken(new HashMap<>(), username, refreshExpiration);
        } catch (Exception e) {
            log.error("리프레시 토큰 생성 중 오류 발생: {}", e.getMessage());
            throw new AuthException(AuthErrorCode.AUTHENTICATION_FAILED, "리프레시 토큰 생성 중 오류가 발생했습니다.");
        }
    }

    // 토큰 생성 로직
    private String buildToken(Map<String, Object> extraClaims, String username, long expiration) {
        long currentTime = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(username)
                .setIssuedAt(new Date(currentTime))
                .setExpiration(new Date(currentTime + expiration))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰 유효성 검사
    public boolean isTokenValid(String token, String username) {
        try {
            final String tokenUsername = extractUsername(token);
            boolean isValid = (tokenUsername.equals(username)) && !isTokenExpired(token);

            log.debug("Token validation for user '{}': {}", username, isValid);
            return isValid;
        } catch (Exception e) {
            log.warn("토큰 유효성 검사 중 오류 발생: {}", e.getMessage());
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }
    }

    // 토큰 만료 여부 확인
    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (AuthException e) {
            if (e.getErrorCode() == AuthErrorCode.EXPIRED_TOKEN) {
                return true; // 만료된 토큰이면 true 반환
            }
            throw e;
        } catch (Exception e) {
            log.warn("토큰 만료 여부 확인 중 오류 발생: {}", e.getMessage());
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }
    }

    // 토큰 만료 시간 추출
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // 모든 클레임 추출
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("만료된 토큰으로 클레임 추출 시도");
            throw new AuthException(AuthErrorCode.EXPIRED_TOKEN);
        } catch (MalformedJwtException e) {
            log.warn("잘못된 형식의 토큰으로 클레임 추출 시도");
            throw new AuthException(AuthErrorCode.MALFORMED_TOKEN);
        } catch (SecurityException e) {
            log.warn("토큰 서명이 유효하지 않음");
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰");
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 비어있음");
            throw new AuthException(AuthErrorCode.TOKEN_NOT_FOUND);
        } catch (Exception e) {
            log.error("토큰 파싱 중 예상치 못한 오류: {}", e.getMessage());
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }
    }

    // 토큰 유효성 검사를 조용히 수행 (예외를 던지지 않고 false 반환)
    public boolean isTokenValidSilently(String token, String username) {
        try {
            return isTokenValid(token, username);
        } catch (AuthException e) {
            log.warn("토큰 유효성 검사 실패: {}", e.getMessage());
            return false;
        }
    }
}
