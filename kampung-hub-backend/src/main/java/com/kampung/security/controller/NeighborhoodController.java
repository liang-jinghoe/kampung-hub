package com.kampung.security.controller;

import com.kampung.security.dto.CreateNeighborhoodRequest;
import com.kampung.security.entity.Neighborhood;
import com.kampung.security.service.NeighborhoodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// BE-Req-1 & 2: REST Controller for Neighborhood resource management
@RestController
@RequestMapping("/api/v1/neighborhoods")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class NeighborhoodController {

    private final NeighborhoodService neighborhoodService;

    // BE-Req-1: Query neighborhoods filtered by plan tier or subscription status
    @GetMapping
    public ResponseEntity<List<Neighborhood>> getAllNeighborhoods(
            @RequestParam(name = "tier", required = false) String tier,
            @RequestParam(name = "status", required = false) String status) {
        List<Neighborhood> list = neighborhoodService.getNeighborhoods(tier, status);
        return ResponseEntity.ok(list);
    }

    // BE-Req-1: Fetch single neighborhood details by path parameter
    @GetMapping("/{id}")
    public ResponseEntity<Neighborhood> getNeighborhoodById(@PathVariable("id") String id) {
        Neighborhood neighborhood = neighborhoodService.getNeighborhoodById(id);
        return ResponseEntity.ok(neighborhood);
    }

    // BE-Req-1: Register a new neighborhood
    @PostMapping
    public ResponseEntity<Neighborhood> createNeighborhood(@Valid @RequestBody CreateNeighborhoodRequest request) {
        Neighborhood created = neighborhoodService.createNeighborhood(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
