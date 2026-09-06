package com.kampung.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// BE-Req-1: Payload for pre-registering a visitor pass
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateVisitorPassRequest {
    @NotBlank(message = "Visitor name is required")
    private String visitorName;

    @NotBlank(message = "Visitor plate number is required")
    private String visitorPlateText;

    @NotNull(message = "Valid from date is required")
    private LocalDateTime validFrom;

    @NotNull(message = "Valid until date is required")
    private LocalDateTime validUntil;

    private String neighborhoodId;
    private String unitNumber;
}
