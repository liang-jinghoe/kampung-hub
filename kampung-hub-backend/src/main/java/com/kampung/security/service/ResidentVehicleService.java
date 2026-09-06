package com.kampung.security.service;

import com.kampung.security.dto.CreateVehicleRequest;
import com.kampung.security.dto.UpdateVehicleRequest;
import com.kampung.security.entity.Membership;
import com.kampung.security.entity.ResidentVehicle;
import com.kampung.security.repository.MembershipRepository;
import com.kampung.security.repository.ResidentVehicleRepository;
import com.kampung.security.security.JwtUser;
import com.kampung.security.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// BE-Req-2, 4 & 5: Service for managing resident vehicle whitelist records and enforcing role security boundaries
@Service
@RequiredArgsConstructor
public class ResidentVehicleService {

    private final ResidentVehicleRepository vehicleRepository;
    private final MembershipRepository membershipRepository;

    // BE-Req-4 & 5: Read vehicles list filtered by caller security context
    @Transactional(readOnly = true)
    public List<ResidentVehicle> getVehicles(String neighborhoodId, String ownerMembershipId, String status, String sortBy) {
        JwtUser caller = SecurityUtils.getCurrentUser();
        if (caller == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User authentication required.");
        }

        String sortField = (sortBy != null && !sortBy.isBlank()) ? sortBy : "plateText";
        Sort sort = Sort.by(Sort.Direction.ASC, sortField);
        String targetNhId = (neighborhoodId != null && !neighborhoodId.isBlank()) ? neighborhoodId : caller.getNeighborhoodId();

        boolean isAdmin = caller.getRoles().contains("ADMIN");
        boolean isGuard = caller.getRoles().contains("GUARD");

        if (!isAdmin && !isGuard) {
            // Security boundary: Residents are strictly locked to their own membership context
            return vehicleRepository.findByOwnerMembershipId(caller.getMembershipId());
        } else if (ownerMembershipId != null && !ownerMembershipId.isBlank()) {
            // Admins/Guards are allowed to query vehicles for a specific membership ID
            return vehicleRepository.findByOwnerMembershipId(ownerMembershipId);
        } else if (status != null && !status.isBlank()) {
            return vehicleRepository.findByNeighborhoodIdAndStatus(targetNhId, status);
        } else {
            return vehicleRepository.findByNeighborhoodId(targetNhId, sort);
        }
    }

    // BE-Req-4: Fetch and authorize reading a single vehicle record
    @Transactional(readOnly = true)
    public ResidentVehicle getVehicleById(String id) {
        ResidentVehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found with id: " + id));

        validateVehicleAccess(vehicle, SecurityUtils.getCurrentUser(), "READ");
        return vehicle;
    }

    // BE-Req-4: Save vehicle whitelisting details and inject JWT context for Residents
    @Transactional
    public ResidentVehicle createVehicle(CreateVehicleRequest request) {
        JwtUser caller = SecurityUtils.getCurrentUser();
        if (caller == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User authentication required.");
        }

        boolean isAdmin = caller.getRoles().contains("ADMIN");
        boolean isGuard = caller.getRoles().contains("GUARD");

        if (isGuard && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Security guards cannot register vehicles.");
        }

        String targetNhId;
        String targetMembershipId;
        String targetUnitNumber;

        if (!isAdmin) {
            // Residents automatically bound to their authenticated membership context
            targetNhId = caller.getNeighborhoodId();
            targetMembershipId = caller.getMembershipId();

            Membership membership = membershipRepository.findById(targetMembershipId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Caller membership not found."));
            targetUnitNumber = membership.getUnitNumber();
        } else {
            // Admins can specify owner or default to caller
            targetNhId = (request.getNeighborhoodId() != null && !request.getNeighborhoodId().isBlank())
                    ? request.getNeighborhoodId() : caller.getNeighborhoodId();
            targetMembershipId = (request.getOwnerMembershipId() != null && !request.getOwnerMembershipId().isBlank())
                    ? request.getOwnerMembershipId() : caller.getMembershipId();

            Membership membership = membershipRepository.findById(targetMembershipId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Specified owner membership not found."));
            targetUnitNumber = (request.getUnitNumber() != null && !request.getUnitNumber().isBlank())
                    ? request.getUnitNumber() : membership.getUnitNumber();
        }

        ResidentVehicle vehicle = ResidentVehicle.builder()
                .vehicleId("veh-" + UUID.randomUUID().toString().substring(0, 8))
                .neighborhoodId(targetNhId)
                .ownerMembershipId(targetMembershipId)
                .unitNumber(targetUnitNumber)
                .plateText(request.getPlateText().trim().toUpperCase())
                .model(request.getModel().trim())
                .color(request.getColor().trim().toUpperCase())
                .zoneMask(request.getZoneMask() != null ? request.getZoneMask().trim() : "ALL")
                .status("ACTIVE")
                .updatedAt(LocalDateTime.now())
                .build();

        return vehicleRepository.save(vehicle);
    }

    // BE-Req-4: Update authorized vehicle properties
    @Transactional
    public ResidentVehicle updateVehicle(String id, UpdateVehicleRequest request) {
        ResidentVehicle existing = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found with id: " + id));

        validateVehicleAccess(existing, SecurityUtils.getCurrentUser(), "WRITE");

        existing.setModel(request.getModel().trim());
        existing.setColor(request.getColor().trim().toUpperCase());
        existing.setStatus(request.getStatus().trim().toUpperCase());
        if (request.getZoneMask() != null) {
            existing.setZoneMask(request.getZoneMask().trim());
        }
        existing.setUpdatedAt(LocalDateTime.now());

        return vehicleRepository.save(existing);
    }

    // BE-Req-4: Delete vehicle whitelisting record
    @Transactional
    public void deleteVehicle(String id) {
        ResidentVehicle existing = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found with id: " + id));

        validateVehicleAccess(existing, SecurityUtils.getCurrentUser(), "WRITE");
        vehicleRepository.deleteById(id);
    }

    // Private Helper enforcing context-bound authorization constraints
    private void validateVehicleAccess(ResidentVehicle vehicle, JwtUser caller, String requiredPermission) {
        if (caller == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User authentication required.");
        }

        boolean isAdmin = caller.getRoles().contains("ADMIN");
        boolean isGuard = caller.getRoles().contains("GUARD");

        // Security boundary 1: Admins can only manage vehicles inside their registered neighborhood
        if (isAdmin) {
            if (!vehicle.getNeighborhoodId().equals(caller.getNeighborhoodId())) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Admins can only manage vehicles within their own neighborhood.");
            }
        }
        // Security boundary 2: Guards have READ access for checking plates, but no WRITE permission
        else if (isGuard) {
            if ("WRITE".equals(requiredPermission)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Security guards do not have write access for whitelisted vehicles.");
            }
            if (!vehicle.getNeighborhoodId().equals(caller.getNeighborhoodId())) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Guards can only view vehicles within their own neighborhood.");
            }
        }
        // Security boundary 3: Residents can only CRUD vehicles they own (matching their membership ID)
        else {
            if (!vehicle.getOwnerMembershipId().equals(caller.getMembershipId())) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Residents can only manage their own vehicles.");
            }
        }
    }
}
