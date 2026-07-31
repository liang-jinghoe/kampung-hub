package com.kampung.security.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resident_vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResidentVehicle {

    @Id
    @Column(name = "vehicle_id", nullable = false, updatable = false)
    private String vehicleId;

    @Column(name = "neighborhood_id", nullable = false)
    private String neighborhoodId;

    @Column(name = "owner_membership_id", nullable = false)
    private String ownerMembershipId;

    @Column(name = "unit_number", nullable = false)
    private String unitNumber;

    @Column(name = "plate_text", nullable = false)
    private String plateText;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "color", nullable = false)
    private String color;

    @Column(name = "zone_mask")
    private String zoneMask;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
