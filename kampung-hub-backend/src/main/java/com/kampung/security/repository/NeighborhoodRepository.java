package com.kampung.security.repository;

import com.kampung.security.entity.Neighborhood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// BE-Req-4 & 5: Spring Data JPA Repository for Neighborhood entities
@Repository
public interface NeighborhoodRepository extends JpaRepository<Neighborhood, String> {
    List<Neighborhood> findBySubscriptionTierAndSubscriptionStatus(String subscriptionTier, String subscriptionStatus);
    List<Neighborhood> findBySubscriptionTier(String subscriptionTier);
    List<Neighborhood> findBySubscriptionStatus(String subscriptionStatus);
}
