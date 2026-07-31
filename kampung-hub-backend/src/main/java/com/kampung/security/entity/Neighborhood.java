package com.kampung.security.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "neighborhoods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Neighborhood {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "property_type", nullable = false)
    private String propertyType;

    @Column(name = "subscription_tier", nullable = false)
    private String subscriptionTier;

    @Column(name = "subscription_status", nullable = false)
    private String subscriptionStatus;

    @Column(name = "max_allowed_units", nullable = false)
    private Integer maxAllowedUnits;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
