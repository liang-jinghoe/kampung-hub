package com.kampung.security.controller;

import com.kampung.security.dto.*;
import com.kampung.security.service.MembershipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// BE-Req-1 & 2: REST Controller for managing neighborhood member directory & invitations
@RestController
@RequestMapping("/api/v1/neighborhoods/{neighborhoodId}/members")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    // BE-Req-1: List all members in a neighborhood (supports status and role filters)
    @GetMapping
    public ResponseEntity<List<MemberResponse>> getMembers(
            @PathVariable("neighborhoodId") String neighborhoodId,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "role", required = false) String role) {

        List<MemberResponse> members = membershipService.getMembers(neighborhoodId, status, role);
        return ResponseEntity.ok(members);
    }

    // BE-Req-1: Get specific member details
    @GetMapping("/{membershipId}")
    public ResponseEntity<MemberResponse> getMemberById(
            @PathVariable("neighborhoodId") String neighborhoodId,
            @PathVariable("membershipId") String membershipId) {

        MemberResponse member = membershipService.getMemberById(neighborhoodId, membershipId);
        return ResponseEntity.ok(member);
    }

    // BE-Req-1: Directly create and activate a member profile
    @PostMapping
    public ResponseEntity<MemberResponse> directRegisterMember(
            @PathVariable("neighborhoodId") String neighborhoodId,
            @Valid @RequestBody DirectRegisterMemberRequest request) {

        MemberResponse member = membershipService.directRegisterMember(neighborhoodId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(member);
    }

    // BE-Req-1: Invite a new resident/staff member (starts in INVITED status with token)
    @PostMapping("/invite")
    public ResponseEntity<MemberResponse> inviteMember(
            @PathVariable("neighborhoodId") String neighborhoodId,
            @Valid @RequestBody InviteMemberRequest request) {

        MemberResponse member = membershipService.inviteMember(neighborhoodId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(member);
    }

    // BE-Req-1: Update member profile details or role assignments
    @PutMapping("/{membershipId}")
    public ResponseEntity<MemberResponse> updateMember(
            @PathVariable("neighborhoodId") String neighborhoodId,
            @PathVariable("membershipId") String membershipId,
            @Valid @RequestBody UpdateMemberRequest request) {

        MemberResponse updated = membershipService.updateMember(neighborhoodId, membershipId, request);
        return ResponseEntity.ok(updated);
    }

    // BE-Req-1: Remove a membership from the neighborhood directory
    @DeleteMapping("/{membershipId}")
    public ResponseEntity<Void> deleteMember(
            @PathVariable("neighborhoodId") String neighborhoodId,
            @PathVariable("membershipId") String membershipId) {

        membershipService.deleteMember(neighborhoodId, membershipId);
        return ResponseEntity.noContent().build();
    }
}
