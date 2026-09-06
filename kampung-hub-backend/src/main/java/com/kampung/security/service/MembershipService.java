package com.kampung.security.service;

import com.kampung.security.dto.*;
import com.kampung.security.entity.Membership;
import com.kampung.security.entity.Neighborhood;
import com.kampung.security.entity.User;
import com.kampung.security.repository.MembershipRepository;
import com.kampung.security.repository.NeighborhoodRepository;
import com.kampung.security.repository.UserRepository;
import com.kampung.security.security.JwtUser;
import com.kampung.security.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

// BE-Req-1, 2 & 4: Service managing neighborhood members directory and invite-based onboarding
@Service
@RequiredArgsConstructor
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final NeighborhoodRepository neighborhoodRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<MemberResponse> getMembers(String neighborhoodId, String status, String role) {
        validateAdminAccess(neighborhoodId);

        List<Membership> memberships = membershipRepository.findByNeighborhoodId(neighborhoodId);

        if (status != null && !status.isBlank()) {
            memberships = memberships.stream()
                    .filter(m -> status.equalsIgnoreCase(m.getStatus()))
                    .toList();
        }

        if (role != null && !role.isBlank()) {
            memberships = memberships.stream()
                    .filter(m -> m.getRoles().contains(role.toUpperCase()))
                    .toList();
        }

        List<String> userIds = memberships.stream().map(Membership::getUserId).distinct().toList();
        Map<String, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));

        List<MemberResponse> responses = new ArrayList<>();
        for (Membership m : memberships) {
            User u = userMap.get(m.getUserId());
            responses.add(buildMemberResponse(m, u));
        }

        return responses;
    }

    @Transactional(readOnly = true)
    public MemberResponse getMemberById(String neighborhoodId, String membershipId) {
        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Membership not found."));

        if (!membership.getNeighborhoodId().equals(neighborhoodId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Membership does not belong to this neighborhood.");
        }

        JwtUser caller = SecurityUtils.getCurrentUser();
        if (caller != null && !caller.getRoles().contains("ADMIN") && !caller.getMembershipId().equals(membershipId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to member profile.");
        }

        User user = userRepository.findById(membership.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found."));

        return buildMemberResponse(membership, user);
    }

    @Transactional
    public MemberResponse directRegisterMember(String neighborhoodId, DirectRegisterMemberRequest request) {
        validateAdminAccess(neighborhoodId);
        checkNeighborhoodExists(neighborhoodId);

        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = User.builder()
                    .userId("usr-" + UUID.randomUUID().toString().substring(0, 8))
                    .email(email)
                    .fullName(request.getFullName().trim())
                    .phoneNumber(request.getPhoneNumber())
                    .password(passwordEncoder.encode("password123"))
                    .status("ACTIVE")
                    .createdAt(LocalDateTime.now())
                    .build();
            return userRepository.save(newUser);
        });

        Set<String> roles = (request.getRoles() != null && !request.getRoles().isEmpty())
                ? new HashSet<>(request.getRoles())
                : Set.of("RESIDENT");

        Membership membership = Membership.builder()
                .membershipId("mem-" + UUID.randomUUID().toString().substring(0, 8))
                .neighborhoodId(neighborhoodId)
                .userId(user.getUserId())
                .unitNumber(request.getUnitNumber().trim())
                .roles(roles)
                .status("ACTIVE")
                .invitationToken(null)
                .createdAt(LocalDateTime.now())
                .build();

        Membership saved = membershipRepository.save(membership);
        return buildMemberResponse(saved, user);
    }

    @Transactional
    public MemberResponse inviteMember(String neighborhoodId, InviteMemberRequest request) {
        validateAdminAccess(neighborhoodId);
        checkNeighborhoodExists(neighborhoodId);

        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = User.builder()
                    .userId("usr-" + UUID.randomUUID().toString().substring(0, 8))
                    .email(email)
                    .fullName(request.getFullName().trim())
                    .phoneNumber(null)
                    .password(null)
                    .status("INVITED")
                    .createdAt(LocalDateTime.now())
                    .build();
            return userRepository.save(newUser);
        });

        Set<String> roles = (request.getRoles() != null && !request.getRoles().isEmpty())
                ? new HashSet<>(request.getRoles())
                : Set.of("RESIDENT");

        String token = "token-" + UUID.randomUUID().toString().substring(0, 8);

        Membership membership = Membership.builder()
                .membershipId("mem-" + UUID.randomUUID().toString().substring(0, 8))
                .neighborhoodId(neighborhoodId)
                .userId(user.getUserId())
                .unitNumber(request.getUnitNumber().trim())
                .roles(roles)
                .status("INVITED")
                .invitationToken(token)
                .createdAt(LocalDateTime.now())
                .build();

        Membership saved = membershipRepository.save(membership);
        return buildMemberResponse(saved, user);
    }

    @Transactional
    public MemberResponse onboardMember(String token, RegisterOnboardRequest request) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invitation token is required.");
        }

        Membership membership = membershipRepository.findByInvitationToken(token.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired invitation token."));

        if (!"INVITED".equalsIgnoreCase(membership.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Membership has already been activated.");
        }

        User user = userRepository.findById(membership.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Associated user record not found."));

        user.setFullName(request.getFullName().trim());
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber().trim());
        }
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus("ACTIVE");
        userRepository.save(user);

        membership.setStatus("ACTIVE");
        membership.setInvitationToken(null);
        Membership savedMembership = membershipRepository.save(membership);

        return buildMemberResponse(savedMembership, user);
    }

    @Transactional
    public MemberResponse updateMember(String neighborhoodId, String membershipId, UpdateMemberRequest request) {
        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Membership not found."));

        if (!membership.getNeighborhoodId().equals(neighborhoodId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Membership does not belong to this neighborhood.");
        }

        JwtUser caller = SecurityUtils.getCurrentUser();
        boolean isAdmin = caller != null && caller.getRoles().contains("ADMIN");

        if (!isAdmin && (caller == null || !caller.getMembershipId().equals(membershipId))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to update this member profile.");
        }

        User user = userRepository.findById(membership.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found."));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber().trim());
        }
        userRepository.save(user);

        if (isAdmin) {
            if (request.getUnitNumber() != null && !request.getUnitNumber().isBlank()) {
                membership.setUnitNumber(request.getUnitNumber().trim());
            }
            if (request.getStatus() != null && !request.getStatus().isBlank()) {
                membership.setStatus(request.getStatus().trim().toUpperCase());
            }
            if (request.getRoles() != null && !request.getRoles().isEmpty()) {
                membership.setRoles(new HashSet<>(request.getRoles()));
            }
        }

        Membership saved = membershipRepository.save(membership);
        return buildMemberResponse(saved, user);
    }

    @Transactional
    public void deleteMember(String neighborhoodId, String membershipId) {
        validateAdminAccess(neighborhoodId);

        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Membership not found."));

        if (!membership.getNeighborhoodId().equals(neighborhoodId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Membership does not belong to this neighborhood.");
        }

        membershipRepository.deleteById(membershipId);
    }

    private void validateAdminAccess(String neighborhoodId) {
        JwtUser caller = SecurityUtils.getCurrentUser();
        if (caller == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }
        if (!caller.getRoles().contains("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin privileges required.");
        }
        if (!neighborhoodId.equals(caller.getNeighborhoodId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admins can only manage members within their own neighborhood.");
        }
    }

    private void checkNeighborhoodExists(String neighborhoodId) {
        if (!neighborhoodRepository.existsById(neighborhoodId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Neighborhood not found with id: " + neighborhoodId);
        }
    }

    private MemberResponse buildMemberResponse(Membership m, User u) {
        return MemberResponse.builder()
                .membershipId(m.getMembershipId())
                .userId(m.getUserId())
                .neighborhoodId(m.getNeighborhoodId())
                .fullName(u != null ? u.getFullName() : "Unknown")
                .email(u != null ? u.getEmail() : "Unknown")
                .unitNumber(m.getUnitNumber())
                .phoneNumber(u != null ? u.getPhoneNumber() : null)
                .roles(new ArrayList<>(m.getRoles()))
                .status(m.getStatus())
                .invitationToken(m.getInvitationToken())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
