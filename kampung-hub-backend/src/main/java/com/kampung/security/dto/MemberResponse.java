package com.kampung.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

// BE-Req-1: Response payload representing a detailed membership and user profile
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberResponse {
    private String membershipId;
    private String userId;
    private String neighborhoodId;
    private String fullName;
    private String email;
    private String unitNumber;
    private String phoneNumber;
    private List<String> roles;
    private String status;
    private String invitationToken;
    private LocalDateTime createdAt;
}
