package com.kampung.security.repository;

import com.kampung.security.entity.VisitorPass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VisitorPassRepository extends JpaRepository<VisitorPass, String> {
    List<VisitorPass> findByNeighborhoodId(String neighborhoodId);
    Optional<VisitorPass> findByPassToken(String passToken);
    List<VisitorPass> findByNeighborhoodIdAndUnitNumber(String neighborhoodId, String unitNumber);
}
