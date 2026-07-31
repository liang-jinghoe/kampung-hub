package com.kampung.security.repository;

import com.kampung.security.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, String> {
    List<Membership> findByNeighborhoodId(String neighborhoodId);
    List<Membership> findByUserId(String userId);
    Optional<Membership> findByInvitationToken(String invitationToken);
}
