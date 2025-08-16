package com.backend.gogreen.common.config.security;

import com.backend.gogreen.api.member.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityMember implements UserDetails {

    private Long id;
    private String email;
    private String password;
    private Role role;
}
