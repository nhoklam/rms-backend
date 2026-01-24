package com.company.rms.controller.api.v1;

import com.company.rms.dto.request.TimeOffCreateRequest;
import com.company.rms.dto.response.ApiResponse;
import com.company.rms.dto.response.TimeOffResponse;
import com.company.rms.service.hr.TimeOffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/time-off")
@RequiredArgsConstructor
public class TimeOffController {

    private final TimeOffService timeOffService;

    @PostMapping
    public ResponseEntity<ApiResponse<TimeOffResponse>> createRequest(@Valid @RequestBody TimeOffCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(timeOffService.createRequest(request)));
    }

    @GetMapping("/my-requests")
    public ResponseEntity<ApiResponse<List<TimeOffResponse>>> getMyRequests() {
        return ResponseEntity.ok(ApiResponse.success(timeOffService.getMyRequests()));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelRequest(@PathVariable Long id) {
        timeOffService.cancelRequest(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã hủy yêu cầu nghỉ phép"));
    }

    // --- [QUAN TRỌNG] CÁC HÀM NÀY ĐANG BỊ THIẾU TRONG CODE CỦA BẠN ---

    @GetMapping("/approvals")
    @PreAuthorize("hasAnyAuthority('ROLE_RM', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<TimeOffResponse>>> getPendingApprovals() {
        return ResponseEntity.ok(ApiResponse.success(timeOffService.getPendingApprovals()));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('ROLE_RM', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> approveRequest(@PathVariable Long id) {
        timeOffService.approveRequest(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã duyệt đơn nghỉ phép"));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyAuthority('ROLE_RM', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> rejectRequest(@PathVariable Long id) {
        timeOffService.rejectRequest(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã từ chối đơn nghỉ phép"));
    }
}