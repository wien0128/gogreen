package com.backend.gogreen.api.member.oauth2.service;

import com.backend.gogreen.api.member.entity.Member;
import com.backend.gogreen.api.member.entity.Role;
import com.backend.gogreen.api.member.oauth2.dto.GoogleOAuth2UserInfo;
import com.backend.gogreen.api.member.oauth2.dto.KakaoOAuth2UserInfo;
import com.backend.gogreen.api.member.oauth2.dto.NaverOAuth2UserInfo;
import com.backend.gogreen.api.member.oauth2.dto.OAuth2UserInfo;
import com.backend.gogreen.api.member.repository.MemberRepository;
import com.backend.gogreen.common.config.security.SecurityMember;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId().toUpperCase();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        OAuth2UserInfo userInfo;
        switch (registrationId) {
            case "google":
                userInfo = new GoogleOAuth2UserInfo(attributes);
                break;
            case "naver":
                userInfo = new NaverOAuth2UserInfo(
                        (Map<String, Object>) attributes.get("response")
                );
                break;
            case "kakao":
                userInfo = new KakaoOAuth2UserInfo(attributes);
                break;
            default:
                throw new OAuth2AuthenticationException("지원하지 않는 OAuth2 공급자: " + registrationId);
        }

        if (userInfo.getEmail() == null || userInfo.getEmail().isBlank()) {
            throw new OAuth2AuthenticationException("등록되지 않은 이메일입니다.");
        }

        Member member = memberRepository.findByEmail(userInfo.getEmail())
                .orElseGet(() -> {
                    Member newMember = Member.builder()
                            .userId(generateUserId(userInfo, registrationId))
                            .email(userInfo.getEmail())
                            .role(Role.USER)
                            .build();
                    return memberRepository.save(newMember);
                });

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority(member.getRole().name())),
                attributes,
                "email"
        );
    }

    private String generateUserId(OAuth2UserInfo userInfo, String provider) {
        String id = userInfo.getUserId();
        // 예시: google_102394023, kakao_3823902
        return provider + "_" + (id != null ? id : userInfo.getEmail());
    }
}
