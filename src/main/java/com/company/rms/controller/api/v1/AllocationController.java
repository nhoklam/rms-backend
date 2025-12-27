package com.company.rms.controller.api.v1;

import com.company.rms.dto.request.AllocationRequest;
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

    /**
     * POST /api/v1/allocations - Create new allocation (Booking)
     * Chỉ RM và ADMIN được phép thực hiện
     */
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

    /**
     * GET /api/v1/allocations/employee/{employeeId} - Get allocations history
     * Cho phép cả EMP xem lịch sử của chính mình (nếu cần mở rộng sau này)
     */
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyAuthority('ROLE_RM', 'ROLE_PM', 'ROLE_ADMIN', 'ROLE_EMP')")
    public ResponseEntity<ApiResponse<List<AllocationResponse>>> getCurrentAllocations(
            @PathVariable Long employeeId) {

        List<AllocationResponse> allocations = allocationService.getCurrentAllocations(employeeId);
        return ResponseEntity.ok(ApiResponse.success(allocations));
    }

    /**
     * GET /api/v1/allocations/capacity - Calculate remaining capacity
     */
    @GetMapping("/capacity")
    @PreAuthorize("hasAnyAuthority('ROLE_RM', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<BigDecimal>> calculateCapacity(
            @RequestParam Long employeeId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        BigDecimal capacity = allocationService.calculateCapacity(employeeId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(capacity));
    }
}