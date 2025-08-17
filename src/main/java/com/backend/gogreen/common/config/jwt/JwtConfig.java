package com.backend.gogreen.common.config.jwt;

import com.backend.gogreen.api.member.jwt.filter.JwtAuthenticationProcessingFilter;
import com.backend.gogreen.api.member.jwt.service.JwtService;
import com.backend.gogreen.api.member.jwt.service.RefreshTokenService;
import com.backend.gogreen.api.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class JwtConfig {

    private final JwtService jwtService;
    private final MemberRepository memberRepository;
    private final RefreshTokenService refreshTokenService;

    @Bean
    public JwtAuthenticationProcessingFilter jwtAuthenticationProcessingFilter() {
        return new JwtAuthenticationProcessingFilter(jwtService, memberRepository, refreshTokenService);
    }
}
