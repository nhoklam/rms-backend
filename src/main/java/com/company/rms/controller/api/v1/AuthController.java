package com.company.rms.controller.api.v1;

import com.company.rms.dto.request.LoginRequest;
import com.company.rms.dto.response.ApiResponse;
import com.company.rms.dto.response.JwtResponse;
import com.company.rms.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;

    /**
     * POST /api/v1/auth/login - Login and get JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        
        JwtResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }
}