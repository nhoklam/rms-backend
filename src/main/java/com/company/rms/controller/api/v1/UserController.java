package com.company.rms.controller.api.v1;

import com.company.rms.dto.request.UserCreateRequest;
import com.company.rms.dto.response.ApiResponse;
import com.company.rms.dto.response.UserResponse;
import com.company.rms.service.iam.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.company.rms.dto.request.UserUpdateRequest;
import com.company.rms.dto.request.ChangePasswordRequest;
import com.company.rms.security.SecurityUtils; // Để lấy ID người đang đăng nhập


import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RM')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers()));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.createUser(request)));
    }
    
    // --- BỔ SUNG PUT ---
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id, 
            @Valid @RequestBody UserUpdateRequest request) { // <-- Đổi thành UserUpdateRequest
        return ResponseEntity.ok(ApiResponse.success(userService.updateUser(id, request)));
    }

    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggleStatus(@PathVariable Long id) {
        userService.toggleUserStatus(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User status updated"));
    }
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()") 
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        userService.changePassword(currentUserId, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Đổi mật khẩu thành công"));
    }
}