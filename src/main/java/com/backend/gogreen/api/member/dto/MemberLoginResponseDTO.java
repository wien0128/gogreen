package com.backend.gogreen.api.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MemberLoginResponseDTO {

    private String userId;
    private String email;
    private String role;
    private String accessToken;
    private String refreshToken;
}
