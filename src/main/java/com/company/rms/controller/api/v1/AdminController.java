package com.company.rms.controller.api.v1;

import com.company.rms.dto.response.ApiResponse;
import com.company.rms.entity.iam.SystemAuditLog;
import com.company.rms.repository.iam.SystemAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final SystemAuditLogRepository auditLogRepository;

    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('ADMIN')") // Chỉ Admin mới được xem
    public ResponseEntity<ApiResponse<Page<SystemAuditLog>>> getAuditLogs(
            @PageableDefault(sort = "changedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<SystemAuditLog> logs = auditLogRepository.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}