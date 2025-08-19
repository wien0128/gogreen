package com.backend.gogreen.api.member.jwt.service;

import com.backend.gogreen.api.member.entity.Member;
import com.backend.gogreen.api.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final MemberRepository memberRepository;

    public void updateRefreshToken(String userId, String refreshToken) {
        memberRepository.findByUserId(userId).ifPresent(member -> {
            Member updatedMember = member.updateRefreshToken(refreshToken);
            memberRepository.save(updatedMember);
        });
    }
}
