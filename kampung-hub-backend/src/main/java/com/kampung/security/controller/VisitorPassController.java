package com.kampung.security.controller;

import com.kampung.security.dto.CreateVisitorPassRequest;
import com.kampung.security.dto.VisitorPassResponse;
import com.kampung.security.service.VisitorPassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// BE-Req-1 & 2: REST Controller for pre-registered visitor passes and gatehouse verification
@RestController
@RequestMapping("/api/v1/visitor-passes")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class VisitorPassController {

    private final VisitorPassService visitorPassService;

    // BE-Req-1: Search visitor passes by neighborhood, unit, token, or status
    @GetMapping
    public ResponseEntity<List<VisitorPassResponse>> searchVisitorPasses(
            @RequestParam(name = "neighborhoodId", required = false) String neighborhoodId,
            @RequestParam(name = "unitNumber", required = false) String unitNumber,
            @RequestParam(name = "passToken", required = false) String passToken,
            @RequestParam(name = "status", required = false) String status) {

        List<VisitorPassResponse> passes = visitorPassService.searchVisitorPasses(neighborhoodId, unitNumber, passToken, status);
        return ResponseEntity.ok(passes);
    }

    // BE-Req-1: Create temporary pre-registered visitor pass
    @PostMapping
    public ResponseEntity<VisitorPassResponse> createVisitorPass(
            @Valid @RequestBody CreateVisitorPassRequest request) {

        VisitorPassResponse created = visitorPassService.createVisitorPass(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // BE-Req-1: Guard check-in visitor pass at gatehouse and set status to USED
    @PutMapping("/{id}/check-in")
    public ResponseEntity<VisitorPassResponse> checkInVisitorPass(@PathVariable("id") String id) {
        VisitorPassResponse checkedIn = visitorPassService.checkInVisitorPass(id);
        return ResponseEntity.ok(checkedIn);
    }
}
