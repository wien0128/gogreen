package com.backend.gogreen.api.member.oauth2.service;

import com.backend.gogreen.api.member.entity.Member;
import com.backend.gogreen.api.member.oauth2.dto.OAuth2UserInfo;
import com.backend.gogreen.api.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {


    }

    @Transactional
    private OAuth2User processOAuth2User(OAuth2UserInfo userInfo) throws OAuth2AuthenticationException {

        Optional<Member> optionalMember = memberRepository.findByEmail(userInfo.getEmail());

        Member member;
    }
}
