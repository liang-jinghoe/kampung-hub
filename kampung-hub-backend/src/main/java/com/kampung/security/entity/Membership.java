package com.kampung.security.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "memberships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Membership {

    @Id
    @Column(name = "membership_id", nullable = false, updatable = false)
    private String membershipId;

    @Column(name = "neighborhood_id", nullable = false)
    private String neighborhoodId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "unit_number", nullable = false)
    private String unitNumber;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "membership_roles", joinColumns = @JoinColumn(name = "membership_id"))
    @Column(name = "role")
    @Builder.Default
    private Set<String> roles = new HashSet<>();

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "invitation_token")
    private String invitationToken;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
