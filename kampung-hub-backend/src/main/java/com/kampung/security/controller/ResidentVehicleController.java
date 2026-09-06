package com.kampung.security.controller;

import com.kampung.security.dto.CreateVehicleRequest;
import com.kampung.security.dto.UpdateVehicleRequest;
import com.kampung.security.entity.ResidentVehicle;
import com.kampung.security.service.ResidentVehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// BE-Req-1 & 2: REST Controller for resident vehicles whitelist management
@RestController
@RequestMapping("/api/v1/vehicles")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class ResidentVehicleController {

    private final ResidentVehicleService vehicleService;

    // BE-Req-1 & 2: GET Endpoint accepting query parameters
    @GetMapping
    public ResponseEntity<List<ResidentVehicle>> getAllVehicles(
            @RequestParam(name = "neighborhoodId", required = false) String neighborhoodId,
            @RequestParam(name = "ownerMembershipId", required = false) String ownerMembershipId,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "sortBy", defaultValue = "plateText") String sortBy) {

        List<ResidentVehicle> result = vehicleService.getVehicles(
                neighborhoodId, ownerMembershipId, status, sortBy);
        return ResponseEntity.ok(result);
    }

    // BE-Req-1 & 2: GET Endpoint accepting path parameters
    @GetMapping("/{id}")
    public ResponseEntity<ResidentVehicle> getVehicleById(@PathVariable("id") String id) {
        ResidentVehicle vehicle = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(vehicle);
    }

    // BE-Req-1 & 2: POST Endpoint for vehicle registration
    @PostMapping
    public ResponseEntity<ResidentVehicle> createVehicle(@Valid @RequestBody CreateVehicleRequest request) {
        ResidentVehicle savedVehicle = vehicleService.createVehicle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedVehicle);
    }

    // BE-Req-1 & 2: PUT Endpoint for vehicle updates
    @PutMapping("/{id}")
    public ResponseEntity<ResidentVehicle> updateVehicle(
            @PathVariable("id") String id,
            @Valid @RequestBody UpdateVehicleRequest request) {

        ResidentVehicle updated = vehicleService.updateVehicle(id, request);
        return ResponseEntity.ok(updated);
    }

    // BE-Req-1 & 2: DELETE Endpoint
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable("id") String id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}
