package com.kampung.security.controller;

import com.kampung.security.dto.MemberResponse;
import com.kampung.security.dto.RegisterOnboardRequest;
import com.kampung.security.service.MembershipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// BE-Req-1 & 2: Public endpoint for resident onboarding registration using invitation token
@RestController
@RequestMapping("/api/v1/members")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class MemberRegisterController {

    private final MembershipService membershipService;

    // BE-Req-1: Complete registration, activate member profile and set password
    @PostMapping("/register")
    public ResponseEntity<MemberResponse> registerOnboard(
            @RequestParam("token") String token,
            @Valid @RequestBody RegisterOnboardRequest request) {

        MemberResponse member = membershipService.onboardMember(token, request);
        return ResponseEntity.ok(member);
    }
}
