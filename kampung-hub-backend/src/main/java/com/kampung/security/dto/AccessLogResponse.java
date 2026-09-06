package com.kampung.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// BE-Req-1: Response payload for gatehouse access logs
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessLogResponse {
    private String logId;
    private String neighborhoodId;
    private String plateText;
    private String accessType;
    private String visitorPassId;
    private String membershipId;
    private String verifiedByGuardId;
    private LocalDateTime timestamp;
}
