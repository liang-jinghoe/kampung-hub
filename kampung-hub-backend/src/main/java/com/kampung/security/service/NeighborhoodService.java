package com.kampung.security.service;

import com.kampung.security.dto.CreateNeighborhoodRequest;
import com.kampung.security.entity.Neighborhood;
import com.kampung.security.repository.NeighborhoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// BE-Req-1, 2 & 4: Service managing Neighborhood directory and registration
@Service
@RequiredArgsConstructor
public class NeighborhoodService {

    private final NeighborhoodRepository neighborhoodRepository;

    @Transactional(readOnly = true)
    public List<Neighborhood> getNeighborhoods(String tier, String status) {
        if (tier != null && !tier.isBlank() && status != null && !status.isBlank()) {
            return neighborhoodRepository.findBySubscriptionTierAndSubscriptionStatus(tier, status);
        } else if (tier != null && !tier.isBlank()) {
            return neighborhoodRepository.findBySubscriptionTier(tier);
        } else if (status != null && !status.isBlank()) {
            return neighborhoodRepository.findBySubscriptionStatus(status);
        }
        return neighborhoodRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Neighborhood getNeighborhoodById(String id) {
        return neighborhoodRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Neighborhood not found with id: " + id));
    }

    @Transactional
    public Neighborhood createNeighborhood(CreateNeighborhoodRequest request) {
        Neighborhood neighborhood = Neighborhood.builder()
                .id("nh-" + UUID.randomUUID().toString().substring(0, 8))
                .name(request.getName().trim())
                .propertyType(request.getPropertyType().trim().toUpperCase())
                .subscriptionTier(request.getSubscriptionTier().trim().toUpperCase())
                .subscriptionStatus("ACTIVE")
                .maxAllowedUnits(request.getMaxAllowedUnits())
                .createdAt(LocalDateTime.now())
                .build();

        return neighborhoodRepository.save(neighborhood);
    }
}
