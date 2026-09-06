package com.kampung.security.service;

import com.kampung.security.dto.AccessLogResponse;
import com.kampung.security.dto.CreateAccessLogRequest;
import com.kampung.security.entity.AccessLog;
import com.kampung.security.repository.AccessLogRepository;
import com.kampung.security.security.JwtUser;
import com.kampung.security.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// BE-Req-1, 2 & 4: Service for querying and creating gatehouse checkpoint entry/exit access logs
@Service
@RequiredArgsConstructor
public class AccessLogService {

    private final AccessLogRepository accessLogRepository;

    @Transactional(readOnly = true)
    public List<AccessLogResponse> getAccessLogs(String neighborhoodId, int page, int size, String sortBy) {
        JwtUser caller = SecurityUtils.getCurrentUser();
        if (caller == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }

        boolean isAdmin = caller.getRoles().contains("ADMIN");
        boolean isGuard = caller.getRoles().contains("GUARD");

        if (!isAdmin && !isGuard) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access to checkpoint logs requires Guard or Admin authority.");
        }

        String targetNhId = (neighborhoodId != null && !neighborhoodId.isBlank()) ? neighborhoodId : caller.getNeighborhoodId();

        String sortProperty = (sortBy != null && !sortBy.isBlank()) ? sortBy : "timestamp";
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortProperty));

        Page<AccessLog> logs = accessLogRepository.findByNeighborhoodId(targetNhId, pageable);
        return logs.getContent().stream().map(this::buildAccessLogResponse).toList();
    }

    @Transactional
    public AccessLogResponse createAccessLog(CreateAccessLogRequest request) {
        JwtUser caller = SecurityUtils.getCurrentUser();
        if (caller == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }

        boolean isAdmin = caller.getRoles().contains("ADMIN");
        boolean isGuard = caller.getRoles().contains("GUARD");

        if (!isAdmin && !isGuard) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access logs can only be created by gate guards.");
        }

        String targetNhId = (request.getNeighborhoodId() != null && !request.getNeighborhoodId().isBlank())
                ? request.getNeighborhoodId() : caller.getNeighborhoodId();

        AccessLog logEntry = AccessLog.builder()
                .logId("log-" + UUID.randomUUID().toString().substring(0, 8))
                .neighborhoodId(targetNhId)
                .plateText(request.getPlateText().trim().toUpperCase())
                .accessType(request.getAccessType().trim().toUpperCase())
                .visitorPassId(request.getVisitorPassId())
                .membershipId(request.getMembershipId())
                .verifiedByGuardId(caller.getUserId())
                .timestamp(LocalDateTime.now())
                .build();

        AccessLog saved = accessLogRepository.save(logEntry);
        return buildAccessLogResponse(saved);
    }

    private AccessLogResponse buildAccessLogResponse(AccessLog log) {
        return AccessLogResponse.builder()
                .logId(log.getLogId())
                .neighborhoodId(log.getNeighborhoodId())
                .plateText(log.getPlateText())
                .accessType(log.getAccessType())
                .visitorPassId(log.getVisitorPassId())
                .membershipId(log.getMembershipId())
                .verifiedByGuardId(log.getVerifiedByGuardId())
                .timestamp(log.getTimestamp())
                .build();
    }
}
