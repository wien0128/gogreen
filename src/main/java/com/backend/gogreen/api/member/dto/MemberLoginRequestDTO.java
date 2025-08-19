package com.backend.gogreen.api.member.dto;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
public class MemberLoginRequestDTO {

    private String userId;
    private String password;
}
