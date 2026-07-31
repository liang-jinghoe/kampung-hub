package com.kampung.security.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "access_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessLog {

    @Id
    @Column(name = "log_id", nullable = false, updatable = false)
    private String logId;

    @Column(name = "neighborhood_id", nullable = false)
    private String neighborhoodId;

    @Column(name = "plate_text", nullable = false)
    private String plateText;

    @Column(name = "access_type", nullable = false)
    private String accessType; // e.g., ENTRY, EXIT

    @Column(name = "visitor_pass_id")
    private String visitorPassId;

    @Column(name = "membership_id")
    private String membershipId;

    @Column(name = "verified_by_guard_id", nullable = false)
    private String verifiedByGuardId;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
}
