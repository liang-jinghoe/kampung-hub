package com.kampung.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// BE-Req-1: Payload for registering a vehicle on the neighborhood whitelist
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateVehicleRequest {
    private String neighborhoodId;
    private String ownerMembershipId;
    private String unitNumber;

    @NotBlank(message = "Plate number is required")
    @Pattern(regexp = "^[A-Z]{1,3}\\s?[0-9]{1,4}\\s?[A-Z]?$", message = "Invalid Malaysian plate format (e.g. VHM8807 or WYY1234).")
    private String plateText;

    @NotBlank(message = "Vehicle model is required")
    private String model;

    @NotBlank(message = "Vehicle color is required")
    private String color;

    private String zoneMask;
}
