package com.company.rms.controller.api.v1;

import com.company.rms.dto.request.AllocationRequest;
import com.company.rms.dto.request.AllocationUpdateRequest; // [Import DTO]
import com.company.rms.dto.response.AllocationResponse;
import com.company.rms.dto.response.ApiResponse;
import com.company.rms.security.SecurityUtils;
import com.company.rms.service.allocation.AllocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/allocations")
@RequiredArgsConstructor
@Slf4j
public class AllocationController {

    private final AllocationService allocationService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_RM', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<AllocationResponse>> createAllocation(
            @Valid @RequestBody AllocationRequest request) {
        log.info("Creating allocation: {}", request);
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        AllocationResponse response = allocationService.createAllocation(request, currentUserId);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "Allocation created successfully"));
    }



    @GetMapping("/capacity")
    @PreAuthorize("hasAnyAuthority('ROLE_RM', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<BigDecimal>> calculateCapacity(
            @RequestParam Long employeeId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        BigDecimal capacity = allocationService.calculateCapacity(employeeId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(capacity));
    }
    
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_RM', 'ROLE_PM', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<AllocationResponse>>> searchAllocations(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String status) {
        
        List<AllocationResponse> results = allocationService.searchAllocations(projectId, employeeId, status);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    // [FIX] Update Allocation API
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_RM', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<AllocationResponse>> updateAllocation(
            @PathVariable Long id,
            @RequestBody AllocationUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(allocationService.updateAllocation(id, request)));
    }

    @PutMapping("/{id}/terminate")
    @PreAuthorize("hasAnyAuthority('ROLE_RM', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> terminateAllocation(@PathVariable Long id) {
        allocationService.terminateAllocation(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Allocation terminated"));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyAuthority('ROLE_RM', 'ROLE_PM', 'ROLE_ADMIN', 'ROLE_EMP')")
    public ResponseEntity<ApiResponse<List<AllocationResponse>>> getCurrentAllocations(
            @PathVariable Long employeeId,
            @RequestParam(required = false) LocalDate fromDate, // Param mới
            @RequestParam(required = false) LocalDate toDate    // Param mới
    ) {
        List<AllocationResponse> allocations;
        
        if (fromDate != null && toDate != null) {
            // Logic cho Timesheet: Lấy theo tuần đã chọn
            allocations = allocationService.getAllocationsForTimesheet(employeeId, fromDate, toDate);
        } else {
            // Logic cho Dashboard: Lấy tại thời điểm hiện tại
            allocations = allocationService.getCurrentAllocations(employeeId);
        }
        
        return ResponseEntity.ok(ApiResponse.success(allocations));
    }
    // ... các import khác
    // Thêm method này vào AllocationController

    @GetMapping("/history/{employeeId}")
    @PreAuthorize("hasAnyAuthority('ROLE_RM', 'ROLE_PM', 'ROLE_ADMIN', 'ROLE_EMP')")
    public ResponseEntity<ApiResponse<List<AllocationResponse>>> getProjectHistory(@PathVariable Long employeeId) {
        // Tận dụng hàm searchAllocations đã có trong Service để lấy tất cả trạng thái
        // status = null nghĩa là lấy tất cả (ACTIVE, COMPLETED, TERMINATED)
        List<AllocationResponse> history = allocationService.searchAllocations(null, employeeId, null);
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}