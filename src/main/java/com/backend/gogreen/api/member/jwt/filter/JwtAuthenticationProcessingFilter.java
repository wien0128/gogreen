package com.backend.gogreen.api.member.jwt.filter;

import com.backend.gogreen.api.member.entity.Member;
import com.backend.gogreen.api.member.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {

    @Value("${jwt.access.header}")
    private String accessTokenHeader;

    @Value("${jwt.refresh.header}")
    private String refreshTokenHeader;

    private static final String TOKEN_REISSUE_URL = "/api/v1/member/token-reissue";

    private final JwtService jwtService;
    private final MemberRepository memberRepository;

    private static final String[] SWAGGER_URIS = {
            "/swagger-ui",
            "/v3/api-docs",
            "/swagger-ui/index.html",
    };

    // 스웨거 관련 경로 필터링 제외 -> 디버깅 편의성
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String requestURI = request.getRequestURI();

        for (String uri : SWAGGER_URIS) {
            if (requestURI.contains(uri)) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        // Refresh Token 재발급 로직은 /token-reissue 엔드포인트로 제한
        if (requestURI.equals(TOKEN_REISSUE_URL)) {

            // 유효한 Refresh Token 이 존재하는지 검증
            Optional<String> refreshToken = extractToken(request, refreshTokenHeader)
                    .filter(jwtService::isTokenValid);

            // Refresh Token 이 유효하다면 Access Token 을 재발급 후 인증 정보를 재설정
            if (refreshToken.isPresent()) {
                handleRefreshToken(response, refreshToken.get());
            }

            filterChain.doFilter(request, response);
            return;
        }

        // 유효한 Access Token 이 존재하는지 검증
        Optional<String> accessToken = extractToken(request, accessTokenHeader)
                .filter(jwtService::isTokenValid);


    }

    // Refresh Token, Access Token 재발급 및 인증 핸들러
    private void handleRefreshToken(HttpServletResponse response, String refreshToken) throws IOException {

        memberRepository.findByRefreshToken(refreshToken)
                .ifPresent(user -> {
                    String newAccessToken = jwtService.createAccessToken(user.getId());
                    String newRefreshToken = jwtService.createRefreshToken(user.getId());

                    // Refresh Token 갱신
                    jwtService.updateRefreshToken(user.getId(), newRefreshToken);

                    // 새로운 Access Token, Refresh Token 헤더로 재설정
                    response.setHeader(accessTokenHeader, "Bearer " + newAccessToken);
                    response.setHeader(refreshTokenHeader, "Bearer " + newRefreshToken);

                    log.info("Access Token and Refresh Token update SUCCESSFULLY ---");
                    log.info("Access Token : {}", newAccessToken);
                    log.info("Refresh Token : {}", newRefreshToken);
                });
    }

    // 요청 헤더의 토큰 추출 유틸 메서드
    private Optional<String> extractToken(HttpServletRequest request, String header) {

        String bearerToken = request.getHeader(header);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return Optional.of(bearerToken.substring(7));
        }
        
        return Optional.empty();
    }

    // SecurityContext에 사용자 인증 정보를 등록
    private void setAuthentication(Member member) {

        SecurityMember securityMember = SecurityMember.builder()
                .id(member.getId())
                .email(member.getEmail())
                .password(member.getPassword())
                .role(member.getRole())
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                securityMember, null, null);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
