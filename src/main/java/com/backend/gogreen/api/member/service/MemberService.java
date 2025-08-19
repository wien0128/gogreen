package com.backend.gogreen.api.member.service;

import com.backend.gogreen.api.member.dto.MemberLoginRequestDTO;
import com.backend.gogreen.api.member.dto.MemberLoginResponseDTO;
import com.backend.gogreen.api.member.dto.MemberSignupRequestDTO;
import com.backend.gogreen.api.member.entity.Member;
import com.backend.gogreen.api.member.entity.Role;
import com.backend.gogreen.api.member.jwt.service.JwtService;
import com.backend.gogreen.api.member.repository.MemberRepository;
import com.backend.gogreen.common.exception.BadRequestException;
import com.backend.gogreen.common.exception.BaseException;
import com.backend.gogreen.common.exception.NotFoundException;
import com.backend.gogreen.common.exception.UnauthorizedException;
import com.backend.gogreen.common.response.ErrorStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;


    public void signupMember(MemberSignupRequestDTO signupRequestDTO) {

        // 사용자 ID 중복 확인
        if (memberRepository.findByUserId(signupRequestDTO.getUserId()).isPresent()) {
            throw new BaseException(ErrorStatus.BAD_REQUEST_DUPLICATE_USERID.getHttpStatus(),
                    ErrorStatus.BAD_REQUEST_DUPLICATE_USERID.getMessage());
        }

        // 이메일 중복 확인
        if (memberRepository.findByEmail(signupRequestDTO.getEmail()).isPresent()) {
            throw new BaseException(ErrorStatus.BAD_REQUEST_DUPLICATE_EMAIL.getHttpStatus(),
                    ErrorStatus.BAD_REQUEST_DUPLICATE_EMAIL.getMessage());
        }

        Member member = Member.builder()
                .userId(signupRequestDTO.getUserId())
                .email(signupRequestDTO.getEmail())
                .password(passwordEncoder.encode(signupRequestDTO.getPassword()))
                .role(Role.USER)
                .build();

        memberRepository.save(member);
    }

    public MemberLoginResponseDTO login(MemberLoginRequestDTO loginRequestDTO) {

        Member member = memberRepository.findById(loginRequestDTO.getUserId())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.RESOURCE_NOT_FOUND.getMessage()));

        if (!passwordEncoder.matches(loginRequestDTO.getPassword(), member.getPassword())) {
            throw new BadRequestException(ErrorStatus.UNAUTHORIZED_EMAIL_OR_PASSWORD.getMessage());
        }

        Map<String, String> tokens = jwtService.createAccessAndRefreshToken(member.getUserId(), member.getEmail(), member.getRole());

        return new MemberLoginResponseDTO(
                member.getUserId(),
                member.getEmail(),
                member.getRole().toString(),
                tokens.get("accessToken"),
                tokens.get("refreshToken")
        );
    }

    public Map<String, String> reissueTokens(String refreshToken) {

        if (!jwtService.isTokenValid(refreshToken)) {
            throw new UnauthorizedException(ErrorStatus.UNAUTHORIZED_REFRESH_TOKEN_EXCEPTION.getMessage());
        }

        Member member = memberRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new UnauthorizedException(ErrorStatus.UNAUTHORIZED_REFRESH_TOKEN_EXCEPTION.getMessage()));

        Map<String, String> tokens = jwtService.createAccessAndRefreshToken(member.getUserId(), member.getEmail(), member.getRole());

        member.updateRefreshToken(tokens.get("refreshToken"));

        memberRepository.save(member);

        return Map.of(
                "accessToken", tokens.get("accessToken"),
                "refreshToken", tokens.get("refreshToken")
        );
    }

    public void verifyMemberPassword(String rawPassword, String userId) {

        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.RESOURCE_NOT_FOUND.getMessage()));

        if (!passwordEncoder.matches(rawPassword, member.getPassword())) {
            throw new BadRequestException(ErrorStatus.UNAUTHORIZED_EMAIL_OR_PASSWORD.getMessage());
        }
    }
}
