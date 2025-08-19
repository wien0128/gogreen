package com.backend.gogreen.api.member.oauth2.dto;

import java.util.Map;


/***
 * 네이버는 응답 JSOM 중 response 키 안에 진짜 사용자 정보가 포함.
 * user-name-attribute 가 response로 설정되어 있음.
 */
public class NaverOAuth2UserInfo implements OAuth2UserInfo{

    private final Map<String, Object> response;

    public NaverOAuth2UserInfo(Map<String, Object> response) {
        this.response = response;
    }

    @Override
    public String getUserId() {
        return (String) response.get("id");
    }

    @Override
    public String getEmail() {
        return (String) response.get("email");
    }

    @Override
    public String getName() {
        return (String)  response.get("name");
    }
}
