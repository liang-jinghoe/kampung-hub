package com.kampung.security.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// BE-Req-1: Payload for registering a new Neighborhood
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateNeighborhoodRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Property type is required")
    private String propertyType;

    @NotBlank(message = "Subscription tier is required")
    private String subscriptionTier;

    @NotNull(message = "Max allowed units is required")
    @Min(value = 1, message = "Max allowed units must be at least 1")
    private Integer maxAllowedUnits;
}
