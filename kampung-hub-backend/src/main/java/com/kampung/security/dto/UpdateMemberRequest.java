package com.kampung.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// BE-Req-1: Payload for updating an existing membership profile
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateMemberRequest {
    private String fullName;
    private String phoneNumber;
    private String unitNumber;
    private String status;
    private List<String> roles;
}
