package com.backend.gogreen.api.member.controller;

import com.backend.gogreen.api.member.dto.MemberSignupRequestDTO;
import com.backend.gogreen.api.member.service.MemberService;
import com.backend.gogreen.common.exception.BadRequestException;
import com.backend.gogreen.common.response.ApiResponse;
import com.backend.gogreen.common.response.ErrorStatus;
import com.backend.gogreen.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member", description = "Member API")
@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final JwtService jwtService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(
            @Validated @RequestBody MemberSignupRequestDTO signupRequestDTO) {

        memberService.signupMember(signupRequestDTO);

        return ApiResponse.success_only(SuccessStatus.MEMBER_SIGNUP_SUCCESS);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(
            @Validated @RequestBody MemberLoginRequestDTO loginRequestDTO) {

        MemberLoginResponseDTO respDTO = memberService.login(loginRequestDTO);

        return ApiResponse.success(SuccessStatus.MEMBER_LOGIN_SUCCESS, respDTO);
    }

    @GetMapping("/token-reissue")
    public ResponseEntity<ApiResponse<Void>> reissueToken(
            @RequestHeader(value = "Authorization-Refresh", required = false) String refreshToken) {

        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new BadRequestException(ErrorStatus.MISSING_REFRESH_TOKEN_EXCEPTION.getMessage());
        }

        String pureRefreshToken = refreshToken.substring("Bearer ".length());
        if (jwtService.isTokenValid(pureRefreshToken)) {
            throw new BadRequestException(ErrorStatus.UNAUTHORIZED_REFRESH_TOKEN_EXCEPTION.getMessage());
        }

        return ApiResponse.success_only(SuccessStatus.MEMBER_REISSUE_TOKEN_SUCCESS);
    }

}
