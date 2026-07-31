package com.kampung.security.repository;

import com.kampung.security.entity.AccessLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessLogRepository extends JpaRepository<AccessLog, String> {
    Page<AccessLog> findByNeighborhoodId(String neighborhoodId, Pageable pageable);
}
