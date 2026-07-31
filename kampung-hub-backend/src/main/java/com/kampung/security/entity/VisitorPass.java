package com.kampung.security.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "visitor_passes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitorPass {

    @Id
    @Column(name = "pass_id", nullable = false, updatable = false)
    private String passId;

    @Column(name = "neighborhood_id", nullable = false)
    private String neighborhoodId;

    @Column(name = "unit_number", nullable = false)
    private String unitNumber;

    @Column(name = "requester_membership_id", nullable = false)
    private String requesterMembershipId;

    @Column(name = "visitor_name", nullable = false)
    private String visitorName;

    @Column(name = "visitor_plate_text", nullable = false)
    private String visitorPlateText;

    @Column(name = "pass_token", nullable = false)
    private String passToken;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_until", nullable = false)
    private LocalDateTime validUntil;

    @Column(name = "status", nullable = false)
    private String status;
}
