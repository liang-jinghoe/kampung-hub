package com.kampung.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// BE-Req-1: Response payload for visitor passes
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitorPassResponse {
    private String passId;
    private String neighborhoodId;
    private String unitNumber;
    private String requesterMembershipId;
    private String visitorName;
    private String visitorPlateText;
    private String passToken;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private String status;
}
