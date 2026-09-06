package com.kampung.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// BE-Req-1: DTO modeling a user's role and unit membership within a neighborhood
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipContextDto {
    private String membershipId;
    private String neighborhoodId;
    private String neighborhoodName;
    private String unitNumber;
    private List<String> roles;
}
