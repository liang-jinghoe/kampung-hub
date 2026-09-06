package com.kampung.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kampung.security.dto.CreateVehicleRequest;
import com.kampung.security.dto.UpdateVehicleRequest;
import com.kampung.security.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ResidentVehicleControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private String adminToken;
    private String residentToken;
    private String guardToken;

    @BeforeEach
    void setUp() {
        // Admin Token (Ahmad - OWNER + ADMIN)
        adminToken = jwtUtil.generateToken(
                "usr-001",
                "ahmad@example.com",
                "Ahmad Zulkifli",
                List.of("OWNER", "ADMIN"),
                "mem-usj4-001",
                "nh-usj4-001"
        );

        // Resident Token (Siti - RESIDENT)
        residentToken = jwtUtil.generateToken(
                "usr-002",
                "siti@example.com",
                "Siti Aminah",
                List.of("RESIDENT"),
                "mem-usj4-002",
                "nh-usj4-001"
        );

        // Guard Token (Muthu - GUARD)
        guardToken = jwtUtil.generateToken(
                "usr-guard",
                "muthu@example.com",
                "Guard Muthu",
                List.of("GUARD"),
                "mem-usj4-guard",
                "nh-usj4-001"
        );
    }

    @Test
    @DisplayName("BE-Req-4: Resident query is strictly locked to own membership vehicles")
    void testGetVehicles_AsResident_ReturnsOnlyOwnVehicles() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles")
                        .header("Authorization", "Bearer " + residentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].plateText", is("WYY1234")))
                .andExpect(jsonPath("$[0].ownerMembershipId", is("mem-usj4-002")));
    }

    @Test
    @DisplayName("BE-Req-4 & 5: Admin query returns all whitelisted vehicles in neighborhood")
    void testGetVehicles_AsAdmin_ReturnsAllVehiclesInNeighborhood() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles")
                        .param("neighborhoodId", "nh-usj4-001")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[*].plateText", hasItems("VHM8807", "WYY1234")));
    }

    @Test
    @DisplayName("BE-Req-4: Fetch single vehicle by ID returns authorized vehicle details")
    void testGetVehicleById_Success() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles/veh-001")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleId", is("veh-001")))
                .andExpect(jsonPath("$.plateText", is("VHM8807")))
                .andExpect(jsonPath("$.model", is("Perodua Myvi")));
    }

    @Test
    @DisplayName("BE-Req-4: Resident registering vehicle automatically binds caller membership context")
    void testCreateVehicle_AsResident_Success() throws Exception {
        CreateVehicleRequest request = CreateVehicleRequest.builder()
                .plateText("BKA8888")
                .model("Honda Civic")
                .color("WHITE")
                .zoneMask("Jalan 4/2")
                .build();

        mockMvc.perform(post("/api/v1/vehicles")
                        .header("Authorization", "Bearer " + residentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vehicleId", startsWith("veh-")))
                .andExpect(jsonPath("$.plateText", is("BKA8888")))
                .andExpect(jsonPath("$.ownerMembershipId", is("mem-usj4-002")))
                .andExpect(jsonPath("$.neighborhoodId", is("nh-usj4-001")))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    @DisplayName("BE-Req-1 & 6: Invalid Malaysian plate text triggers validation 400 Bad Request")
    void testCreateVehicle_InvalidPlatePattern_Returns400() throws Exception {
        CreateVehicleRequest request = CreateVehicleRequest.builder()
                .plateText("INVALID_PLATE_123456")
                .model("Honda Civic")
                .color("WHITE")
                .build();

        mockMvc.perform(post("/api/v1/vehicles")
                        .header("Authorization", "Bearer " + residentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode", is(400)))
                .andExpect(jsonPath("$.message", containsString("Invalid Malaysian plate format")));
    }

    @Test
    @DisplayName("BE-Req-4: Security guard attempting to register a vehicle is forbidden (403)")
    void testCreateVehicle_AsGuard_Forbidden403() throws Exception {
        CreateVehicleRequest request = CreateVehicleRequest.builder()
                .plateText("BKA9999")
                .model("Toyota Vios")
                .color("BLACK")
                .build();

        mockMvc.perform(post("/api/v1/vehicles")
                        .header("Authorization", "Bearer " + guardToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode", is(403)))
                .andExpect(jsonPath("$.message", containsString("Security guards cannot register vehicles")));
    }

    @Test
    @DisplayName("BE-Req-4: Vehicle owner can update vehicle details and status")
    void testUpdateVehicle_Success() throws Exception {
        UpdateVehicleRequest request = UpdateVehicleRequest.builder()
                .model("Perodua Myvi Gen3")
                .color("RED")
                .zoneMask("Jalan 4/1")
                .status("SUSPENDED")
                .build();

        mockMvc.perform(put("/api/v1/vehicles/veh-001")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model", is("Perodua Myvi Gen3")))
                .andExpect(jsonPath("$.color", is("RED")))
                .andExpect(jsonPath("$.status", is("SUSPENDED")));
    }

    @Test
    @DisplayName("BE-Req-4: Vehicle owner can delete vehicle record")
    void testDeleteVehicle_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/vehicles/veh-001")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/vehicles/veh-001")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }
}
