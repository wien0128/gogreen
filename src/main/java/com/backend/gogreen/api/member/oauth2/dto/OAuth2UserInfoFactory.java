package com.backend.gogreen.api.member.oauth2.dto;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {

        registrationId = registrationId.toLowerCase();

        switch (registrationId) {
            case "google":
                return new GoogleOAuth2UserInfo(attributes);
            case "naver":
                // 네이버는 attributes 안에 "response"가 진짜 사용자 정보임
                Map<String, Object> response = (Map<String, Object>) attributes.get("response");
                return new NaverOAuth2UserInfo(response);
            case "kakao":
                return new KakaoOAuth2UserInfo(attributes);
            default:
                throw new IllegalArgumentException("Invalid registrationId: " + registrationId);
        }
    }
}
