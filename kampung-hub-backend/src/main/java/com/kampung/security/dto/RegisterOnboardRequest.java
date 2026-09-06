package com.kampung.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// BE-Req-1: Payload submitted by user to complete onboarding registration
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterOnboardRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;

    private String phoneNumber;

    @NotBlank(message = "Password is required")
    private String password;
}
