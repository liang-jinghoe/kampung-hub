package com.kampung.security.service;

import com.kampung.security.dto.AuthResponse;
import com.kampung.security.dto.LoginRequest;
import com.kampung.security.dto.MembershipContextDto;
import com.kampung.security.dto.SwitchContextRequest;
import com.kampung.security.entity.Membership;
import com.kampung.security.entity.Neighborhood;
import com.kampung.security.entity.User;
import com.kampung.security.repository.MembershipRepository;
import com.kampung.security.repository.NeighborhoodRepository;
import com.kampung.security.repository.UserRepository;
import com.kampung.security.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// BE-Req-1, 2 & 4: Service handling user authentication and multi-neighborhood context switching
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final NeighborhoodRepository neighborhoodRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password."));

        if (user.getPassword() == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        if ("INACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your user profile is inactive.");
        }

        List<Membership> activeMemberships = membershipRepository.findByUserId(user.getUserId()).stream()
                .filter(m -> "ACTIVE".equalsIgnoreCase(m.getStatus()))
                .toList();

        if (activeMemberships.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No active memberships found for this account.");
        }

        List<MembershipContextDto> membershipDtos = mapToMembershipContextDtos(activeMemberships);

        MembershipContextDto activeContext = selectActiveMembershipContext(
                membershipDtos,
                request.getPreferredMembershipId()
        );

        String token = jwtUtil.generateToken(
                user.getUserId(),
                user.getEmail(),
                user.getFullName(),
                activeContext.getRoles(),
                activeContext.getMembershipId(),
                activeContext.getNeighborhoodId()
        );

        return AuthResponse.builder()
                .token(token)
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .activeMembership(activeContext)
                .memberships(membershipDtos)
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse switchContext(SwitchContextRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found."));

        Membership targetMembership = membershipRepository.findById(request.getMembershipId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target membership not found."));

        if (!targetMembership.getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own the requested membership.");
        }

        if (!"ACTIVE".equalsIgnoreCase(targetMembership.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Target membership is not active.");
        }

        List<Membership> activeMemberships = membershipRepository.findByUserId(user.getUserId()).stream()
                .filter(m -> "ACTIVE".equalsIgnoreCase(m.getStatus()))
                .toList();

        List<MembershipContextDto> membershipDtos = mapToMembershipContextDtos(activeMemberships);

        MembershipContextDto activeContext = membershipDtos.stream()
                .filter(m -> m.getMembershipId().equals(targetMembership.getMembershipId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to resolve context."));

        String token = jwtUtil.generateToken(
                user.getUserId(),
                user.getEmail(),
                user.getFullName(),
                activeContext.getRoles(),
                activeContext.getMembershipId(),
                activeContext.getNeighborhoodId()
        );

        return AuthResponse.builder()
                .token(token)
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .activeMembership(activeContext)
                .memberships(membershipDtos)
                .build();
    }

    private List<MembershipContextDto> mapToMembershipContextDtos(List<Membership> memberships) {
        List<String> neighborhoodIds = memberships.stream().map(Membership::getNeighborhoodId).distinct().toList();
        Map<String, String> neighborhoodNameMap = neighborhoodRepository.findAllById(neighborhoodIds).stream()
                .collect(Collectors.toMap(Neighborhood::getId, Neighborhood::getName));

        List<MembershipContextDto> result = new ArrayList<>();
        for (Membership m : memberships) {
            String nhName = neighborhoodNameMap.getOrDefault(m.getNeighborhoodId(), "Unknown Neighborhood");
            result.add(MembershipContextDto.builder()
                    .membershipId(m.getMembershipId())
                    .neighborhoodId(m.getNeighborhoodId())
                    .neighborhoodName(nhName)
                    .unitNumber(m.getUnitNumber())
                    .roles(new ArrayList<>(m.getRoles()))
                    .build());
        }
        return result;
    }

    private MembershipContextDto selectActiveMembershipContext(List<MembershipContextDto> list, String preferredId) {
        if (preferredId != null && !preferredId.isBlank()) {
            for (MembershipContextDto dto : list) {
                if (dto.getMembershipId().equals(preferredId)) {
                    return dto;
                }
            }
        }
        return list.get(0);
    }
}
