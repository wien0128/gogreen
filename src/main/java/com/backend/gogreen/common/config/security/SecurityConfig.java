package com.backend.gogreen.common.config.security;

import com.backend.gogreen.api.member.oauth2.handler.OAuth2FailureHandler;
import com.backend.gogreen.api.member.oauth2.handler.OAuth2SuccessHandler;
import com.backend.gogreen.api.member.oauth2.service.CustomOAuth2UserService;
import com.backend.gogreen.common.config.jwt.JwtConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtConfig jwtConfig;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of(
                            "http://localhost:*",
                            "http://127.0.0.1:*"
                    ));
                    config.setAllowedMethods(List.of(
                            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
                    ));
                    config.setAllowedHeaders(List.of(
                            "Authorization",
                            "Authorization-Refresh",
                            "Content-Type",
                            "X-Requested-With",
                            "Accept",
                            "Origin"
                    ));
                    config.setMaxAge(3600L);
                    config.addExposedHeader("Authorization");
                    config.addExposedHeader("Authorization-Refresh");
                    return config;
                }))
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // Swagger, HealthCheck 엔드포인트 허가
                        .requestMatchers(
                                "/api-doc", "/health", "/v3/api-docs/**",
                                "/swagger-resources/**","/swagger-ui/**"
                        ).permitAll()
                        // 인덱스 페이지, 로그인, 회원가입 토큰 재발급 허가
                        .requestMatchers(
                                "/api/v1/member/login", "/api/v1/member/signup",
                                "/api/v1/member/token-reissue", "/oauth2/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                                // 인증 실패 시 예외 처리
                                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                                // 인가 실패 시 예외 처리
                                .accessDeniedHandler((req, resp, ex) -> {
                                    resp.setContentType("application/json");
                                    resp.setCharacterEncoding("UTF-8");
                                    resp.setStatus(HttpStatus.FORBIDDEN.value());
                                    resp.getWriter().write(
                                                    """
                                                    {
                                                        "status": 403,
                                                        "error": "FORBIDDEN",
                                                        "message": "접근 권한이 없습니다"
                                                    }
                                                    """);
                                })
                        )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/oauth2/authorize")
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2FailureHandler)
                        .failureHandler(oAuth2FailureHandler)
                )
                .addFilterBefore(jwtConfig.jwtAuthenticationProcessingFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Spring Security 최신 환경(5.x, 6.x, Boot 2.7~3.x) 에서는 빈 등록 불필요
     * Spring Boot 내부적으로 UserDetailsService 와 PasswordEncoder 빈을 감지해서
     * 적절한 AuthenticationManger 를 자동으로 생성 및 등록
     */
//    @Bean
//    public AuthenticationManager authenticationManager() {
//        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//        provider.setPasswordEncoder(passwordEncoder());
//        return new ProviderManager(provider);
//    }
}
