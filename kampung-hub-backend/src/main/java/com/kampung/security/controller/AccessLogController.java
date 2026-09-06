package com.kampung.security.controller;

import com.kampung.security.dto.AccessLogResponse;
import com.kampung.security.dto.CreateAccessLogRequest;
import com.kampung.security.service.AccessLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// BE-Req-1 & 2: REST Controller for gatehouse checkpoint logs
@RestController
@RequestMapping("/api/v1/access-logs")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class AccessLogController {

    private final AccessLogService accessLogService;

    // BE-Req-1: Fetch paginated access logs for guardhouse and admin views
    @GetMapping
    public ResponseEntity<List<AccessLogResponse>> getAccessLogs(
            @RequestParam(name = "neighborhoodId", required = false) String neighborhoodId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sortBy", defaultValue = "timestamp") String sortBy) {

        List<AccessLogResponse> logs = accessLogService.getAccessLogs(neighborhoodId, page, size, sortBy);
        return ResponseEntity.ok(logs);
    }

    // BE-Req-1: Log gate access event
    @PostMapping
    public ResponseEntity<AccessLogResponse> createAccessLog(
            @Valid @RequestBody CreateAccessLogRequest request) {

        AccessLogResponse created = accessLogService.createAccessLog(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
