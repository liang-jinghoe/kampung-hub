package com.kampung.security.security;

import io.jsonwebtoken.Claims;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

// BE-Req-2 & 4: Custom Principal representation extracted from verified JWT claims
@Getter
public class JwtUser {
    private final String userId;
    private final String email;
    private final String fullName;
    private final String role;
    private final List<String> roles;
    private final String membershipId;
    private final String neighborhoodId;

    public JwtUser(Claims claims) {
        this.userId = claims.getSubject();
        this.email = claims.get("email", String.class);
        this.fullName = claims.get("fullName", String.class);
        
        List<?> rawRoles = claims.get("roles", List.class);
        if (rawRoles != null && !rawRoles.isEmpty()) {
            this.roles = rawRoles.stream().map(Object::toString).toList();
            this.role = this.roles.get(0);
        } else {
            this.roles = Collections.singletonList("RESIDENT");
            this.role = "RESIDENT";
        }
        
        this.membershipId = claims.get("membershipId", String.class);
        this.neighborhoodId = claims.get("neighborhoodId", String.class);
    }

    public JwtUser(String userId, String email, String fullName, List<String> roles, String membershipId, String neighborhoodId) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.roles = roles != null ? roles : Collections.singletonList("RESIDENT");
        this.role = !this.roles.isEmpty() ? this.roles.get(0) : "RESIDENT";
        this.membershipId = membershipId;
        this.neighborhoodId = neighborhoodId;
    }
}
