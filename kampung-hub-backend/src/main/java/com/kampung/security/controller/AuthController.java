package com.kampung.security.controller;

import com.kampung.security.dto.AuthResponse;
import com.kampung.security.dto.LoginRequest;
import com.kampung.security.dto.SwitchContextRequest;
import com.kampung.security.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// BE-Req-1 & 2: REST Controller for Authentication and Context Switching
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // BE-Req-1: Authenticate platform credentials and bind active neighborhood membership
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // BE-Req-1: Switch active neighborhood context and obtain bound JWT token
    @PostMapping("/context")
    public ResponseEntity<AuthResponse> switchContext(@Valid @RequestBody SwitchContextRequest request) {
        AuthResponse response = authService.switchContext(request);
        return ResponseEntity.ok(response);
    }
}
