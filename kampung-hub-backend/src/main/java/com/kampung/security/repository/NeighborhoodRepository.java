package com.kampung.security.repository;

import com.kampung.security.entity.Neighborhood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NeighborhoodRepository extends JpaRepository<Neighborhood, String> {
}
