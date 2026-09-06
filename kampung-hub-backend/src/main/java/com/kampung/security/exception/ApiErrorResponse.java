package com.kampung.security.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// BE-Req-6: Centralized API Error Response DTO
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiErrorResponse {
    private int statusCode;
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;
}
