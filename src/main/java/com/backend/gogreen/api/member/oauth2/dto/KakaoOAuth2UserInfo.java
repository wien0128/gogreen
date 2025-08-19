package com.backend.gogreen.api.member.oauth2.dto;

import java.util.Map;

/***
 * 카카오는 id 가 최상위에 위치
 * 프로필 정보는 properties 및 kakao_account 하위에 위치
 */
public class KakaoOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getUserId() {
        // user-name-attribute 가 id 이므로 최상위에서 꺼냄
        return attributes.get("id").toString();
    }

    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

        if (kakaoAccount != null) {
            return null;
        }

        return (String)  kakaoAccount.get("email");
    }

    @Override
    public String getName() {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");

        if (properties == null) {
            return null;
        }

        return (String) properties.get("nickname");
    }
}
