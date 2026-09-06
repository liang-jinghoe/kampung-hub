package com.kampung.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kampung.security.dto.CreateNeighborhoodRequest;
import com.kampung.security.dto.LoginRequest;
import com.kampung.security.dto.SwitchContextRequest;
import com.kampung.security.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthAndNeighborhoodControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private String validJwtToken;

    @BeforeEach
    void setUp() {
        validJwtToken = jwtUtil.generateToken(
                "usr-001",
                "ahmad@example.com",
                "Ahmad Zulkifli",
                List.of("OWNER", "ADMIN"),
                "mem-usj4-001",
                "nh-usj4-001"
        );
    }

    @Test
    @DisplayName("BE-Req-1: Login successfully returns bound JWT and available membership list")
    void testLogin_Success() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("ahmad@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.userId", is("usr-001")))
                .andExpect(jsonPath("$.fullName", is("Ahmad Zulkifli")))
                .andExpect(jsonPath("$.activeMembership.membershipId", is("mem-usj4-001")))
                .andExpect(jsonPath("$.activeMembership.neighborhoodName", is("Taman USJ 4")))
                .andExpect(jsonPath("$.memberships", hasSize(2)));
    }

    @Test
    @DisplayName("BE-Req-1: Login with invalid password returns 401 Unauthorized ApiErrorResponse")
    void testLogin_InvalidPassword_Returns401() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("ahmad@example.com")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")))
                .andExpect(jsonPath("$.message", containsString("Invalid email or password")));
    }

    @Test
    @DisplayName("BE-Req-1 & 6: Login validation failure returns 400 Bad Request")
    void testLogin_ValidationError_Returns400() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("not-an-email")
                .password("")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode", is(400)))
                .andExpect(jsonPath("$.error", containsString("Validation Error")));
    }

    @Test
    @DisplayName("BE-Req-1: Switch context returns new JWT bound to targeted neighborhood")
    void testSwitchContext_Success() throws Exception {
        SwitchContextRequest request = SwitchContextRequest.builder()
                .userId("usr-001")
                .membershipId("mem-ss15-999")
                .build();

        mockMvc.perform(post("/api/v1/auth/context")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.activeMembership.membershipId", is("mem-ss15-999")))
                .andExpect(jsonPath("$.activeMembership.neighborhoodName", is("SS15 Condominium")))
                .andExpect(jsonPath("$.activeMembership.unitNumber", is("A-12-05")));
    }

    @Test
    @DisplayName("BE-Req-1: GET /api/v1/neighborhoods returns all seeded neighborhoods")
    void testGetAllNeighborhoods_Success() throws Exception {
        mockMvc.perform(get("/api/v1/neighborhoods")
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[*].name", hasItem("Taman USJ 4")));
    }

    @Test
    @DisplayName("BE-Req-1 & 5: GET /api/v1/neighborhoods?tier=... filters by subscription tier")
    void testGetNeighborhoods_WithFilter() throws Exception {
        mockMvc.perform(get("/api/v1/neighborhoods")
                        .param("tier", "PREMIUM_LANDED")
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Taman USJ 4")));
    }

    @Test
    @DisplayName("BE-Req-1: GET /api/v1/neighborhoods/{id} returns details for existing neighborhood")
    void testGetNeighborhoodById_Success() throws Exception {
        mockMvc.perform(get("/api/v1/neighborhoods/nh-usj4-001")
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("nh-usj4-001")))
                .andExpect(jsonPath("$.name", is("Taman USJ 4")))
                .andExpect(jsonPath("$.maxAllowedUnits", is(350)));
    }

    @Test
    @DisplayName("BE-Req-1 & 6: GET /api/v1/neighborhoods/{id} returns 404 for non-existent neighborhood")
    void testGetNeighborhoodById_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/neighborhoods/nh-nonexistent-999")
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode", is(404)))
                .andExpect(jsonPath("$.error", containsString("404")));
    }

    @Test
    @DisplayName("BE-Req-1: POST /api/v1/neighborhoods creates new neighborhood record (201 Created)")
    void testCreateNeighborhood_Success() throws Exception {
        CreateNeighborhoodRequest request = CreateNeighborhoodRequest.builder()
                .name("Bandar Sunway Palms")
                .propertyType("LANDED")
                .subscriptionTier("PREMIUM_LANDED")
                .maxAllowedUnits(500)
                .build();

        mockMvc.perform(post("/api/v1/neighborhoods")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", startsWith("nh-")))
                .andExpect(jsonPath("$.name", is("Bandar Sunway Palms")))
                .andExpect(jsonPath("$.subscriptionStatus", is("ACTIVE")))
                .andExpect(jsonPath("$.maxAllowedUnits", is(500)));
    }
}
