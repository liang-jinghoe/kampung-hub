package com.kampung.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// BE-Req-1: Payload for logging a gatehouse access event
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAccessLogRequest {
    @NotBlank(message = "Plate number is required")
    private String plateText;

    @NotBlank(message = "Access type is required (e.g. ENTRY, EXIT)")
    private String accessType;

    private String visitorPassId;
    private String membershipId;
    private String neighborhoodId;
}
