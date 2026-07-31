package com.kampung.security.repository;

import com.kampung.security.entity.ResidentVehicle;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ResidentVehicleRepository extends JpaRepository<ResidentVehicle, String> {

    List<ResidentVehicle> findByNeighborhoodIdAndStatus(String neighborhoodId, String status);

    List<ResidentVehicle> findByPlateTextContainingIgnoreCase(String plateText);

    @Query("SELECT v FROM ResidentVehicle v WHERE v.neighborhoodId = :nhId AND v.unitNumber = :unit ORDER BY v.updatedAt DESC")
    List<ResidentVehicle> findVehiclesByUnitCustom(@Param("nhId") String nhId, @Param("unit") String unit);

    @Query(value = "SELECT * FROM resident_vehicles WHERE neighborhood_id = :nhId AND status = 'ACTIVE' ORDER BY plate_text ASC", nativeQuery = true)
    List<ResidentVehicle> findActiveVehiclesNative(@Param("nhId") String nhId);

    List<ResidentVehicle> findByOwnerMembershipId(String ownerMembershipId);

    List<ResidentVehicle> findByNeighborhoodId(String neighborhoodId, Sort sort);
}
