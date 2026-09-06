package com.kampung.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// BE-Req-1: Payload for updating an existing resident vehicle
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateVehicleRequest {
    @NotBlank(message = "Vehicle model is required")
    private String model;

    @NotBlank(message = "Vehicle color is required")
    private String color;

    private String zoneMask;

    @NotBlank(message = "Status is required")
    private String status;
}
