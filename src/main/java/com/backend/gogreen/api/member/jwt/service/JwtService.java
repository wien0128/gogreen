package com.backend.gogreen.api.member.jwt.service;

import com.backend.gogreen.api.member.entity.Role;
import com.backend.gogreen.api.member.repository.MemberRepository;
import com.backend.gogreen.common.exception.UnauthorizedException;
import com.backend.gogreen.common.response.ErrorStatus;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.awt.*;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Service
@Getter
@Slf4j
public class JwtService {

    private final SecretKey secretKey;

    private final Long accessTokenExpirationPeriod;

    private final Long refreshTokenExpirationPeriod;

    private final MemberRepository memberRepository;

    private final RefreshTokenService refreshTokenService;

    public JwtService(@Value("${jwt.secretKey}") String secretKey,
                      @Value("${jwt.access.expiration}") Long accessTokenExpirationPeriod,
                      @Value("${jwt.refresh.expiration}") Long refreshTokenExpirationPeriod,
                      MemberRepository memberRepository,
                      RefreshTokenService refreshTokenService) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpirationPeriod = accessTokenExpirationPeriod;
        this.refreshTokenExpirationPeriod = refreshTokenExpirationPeriod;
        this.memberRepository = memberRepository;
        this.refreshTokenService = refreshTokenService;
    }

    // Access Token 발급
    public String createAccessToken(String memberId, String email, Role role) {

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationPeriod);

        return Jwts.builder()
                .setSubject(memberId)
                .claim("email", email)
                .claim("role", role.name())
                .claim("type", "ACCESS")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // Refresh Token 발급
    public String createRefreshToken(String memberId) {
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationPeriod);

        return Jwts.builder()
                .setSubject(memberId)
                .claim("type", "REFRESH")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // Access, RefreshToken 발급
    public Map<String, String> createAccessAndRefreshToken(String memberId, String email, Role role) {

        String accessToken = createAccessToken(memberId, email, role);
        String refreshToken = createRefreshToken(memberId);

        refreshTokenService.updateRefreshToken(memberId, refreshToken);

        log.info("Access Token and Refresh Token 발급 성공");
        log.info("Access Token : {}", accessToken);
        log.info("Refresh Token : {}", refreshToken);

        return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
    }

    // 토큰 유혀성 검증
    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT token: {}", e.getMessage());
            throw new UnauthorizedException(ErrorStatus.TOKEN_EXPIRED.getMessage());
        } catch (MalformedJwtException e) {
            log.info("Malformed JWT token: {}", e.getMessage());
            throw new UnauthorizedException(ErrorStatus.INVALID_TOKEN.getMessage());
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT token: {}", e.getMessage());
            throw new UnauthorizedException(ErrorStatus.UNSUPPORTED_TOKEN.getMessage());
        } catch (IllegalArgumentException e) {
            log.info("Empty JWT token: {}", e.getMessage());
            throw new UnauthorizedException(ErrorStatus.EMPTY_TOKEN.getMessage());
        }
    }

    // 토큰 클레임 추출
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 토큰에서 유저아이디 추출
    public Optional<String> extractMemberIdFromToken(String accessToken) {
        try {
            Claims claims = parseClaims(accessToken);
            return Optional.ofNullable(claims.getSubject());
        } catch (Exception e) {
            log.error("Failed extract from memberId: {}", e.getMessage());
            return Optional.empty();
        }
    }

    // 토큰에서 이메일 추출
    public Optional<String> extractEmailFromToken(String accessToken) {
        try {
            Claims claims = parseClaims(accessToken);
            return Optional.ofNullable(claims.get("email", String.class));
        } catch (Exception e) {
            log.error("Failed extract from email: {}", e.getMessage());
            return Optional.empty();
        }
    }

    // 토큰에서 권한 추출
    public Optional<String> extractRoleFromToken(String accessToken) {
        try {
            Claims claims = parseClaims(accessToken);
            return Optional.ofNullable(claims.get("role", String.class));
        }  catch (Exception e) {
            log.error("Failed extract from role: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
