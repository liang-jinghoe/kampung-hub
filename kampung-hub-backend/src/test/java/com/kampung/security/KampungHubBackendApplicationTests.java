package com.kampung.security;

import com.kampung.security.entity.Neighborhood;
import com.kampung.security.entity.ResidentVehicle;
import com.kampung.security.entity.User;
import com.kampung.security.repository.NeighborhoodRepository;
import com.kampung.security.repository.ResidentVehicleRepository;
import com.kampung.security.repository.UserRepository;
import com.kampung.security.security.JwtUser;
import com.kampung.security.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class KampungHubBackendApplicationTests {

    @Autowired
    private NeighborhoodRepository neighborhoodRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResidentVehicleRepository vehicleRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void contextLoads() {
        assertNotNull(neighborhoodRepository);
        assertNotNull(userRepository);
        assertNotNull(vehicleRepository);
    }

    @Test
    @DisplayName("BE-Req-3: Verify database seed records loaded from data.sql")
    void testDatabaseSeedDataLoaded() {
        // Verify Neighborhoods
        List<Neighborhood> neighborhoods = neighborhoodRepository.findAll();
        assertFalse(neighborhoods.isEmpty(), "Neighborhoods should be pre-populated");
        Optional<Neighborhood> usj4 = neighborhoodRepository.findById("nh-usj4-001");
        assertTrue(usj4.isPresent());
        assertEquals("Taman USJ 4", usj4.get().getName());

        // Verify Users
        Optional<User> ahmad = userRepository.findById("usr-001");
        assertTrue(ahmad.isPresent());
        assertEquals("ahmad@example.com", ahmad.get().getEmail());
        assertTrue(passwordEncoder.matches("password123", ahmad.get().getPassword()),
                "Ahmad's password should match password123");

        // Verify Vehicles
        List<ResidentVehicle> vehicles = vehicleRepository.findAll();
        assertFalse(vehicles.isEmpty(), "Vehicles should be pre-populated");
        assertTrue(vehicles.stream().anyMatch(v -> "VHM8807".equals(v.getPlateText())));
    }

    @Test
    @DisplayName("BE-Req-2 & 4: Verify JWT Token generation, claims extraction, and validity")
    void testJwtTokenGenerationAndValidation() {
        String token = jwtUtil.generateToken(
                "usr-001",
                "ahmad@example.com",
                "Ahmad Zulkifli",
                List.of("OWNER", "ADMIN"),
                "mem-usj4-001",
                "nh-usj4-001"
        );

        assertNotNull(token);
        assertTrue(jwtUtil.isTokenValid(token));

        Claims claims = jwtUtil.extractAllClaims(token);
        assertEquals("usr-001", claims.getSubject());
        assertEquals("ahmad@example.com", claims.get("email"));
        assertEquals("Ahmad Zulkifli", claims.get("fullName"));

        JwtUser jwtUser = new JwtUser(claims);
        assertEquals("usr-001", jwtUser.getUserId());
        assertEquals("ahmad@example.com", jwtUser.getEmail());
        assertEquals("mem-usj4-001", jwtUser.getMembershipId());
        assertEquals("nh-usj4-001", jwtUser.getNeighborhoodId());
        assertEquals("OWNER", jwtUser.getRole());
        assertTrue(jwtUser.getRoles().contains("ADMIN"));
    }
}
