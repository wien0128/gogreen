package com.backend.gogreen.api.member.oauth2.handler;

import com.backend.gogreen.api.member.entity.Member;
import com.backend.gogreen.api.member.jwt.service.JwtService;
import com.backend.gogreen.api.member.oauth2.dto.OAuth2UserInfo;
import com.backend.gogreen.api.member.oauth2.dto.OAuth2UserInfoFactory;
import com.backend.gogreen.api.member.repository.MemberRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final MemberRepository memberRepository;

//    @Value("${}")
    private final String redirectUri = "http://localhost:3000/oauth2/redirect";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oauth2User.getAttributes();

        String registrationId = request.getRequestURI();
        // 또는 userRequest 통해 얻는 방식을 권장하지만 여기선 간단화

        // 공급자별 OAuth2UserInfo 생성 (기존 Factory 패턴 사용 권장)
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);

        // 이메일 필수 체크
        if (userInfo.getEmail() == null || userInfo.getEmail().isBlank()) {
            throw new RuntimeException("이메일 정보를 찾을 수 없습니다.");
        }

        // 회원 조회 (필요 시 가입/갱신 로직 추가)
        Member member = memberRepository.findByEmail(userInfo.getEmail())
                .orElseThrow(() -> new RuntimeException("등록된 회원이 아닙니다."));

        // JWT 토큰 생성
        String accessToken = jwtService.createAccessToken(member.getUserId(), member.getEmail(), member.getRole());
        String refreshToken = jwtService.createRefreshToken(member.getUserId());

        // 리다이렉트 URL 생성 (토큰 전달)
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", accessToken)
                .queryParam("refresh", refreshToken)
                .build().toUriString();

        log.info("OAuth2 로그인 성공. 사용자 이메일: {}, 리다이렉트 URL: {}", member.getEmail(), targetUrl);

        response.sendRedirect(targetUrl);
    }
}
