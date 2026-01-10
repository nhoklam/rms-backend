package com.company.rms.controller.api.v1;

import com.company.rms.dto.request.TimesheetSubmitRequest;
import com.company.rms.dto.response.ApiResponse;
import com.company.rms.dto.response.TimesheetResponse;
import com.company.rms.security.SecurityUtils;
import com.company.rms.service.operations.TimesheetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List; // [FIX] Thêm import này để sửa lỗi "List cannot be resolved"

@RestController
@RequestMapping("/api/v1/timesheets")
@RequiredArgsConstructor
public class TimesheetController {
    
    private final TimesheetService timesheetService;

    @PostMapping
    @PreAuthorize("hasAnyRole('EMP', 'RM', 'PM', 'ADMIN')")
    public ResponseEntity<ApiResponse<TimesheetResponse>> submitTimesheet(
            @Valid @RequestBody TimesheetSubmitRequest request) {
        
        Long employeeId = SecurityUtils.getCurrentEmployeeId();
        TimesheetResponse response = timesheetService.submitTimesheet(request, employeeId);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Timesheet submitted"));
    }
    
    @PostMapping("/lock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> lockTimesheets(
            @RequestParam int year,
            @RequestParam int month) {
        
        int lockedCount = timesheetService.lockTimesheets(year, month);
        return ResponseEntity.ok(ApiResponse.success(
            String.format("Locked %d timesheets", lockedCount)
        ));
    }
    
    // [FIX] Thêm các API Duyệt cho Module E hoàn thiện
    @GetMapping("/approvals")
    @PreAuthorize("hasAnyAuthority('ROLE_PM', 'ROLE_ADMIN')") // Đảm bảo có ROLE_ADMIN
    public ResponseEntity<ApiResponse<List<TimesheetResponse>>> getPendingApprovals() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(timesheetService.getPendingApprovals(currentUserId)));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('ROLE_PM', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> approveTimesheet(@PathVariable Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        timesheetService.approveTimesheet(id, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(null, "Timesheet approved"));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyAuthority('ROLE_PM', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> rejectTimesheet(@PathVariable Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        timesheetService.rejectTimesheet(id, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(null, "Timesheet rejected"));
    }
}