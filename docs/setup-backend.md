# KampungHub Back-End Specification (Java Spring Boot 3+)

This document defines the back-end specifications, dependencies, data models, repository interfaces, and controller implementations for the **KampungHub Security Backend Gateway**.

---

## 2.1 Dependencies & Project Setup (`pom.xml`)
The backend uses Spring Boot 3.x with Java 17, Spring Data JPA, H2 Database (for dev/test), PostgreSQL Driver (for production), Project Lombok, and Spring Boot DevTools.

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>

    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>

    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-devtools</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

---

## 2.2 Detailed Requirement Mapping & Code Implementation Rules

#### [BE-Req 1] REST API Endpoint Design Specification
The API endpoints map to Kampung business entities and logic:

| HTTP Method | API Endpoint | Query / Path Parameters | Business Description |
| :--- | :--- | :--- | :--- |
| **POST** | `/api/v1/auth/login` | None | Authenticate credentials and return JWT or membership context options |
| **POST** | `/api/v1/auth/context` | None | Select neighborhood membership context and return final bound JWT |
| **GET** | `/api/v1/neighborhoods` | Query: `tier`, `status` | List neighborhoods filtered by plan tier or status |
| **GET** | `/api/v1/neighborhoods/{id}` | Path: `id` | Get single neighborhood details |
| **POST** | `/api/v1/neighborhoods` | None | Register a new neighborhood |
| **GET** | `/api/v1/neighborhoods/{id}/members` | Path: `id` | List memberships belonging to a neighborhood |
| **GET** | `/api/v1/neighborhoods/{id}/members/{membershipId}` | Path: `id`, `membershipId` | Get a single neighborhood membership details |
| **POST** | `/api/v1/neighborhoods/{id}/members` | Path: `id` | Register a new membership directly (starts as ACTIVE) |
| **POST** | `/api/v1/neighborhoods/{id}/members/invite` | Path: `id` | Send invitation email to new resident (membership status starts as INVITED) |
| **POST** | `/api/v1/members/register` | Query: `token` | Onboard member: submit registration details & set user/membership status to ACTIVE |
| **PUT** | `/api/v1/neighborhoods/{id}/members/{membershipId}` | Path: `id`, `membershipId` | Update membership profile or status |
| **DELETE** | `/api/v1/neighborhoods/{id}/members/{membershipId}` | Path: `id`, `membershipId` | Remove a membership from the neighborhood directory |
| **GET** | `/api/v1/vehicles` | Query: `neighborhoodId`, `status` | Fetch whitelisted vehicles for a neighborhood |
| **GET** | `/api/v1/vehicles/{id}` | Path: `id` | Get specific vehicle whitelist entry |
| **POST** | `/api/v1/vehicles` | None | Register a new vehicle plate and assign an owner membership |
| **PUT** | `/api/v1/vehicles/{id}` | Path: `id` | Update vehicle details, status, or owner membership |
| **DELETE** | `/api/v1/vehicles/{id}` | Path: `id` | Remove vehicle from whitelist |
| **GET** | `/api/v1/visitor-passes` | Query: `neighborhoodId`, `unitNumber` | Search visitor access passes |
| **POST** | `/api/v1/visitor-passes` | None | Create a temporary visitor pass |
| **PUT** | `/api/v1/visitor-passes/{id}/check-in` | Path: `id` | Check-in visitor pass at gatehouse and update status to USED |
| **GET** | `/api/v1/access-logs` | Query: `neighborhoodId`, `page`, `sortBy` | Fetch sorted access logs for guardhouse |


#### [BE-Req 3] JPA Domain Entities & Database Initialization
Entities represent the relational domain models. Primary keys use UUIDs stored as String/UUID.

```java
package com.kampung.security.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// BE-Req-3: JPA Entity representing the multi-tenant Neighborhood
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
    private String id; // UUID String representation

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "property_type", nullable = false)
    private String propertyType; // e.g., LANDED, HIGH_RISE

    @Column(name = "subscription_tier", nullable = false)
    private String subscriptionTier; // e.g., BASIC_LANDED, PREMIUM_LANDED

    @Column(name = "subscription_status", nullable = false)
    private String subscriptionStatus; // e.g., ACTIVE, EXPIRED

    @Column(name = "max_allowed_units", nullable = false)
    private Integer maxAllowedUnits;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
```

```java
package com.kampung.security.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

// BE-Req-3: JPA Entity representing platform-wide User accounts
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private String userId; // UUID String representation

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "password")
    private String password; // Credential hash (null if user is only invited but has not registered yet)

    @Column(name = "status", nullable = false)
    private String status; // INVITED, ACTIVE, INACTIVE

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
```

```java
package com.kampung.security.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

// BE-Req-3: JPA Entity connecting a User account to a specific Neighborhood
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
    private String membershipId; // UUID String

    @Column(name = "neighborhood_id", nullable = false)
    private String neighborhoodId;

    @Column(name = "user_id", nullable = false)
    private String userId; // Link to the User profile

    @Column(name = "unit_number", nullable = false)
    private String unitNumber; // Can vary per neighborhood!

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "membership_roles", joinColumns = @JoinColumn(name = "membership_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>(); // ADMIN, GUARD, OWNER, RESIDENT, FAMILY_MEMBER, TENANT

    @Column(name = "status", nullable = false)
    private String status; // INVITED, ACTIVE, INACTIVE

    @Column(name = "invitation_token")
    private String invitationToken; // Token used for onboarding registration link

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
```

The `users` table contains platform account profiles. The `memberships` table contains the tenant-specific records linking user accounts to neighborhoods with distinct unit numbers and roles. The `resident_vehicles` table stores `owner_membership_id` referencing the specific neighborhood membership that owns/registers the vehicle.

```java
package com.kampung.security.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// BE-Req-3: JPA Entity modeling whitelisted Resident Vehicles
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
    private String vehicleId; // UUID String

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
    private String zoneMask; // Comma-separated allowed zones/streets for gatehouse access check (e.g., "Jalan 4/1")

    @Column(name = "status", nullable = false)
    private String status; // ACTIVE, SUSPENDED

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

* **Database Initializer (`src/main/resources/data.sql`)**:

```sql
-- BE-Req-3: Initial SQL Seed Records for Database Pre-population
INSERT INTO neighborhoods (id, name, property_type, subscription_tier, subscription_status, max_allowed_units, created_at)
VALUES ('nh-usj4-001', 'Taman USJ 4', 'LANDED', 'PREMIUM_LANDED', 'ACTIVE', 350, CURRENT_TIMESTAMP());

-- Seed Users (Platform Identity Profiles)
INSERT INTO users (user_id, email, full_name, phone_number, password, status, created_at)
VALUES ('usr-001', 'ahmad@example.com', 'Ahmad Zulkifli', '012-3456789', '$2a$10$xyz', 'ACTIVE', CURRENT_TIMESTAMP());

INSERT INTO users (user_id, email, full_name, phone_number, password, status, created_at)
VALUES ('usr-002', 'siti@example.com', 'Siti Aminah', '012-9876543', '$2a$10$xyz', 'ACTIVE', CURRENT_TIMESTAMP());

INSERT INTO users (user_id, email, full_name, phone_number, password, status, created_at)
VALUES ('usr-003', 'chong@example.com', 'Chong Wei', '012-5555555', NULL, 'INVITED', CURRENT_TIMESTAMP());

INSERT INTO users (user_id, email, full_name, phone_number, password, status, created_at)
VALUES ('usr-guard', 'muthu@example.com', 'Guard Muthu', '012-1111111', '$2a$10$xyz', 'ACTIVE', CURRENT_TIMESTAMP());

-- Seed Memberships (Tenant Memberships connecting Users to Neighborhoods)
INSERT INTO memberships (membership_id, neighborhood_id, user_id, unit_number, status, invitation_token, created_at)
VALUES ('mem-usj4-001', 'nh-usj4-001', 'usr-001', 'No. 12 Jalan USJ 4/1', 'ACTIVE', NULL, CURRENT_TIMESTAMP());

INSERT INTO memberships (membership_id, neighborhood_id, user_id, unit_number, status, invitation_token, created_at)
VALUES ('mem-usj4-002', 'nh-usj4-001', 'usr-002', 'No. 45 Jalan USJ 4/2', 'ACTIVE', NULL, CURRENT_TIMESTAMP());

INSERT INTO memberships (membership_id, neighborhood_id, user_id, unit_number, status, invitation_token, created_at)
VALUES ('mem-usj4-003', 'nh-usj4-001', 'usr-003', 'No. 88 Jalan USJ 4/3', 'INVITED', 'token-chong-123', CURRENT_TIMESTAMP());

INSERT INTO memberships (membership_id, neighborhood_id, user_id, unit_number, status, invitation_token, created_at)
VALUES ('mem-usj4-guard', 'nh-usj4-001', 'usr-guard', 'GUARDHOUSE', 'ACTIVE', NULL, CURRENT_TIMESTAMP());

-- Seed Membership Roles
INSERT INTO membership_roles (membership_id, role) VALUES ('mem-usj4-001', 'OWNER');
INSERT INTO membership_roles (membership_id, role) VALUES ('mem-usj4-001', 'ADMIN'); -- Ahmad is a resident admin

INSERT INTO membership_roles (membership_id, role) VALUES ('mem-usj4-002', 'RESIDENT');
INSERT INTO membership_roles (membership_id, role) VALUES ('mem-usj4-002', 'GUARD'); -- Siti is a resident guard

INSERT INTO membership_roles (membership_id, role) VALUES ('mem-usj4-003', 'TENANT');

INSERT INTO membership_roles (membership_id, role) VALUES ('mem-usj4-guard', 'GUARD');

-- Seed Resident Vehicles
INSERT INTO resident_vehicles (vehicle_id, neighborhood_id, owner_membership_id, unit_number, plate_text, model, color, zone_mask, status, updated_at)
VALUES ('veh-001', 'nh-usj4-001', 'mem-usj4-001', 'No. 12 Jalan USJ 4/1', 'VHM8807', 'Perodua Myvi', 'SILVER', 'Jalan 4/1', 'ACTIVE', CURRENT_TIMESTAMP());

INSERT INTO resident_vehicles (vehicle_id, neighborhood_id, owner_membership_id, unit_number, plate_text, model, color, zone_mask, status, updated_at)
VALUES ('veh-002', 'nh-usj4-001', 'mem-usj4-002', 'No. 45 Jalan USJ 4/2', 'WYY1234', 'Proton Saga', 'WHITE', 'Jalan 4/2', 'ACTIVE', CURRENT_TIMESTAMP());
```


#### [BE-Req 4] Membership Repository & Vehicle Ownership Lookup
* **Repository Direction**: Add `MembershipRepository` with queries for memberships by neighborhood, unit number, and status.
* **Ownership Lookup**: Vehicle create/update payloads should validate that `ownerMembershipId` belongs to the same `neighborhoodId` before saving.
* **Vehicle Read Contract**: Vehicle responses should include `ownerMembershipId` so the front-end can show the assigned member/user profile without an extra lookup.

```java
package com.kampung.security.repository;

import com.kampung.security.entity.ResidentVehicle;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResidentVehicleRepository extends JpaRepository<ResidentVehicle, String> {

    // BE-Req-4: Derived query method for simple property filtering
    List<ResidentVehicle> findByNeighborhoodIdAndStatus(String neighborhoodId, String status);

    // BE-Req-5: Derived query method for partial plate pattern search
    List<ResidentVehicle> findByPlateTextContainingIgnoreCase(String plateText);

    // BE-Req-5: Custom JPQL Query with parameter binding
    @Query("SELECT v FROM ResidentVehicle v WHERE v.neighborhoodId = :nhId AND v.unitNumber = :unit ORDER BY v.updatedAt DESC")
    List<ResidentVehicle> findVehiclesByUnitCustom(@Param("nhId") String nhId, @Param("unit") String unit);

    // BE-Req-5: Custom Native SQL Query
    @Query(value = "SELECT * FROM resident_vehicles WHERE neighborhood_id = :nhId AND status = 'ACTIVE' ORDER BY plate_text ASC", nativeQuery = true)
    List<ResidentVehicle> findActiveVehiclesNative(@Param("nhId") String nhId);

    // BE-Req-4: Find vehicles registered under a specific neighborhood membership
    List<ResidentVehicle> findByOwnerMembershipId(String ownerMembershipId);

    // BE-Req-4: Custom derived method with sorting support
    List<ResidentVehicle> findByNeighborhoodId(String neighborhoodId, Sort sort);
}
```


#### [BE-Req 2 & 4] REST Controller & Service Layer Security Separation
To adhere to enterprise clean architecture guidelines, routing and serialization logic are separated from business operations and authorization rules by splitting the system into a **Controller** layer and a **Service** layer.

* **Controller Class (`ResidentVehicleController.java`)**:
  *Handles HTTP requests, parameter binding, request validation, and converts service responses to HttpEntities. Standard Spring Security intercepts the JWT globally, so the controller signatures do not need parameter-bloating header fields.*

```java
package com.kampung.security.controller;

import com.kampung.security.entity.ResidentVehicle;
import com.kampung.security.service.ResidentVehicleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// BE-Req-2: Spring RestController handling RESTful HTTP requests
@RestController
@RequestMapping("/api/v1/vehicles")
@CrossOrigin(origins = "http://localhost:4200") // CORS configuration for Angular frontend
public class ResidentVehicleController {

    @Autowired
    private ResidentVehicleService vehicleService;

    // BE-Req-1 & 2: GET Endpoint accepting query parameters
    @GetMapping
    public ResponseEntity<List<ResidentVehicle>> getAllVehicles(
            @RequestParam(name = "neighborhoodId") String neighborhoodId,
            @RequestParam(name = "ownerMembershipId", required = false) String ownerMembershipId,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "sortBy", defaultValue = "plateText") String sortBy) {

        List<ResidentVehicle> result = vehicleService.getVehicles(
                neighborhoodId, ownerMembershipId, status, sortBy);
        return ResponseEntity.ok(result);
    }

    // BE-Req-1 & 2: GET Endpoint accepting path parameters
    @GetMapping("/{id}")
    public ResponseEntity<ResidentVehicle> getVehicleById(@PathVariable("id") String id) {
        ResidentVehicle vehicle = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(vehicle);
    }

    // BE-Req-1 & 2: POST Endpoint for vehicle registration
    @PostMapping
    public ResponseEntity<ResidentVehicle> createVehicle(@Valid @RequestBody ResidentVehicle vehiclePayload) {
        ResidentVehicle savedVehicle = vehicleService.createVehicle(vehiclePayload);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedVehicle);
    }

    // BE-Req-1 & 2: PUT Endpoint for vehicle updates
    @PutMapping("/{id}")
    public ResponseEntity<ResidentVehicle> updateVehicle(
            @PathVariable("id") String id,
            @Valid @RequestBody ResidentVehicle updatePayload) {

        ResidentVehicle updated = vehicleService.updateVehicle(id, updatePayload);
        return ResponseEntity.ok(updated);
    }

    // BE-Req-1 & 2: DELETE Endpoint
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable("id") String id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}
```

* **Service Class (`ResidentVehicleService.java`)**:
  *Implements database transactions, business logic processing, and role-based access security constraints by reading from Spring's SecurityContext via SecurityUtils.*

```java
package com.kampung.security.service;

import com.kampung.security.entity.ResidentVehicle;
import com.kampung.security.repository.ResidentVehicleRepository;
import com.kampung.security.security.SecurityUtils;
import com.kampung.security.security.JwtUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ResidentVehicleService {

    @Autowired
    private ResidentVehicleRepository vehicleRepository;

    // BE-Req-4 & 5: Read vehicles list filtered by caller security context
    public List<ResidentVehicle> getVehicles(String neighborhoodId, String ownerMembershipId, String status, String sortBy) {
        JwtUser caller = SecurityUtils.getCurrentUser();
        Sort sort = Sort.by(Sort.Direction.ASC, sortBy);

        if ("RESIDENT".equals(caller.getRole())) {
            // Security boundary: Residents are strictly locked to their own membership context
            return vehicleRepository.findByOwnerMembershipId(caller.getMembershipId());
        } else if (ownerMembershipId != null && !ownerMembershipId.isEmpty()) {
            // Admins/Guards are allowed to query vehicles for a specific membership ID
            return vehicleRepository.findByOwnerMembershipId(ownerMembershipId);
        } else if (status != null && !status.isEmpty()) {
            return vehicleRepository.findByNeighborhoodIdAndStatus(neighborhoodId, status);
        } else {
            return vehicleRepository.findByNeighborhoodId(neighborhoodId, sort);
        }
    }

    // BE-Req-4: Fetch and authorize reading a single vehicle record
    public ResidentVehicle getVehicleById(String id) {
        ResidentVehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found."));

        validateVehicleAccess(vehicle, SecurityUtils.getCurrentUser(), "READ");
        return vehicle;
    }

    // BE-Req-4: Save vehicle whitelisting details and inject JWT context for Residents
    public ResidentVehicle createVehicle(ResidentVehicle vehiclePayload) {
        JwtUser caller = SecurityUtils.getCurrentUser();

        if ("RESIDENT".equals(caller.getRole())) {
            // Automatically bind registration properties to current login profile (avoids tampering)
            vehiclePayload.setOwnerMembershipId(caller.getMembershipId());
            vehiclePayload.setNeighborhoodId(caller.getNeighborhoodId());
        } else if ("GUARD".equals(caller.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Security guards cannot register vehicles.");
        }

        vehiclePayload.setVehicleId("veh-" + UUID.randomUUID().toString().substring(0, 8));
        vehiclePayload.setUpdatedAt(LocalDateTime.now());
        if (vehiclePayload.getStatus() == null) {
            vehiclePayload.setStatus("ACTIVE");
        }

        return vehicleRepository.save(vehiclePayload);
    }

    // BE-Req-4: Update authorized vehicle properties
    public ResidentVehicle updateVehicle(String id, ResidentVehicle updatePayload) {
        ResidentVehicle existing = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found."));

        validateVehicleAccess(existing, SecurityUtils.getCurrentUser(), "WRITE");

        existing.setModel(updatePayload.getModel());
        existing.setColor(updatePayload.getColor());
        existing.setStatus(updatePayload.getStatus());
        existing.setZoneMask(updatePayload.getZoneMask());
        existing.setUpdatedAt(LocalDateTime.now());

        return vehicleRepository.save(existing);
    }

    // BE-Req-4: Delete vehicle whitelisting record
    public void deleteVehicle(String id) {
        ResidentVehicle existing = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found."));

        validateVehicleAccess(existing, SecurityUtils.getCurrentUser(), "WRITE");
        vehicleRepository.deleteById(id);
    }

    // Private Helper enforcing context-bound authorization constraints
    private void validateVehicleAccess(ResidentVehicle vehicle, JwtUser caller, String requiredPermission) {
        // Security boundary 1: Admins can only CRUD vehicles inside their registered neighborhood
        if ("ADMIN".equals(caller.getRole())) {
            if (!vehicle.getNeighborhoodId().equals(caller.getNeighborhoodId())) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Admins can only manage vehicles within their own neighborhood.");
            }
        } 
        // Security boundary 2: Residents can only CRUD vehicles they own (matching their membership ID)
        else if ("RESIDENT".equals(caller.getRole())) {
            if (!vehicle.getOwnerMembershipId().equals(caller.getMembershipId())) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Residents can only manage their own vehicles.");
            }
        } 
        // Security boundary 3: Guards have READ access for checking plate lists, but no WRITE permission
        else if ("GUARD".equals(caller.getRole())) {
            if ("WRITE".equals(requiredPermission)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Security guards do not have write access for whitelisted vehicles.");
            }
            if (!vehicle.getNeighborhoodId().equals(caller.getNeighborhoodId())) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Guards can only view vehicles within their own neighborhood.");
            }
        }
    }
}
```

* **Security Configuration Utility Components (`JwtUtil.java` & `SecurityUtils.java`)**:
  *Extracting token details is isolated into specialized reusable security components.*

```java
// 1. JwtUtil.java - Handles token cryptographic parsing
package com.kampung.security.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;

@Component
public class JwtUtil {
    private final String secretKeyString = "Base64EncodedSecretKeyForKampungHubSecuritySigningKeyPlaceHolderMustBe32BytesLong=";

    public Claims extractAllClaims(String token) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKeyString);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

// 2. JwtUser.java - Custom Principal DTO
package com.kampung.security.security;

import io.jsonwebtoken.Claims;
import java.util.List;

public class JwtUser {
    private final String userId;
    private final String email;
    private final String role;
    private final String membershipId;
    private final String neighborhoodId;

    public JwtUser(Claims claims) {
        this.userId = claims.getSubject();
        this.email = claims.get("email", String.class);
        List<?> rolesList = claims.get("roles", List.class);
        this.role = (rolesList != null && !rolesList.isEmpty()) ? rolesList.get(0).toString() : "RESIDENT";
        this.membershipId = claims.get("membershipId", String.class);
        this.neighborhoodId = claims.get("neighborhoodId", String.class);
    }

    public String getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getMembershipId() { return membershipId; }
    public String getNeighborhoodId() { return neighborhoodId; }
}

// 3. SecurityUtils.java - Static context holder interface
package com.kampung.security.security;

import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    public static JwtUser getCurrentUser() {
        // Resolves the parsed JwtUser principal populated by the JwtAuthenticationFilter
        return (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
```
```

#### [BE-Req 6] Global Exception Handling & Custom Error Payloads
* **Error Response DTO (`ApiErrorResponse.java`)**:

```java
package com.kampung.security.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiErrorResponse {
    private int statusCode;
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;
}
```

* **Global Exception Handler (`GlobalExceptionHandler.java`)**:

```java
package com.kampung.security.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

// BE-Req-6: RestControllerAdvice for centralized global exception handling
@RestControllerAdvice
public class GlobalExceptionHandler {

    // BE-Req-6: Handle validation failures on @RequestBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String detailedMsg = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce("", (a, b) -> a + "; " + b);

        ApiErrorResponse error = ApiErrorResponse.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request - Validation Error")
                .message(detailedMsg)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // BE-Req-6: Handle malformed JSON request bodies
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedJson(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        ApiErrorResponse error = ApiErrorResponse.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request - Malformed JSON")
                .message("The request JSON body is unparseable or contains invalid syntax.")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // BE-Req-6: Handle URL path/query type mismatches
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        ApiErrorResponse error = ApiErrorResponse.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request - Type Mismatch")
                .message(String.format("Parameter '%s' expects type '%s'.", ex.getName(), ex.getRequiredType().getSimpleName()))
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // BE-Req-6: Catch-all handler for unexpected server errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        ApiErrorResponse error = ApiErrorResponse.builder()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred on the Kampung security backend: " + ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

---

## 2.3 Back-End Build & Initialization Checklist

When setting up the back-end, follow this sequence:
1. **Initialize Spring Boot Backend**
   * Create the project using Spring Initializr (Web, JPA, H2 database, Lombok, and Validation dependencies).
2. **Configure application.properties**
   * Enable the H2 console, set `spring.jpa.hibernate.ddl-auto=update` and connection settings.
3. **Develop Domain Entity Classes**
   * Write `@Entity` definitions for `Neighborhood`, `User`, `Membership`, and `ResidentVehicle` [BE-Req 3].
4. **Implement Repositories**
   * Write interface definitions extending `JpaRepository` and add derived query methods (`findByNeighborhoodIdAndStatus`), custom JPQL annotations, and native SQL queries [BE-Req 4, 5].
5. **Develop REST Controllers**
   * Implement HTTP handlers for CRUD resource access using annotation validation (`@Valid`) and path / parameter mapping [BE-Req 1, 2].
6. **Set Up Global Exception Advice**
   * Create a central class annotated with `@RestControllerAdvice` mapping validation, JSON formatting, type mismatches, and general internal exceptions to `ApiErrorResponse` DTO structures [BE-Req 6].
7. **Pre-populate Database Seed Data**
   * Place `data.sql` inside the resources folder containing starter insert statements for multi-tenant simulation [BE-Req 3].
