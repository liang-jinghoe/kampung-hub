package com.kampung.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// BE-Req-1: Payload for inviting a new resident or staff member
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InviteMemberRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Unit number is required")
    private String unitNumber;

    private List<String> roles;
}
