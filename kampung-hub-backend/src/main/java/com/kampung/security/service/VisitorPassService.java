package com.kampung.security.service;

import com.kampung.security.dto.CreateVisitorPassRequest;
import com.kampung.security.dto.VisitorPassResponse;
import com.kampung.security.entity.AccessLog;
import com.kampung.security.entity.Membership;
import com.kampung.security.entity.VisitorPass;
import com.kampung.security.repository.AccessLogRepository;
import com.kampung.security.repository.MembershipRepository;
import com.kampung.security.repository.VisitorPassRepository;
import com.kampung.security.security.JwtUser;
import com.kampung.security.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// BE-Req-1, 2 & 4: Service for pre-registering visitor passes and gatehouse verification check-ins
@Service
@RequiredArgsConstructor
public class VisitorPassService {

    private final VisitorPassRepository visitorPassRepository;
    private final MembershipRepository membershipRepository;
    private final AccessLogRepository accessLogRepository;

    @Transactional(readOnly = true)
    public List<VisitorPassResponse> searchVisitorPasses(
            String neighborhoodId, String unitNumber, String passToken, String status) {

        JwtUser caller = SecurityUtils.getCurrentUser();
        if (caller == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }

        String targetNhId = (neighborhoodId != null && !neighborhoodId.isBlank()) ? neighborhoodId : caller.getNeighborhoodId();

        if (passToken != null && !passToken.isBlank()) {
            return visitorPassRepository.findByPassToken(passToken.trim())
                    .stream()
                    .filter(p -> p.getNeighborhoodId().equals(targetNhId))
                    .map(this::buildVisitorPassResponse)
                    .toList();
        }

        List<VisitorPass> passes = visitorPassRepository.findByNeighborhoodId(targetNhId);

        boolean isAdmin = caller.getRoles().contains("ADMIN");
        boolean isGuard = caller.getRoles().contains("GUARD");

        if (!isAdmin && !isGuard) {
            // Residents only see visitor passes requested for their own membership/unit
            passes = passes.stream()
                    .filter(p -> p.getRequesterMembershipId().equals(caller.getMembershipId()))
                    .toList();
        } else if (unitNumber != null && !unitNumber.isBlank()) {
            passes = passes.stream()
                    .filter(p -> unitNumber.equalsIgnoreCase(p.getUnitNumber()))
                    .toList();
        }

        if (status != null && !status.isBlank()) {
            passes = passes.stream()
                    .filter(p -> status.equalsIgnoreCase(p.getStatus()))
                    .toList();
        }

        return passes.stream().map(this::buildVisitorPassResponse).toList();
    }

    @Transactional
    public VisitorPassResponse createVisitorPass(CreateVisitorPassRequest request) {
        JwtUser caller = SecurityUtils.getCurrentUser();
        if (caller == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }

        if (caller.getRoles().contains("GUARD") && !caller.getRoles().contains("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Security guards cannot generate visitor passes.");
        }

        String targetNhId = (request.getNeighborhoodId() != null && !request.getNeighborhoodId().isBlank())
                ? request.getNeighborhoodId() : caller.getNeighborhoodId();
        String targetMembershipId = caller.getMembershipId();

        Membership membership = membershipRepository.findById(targetMembershipId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Caller membership not found."));

        String unitNumber = (request.getUnitNumber() != null && !request.getUnitNumber().isBlank())
                ? request.getUnitNumber() : membership.getUnitNumber();

        String passToken = "vpass-token-" + UUID.randomUUID().toString().substring(0, 8);

        VisitorPass pass = VisitorPass.builder()
                .passId("pass-" + UUID.randomUUID().toString().substring(0, 8))
                .neighborhoodId(targetNhId)
                .unitNumber(unitNumber)
                .requesterMembershipId(targetMembershipId)
                .visitorName(request.getVisitorName().trim())
                .visitorPlateText(request.getVisitorPlateText().trim().toUpperCase())
                .passToken(passToken)
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .status("ACTIVE")
                .build();

        VisitorPass saved = visitorPassRepository.save(pass);
        return buildVisitorPassResponse(saved);
    }

    @Transactional
    public VisitorPassResponse checkInVisitorPass(String id) {
        JwtUser caller = SecurityUtils.getCurrentUser();
        if (caller == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }

        if (!caller.getRoles().contains("GUARD") && !caller.getRoles().contains("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Gate check-in requires Guard authority.");
        }

        VisitorPass pass = visitorPassRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Visitor pass not found with id: " + id));

        if (!pass.getNeighborhoodId().equals(caller.getNeighborhoodId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot check in visitor passes for another neighborhood.");
        }

        if (!"ACTIVE".equalsIgnoreCase(pass.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Visitor pass status is " + pass.getStatus() + " (not ACTIVE).");
        }

        pass.setStatus("USED");
        VisitorPass saved = visitorPassRepository.save(pass);

        // Auto-record Gate Access Log
        AccessLog logEntry = AccessLog.builder()
                .logId("log-" + UUID.randomUUID().toString().substring(0, 8))
                .neighborhoodId(pass.getNeighborhoodId())
                .plateText(pass.getVisitorPlateText())
                .accessType("ENTRY")
                .visitorPassId(pass.getPassId())
                .membershipId(null)
                .verifiedByGuardId(caller.getUserId())
                .timestamp(LocalDateTime.now())
                .build();

        accessLogRepository.save(logEntry);

        return buildVisitorPassResponse(saved);
    }

    private VisitorPassResponse buildVisitorPassResponse(VisitorPass p) {
        return VisitorPassResponse.builder()
                .passId(p.getPassId())
                .neighborhoodId(p.getNeighborhoodId())
                .unitNumber(p.getUnitNumber())
                .requesterMembershipId(p.getRequesterMembershipId())
                .visitorName(p.getVisitorName())
                .visitorPlateText(p.getVisitorPlateText())
                .passToken(p.getPassToken())
                .validFrom(p.getValidFrom())
                .validUntil(p.getValidUntil())
                .status(p.getStatus())
                .build();
    }
}
