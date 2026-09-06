package com.kampung.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// BE-Req-1: Response payload for successful login and context resolution
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String userId;
    private String email;
    private String fullName;
    private MembershipContextDto activeMembership;
    private List<MembershipContextDto> memberships;
}
