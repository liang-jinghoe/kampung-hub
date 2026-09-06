package com.kampung.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// BE-Req-1: Request payload to switch active neighborhood membership context
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SwitchContextRequest {
    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Membership ID is required")
    private String membershipId;
}
