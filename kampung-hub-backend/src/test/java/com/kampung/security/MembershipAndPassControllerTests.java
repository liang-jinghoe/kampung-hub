package com.kampung.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kampung.security.dto.*;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class MembershipAndPassControllerTests {

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
        adminToken = jwtUtil.generateToken(
                "usr-001",
                "ahmad@example.com",
                "Ahmad Zulkifli",
                List.of("OWNER", "ADMIN"),
                "mem-usj4-001",
                "nh-usj4-001"
        );

        residentToken = jwtUtil.generateToken(
                "usr-002",
                "siti@example.com",
                "Siti Aminah",
                List.of("RESIDENT"),
                "mem-usj4-002",
                "nh-usj4-001"
        );

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
    @DisplayName("BE-Req-1: Admin can query all members in neighborhood directory")
    void testGetMembers_AsAdmin_Success() throws Exception {
        mockMvc.perform(get("/api/v1/neighborhoods/nh-usj4-001/members")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$[*].fullName", hasItem("Ahmad Zulkifli")));
    }

    @Test
    @DisplayName("BE-Req-1: Admin can invite a new resident, generating an onboarding token")
    void testInviteMember_AsAdmin_ReturnsToken() throws Exception {
        InviteMemberRequest request = InviteMemberRequest.builder()
                .fullName("Tan Ah Kow")
                .email("tan@example.com")
                .unitNumber("No. 99 Jalan USJ 4/4")
                .roles(List.of("RESIDENT"))
                .build();

        mockMvc.perform(post("/api/v1/neighborhoods/nh-usj4-001/members/invite")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("INVITED")))
                .andExpect(jsonPath("$.invitationToken", startsWith("token-")))
                .andExpect(jsonPath("$.email", is("tan@example.com")));
    }

    @Test
    @DisplayName("BE-Req-1: Public onboarding endpoint activates member profile with token and sets password")
    void testOnboardMember_PublicRegister_Success() throws Exception {
        RegisterOnboardRequest request = RegisterOnboardRequest.builder()
                .fullName("Chong Wei")
                .phoneNumber("012-5555555")
                .password("newpassword123")
                .build();

        // Onboard via public endpoint with token
        mockMvc.perform(post("/api/v1/members/register")
                        .param("token", "token-chong-12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.fullName", is("Chong Wei")));

        // Verify Chong can now login with newly registered password
        LoginRequest loginRequest = LoginRequest.builder()
                .email("chong@example.com")
                .password("newpassword123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.email", is("chong@example.com")));
    }

    @Test
    @DisplayName("BE-Req-1: Resident can query own visitor passes")
    void testSearchVisitorPasses_AsResident_Success() throws Exception {
        mockMvc.perform(get("/api/v1/visitor-passes")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].visitorName", is("John Doe")))
                .andExpect(jsonPath("$[0].visitorPlateText", is("WYY9900")));
    }

    @Test
    @DisplayName("BE-Req-1: Resident can pre-register temporary visitor pass")
    void testCreateVisitorPass_AsResident_Success() throws Exception {
        CreateVisitorPassRequest request = CreateVisitorPassRequest.builder()
                .visitorName("Alice Smith")
                .visitorPlateText("JJJ1111")
                .validFrom(LocalDateTime.now())
                .validUntil(LocalDateTime.now().plusDays(1))
                .build();

        mockMvc.perform(post("/api/v1/visitor-passes")
                        .header("Authorization", "Bearer " + residentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.passId", startsWith("pass-")))
                .andExpect(jsonPath("$.passToken", startsWith("vpass-token-")))
                .andExpect(jsonPath("$.visitorName", is("Alice Smith")))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    @DisplayName("BE-Req-1: Guard check-in visitor pass sets status to USED and records gate entry log")
    void testCheckInVisitorPass_AsGuard_Success() throws Exception {
        // Guard check-in pass-001
        mockMvc.perform(put("/api/v1/visitor-passes/pass-001/check-in")
                        .header("Authorization", "Bearer " + guardToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.passId", is("pass-001")))
                .andExpect(jsonPath("$.status", is("USED")));

        // Verify access logs contain the newly created gate record
        mockMvc.perform(get("/api/v1/access-logs")
                        .param("neighborhoodId", "nh-usj4-001")
                        .header("Authorization", "Bearer " + guardToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].visitorPassId", hasItem("pass-001")))
                .andExpect(jsonPath("$[*].plateText", hasItem("WYY9900")));
    }

    @Test
    @DisplayName("BE-Req-1: Guard can manually log a gate access event")
    void testCreateAccessLog_AsGuard_Success() throws Exception {
        CreateAccessLogRequest request = CreateAccessLogRequest.builder()
                .plateText("VHM8807")
                .accessType("EXIT")
                .build();

        mockMvc.perform(post("/api/v1/access-logs")
                        .header("Authorization", "Bearer " + guardToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.logId", startsWith("log-")))
                .andExpect(jsonPath("$.plateText", is("VHM8807")))
                .andExpect(jsonPath("$.accessType", is("EXIT")))
                .andExpect(jsonPath("$.verifiedByGuardId", is("usr-guard")));
    }
}
