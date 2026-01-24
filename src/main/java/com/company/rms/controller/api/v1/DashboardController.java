package com.company.rms.controller.api.v1;

import com.company.rms.dto.response.ApiResponse;
// [FIX] Import đúng từ package response
import com.company.rms.dto.response.DashboardStatsResponse;
// [FIX] Import đúng từ package allocation
import com.company.rms.repository.allocation.AllocationRepository;
// [FIX] Import đúng từ package operations
import com.company.rms.repository.operations.TimesheetEntryRepository;
import com.company.rms.security.SecurityUtils; // Import để lấy current user
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.temporal.ChronoField;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final AllocationRepository allocationRepository;
    private final TimesheetEntryRepository timesheetEntryRepository;

    @GetMapping("/employee/stats")
    @PreAuthorize("hasAnyAuthority('ROLE_EMP', 'ROLE_PM', 'ROLE_RM', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getMyStats() {
        // [FIX] Lấy ID động từ token
        Long employeeId = SecurityUtils.getCurrentEmployeeId();
        
        // Nếu user chưa có employeeId (VD: admin thuần), trả về 0 hết
        if (employeeId == null) {
             return ResponseEntity.ok(ApiResponse.success(
                DashboardStatsResponse.builder()
                    .utilization(0)
                    .activeProjects(0)
                    .hoursLoggedThisWeek(0.0)
                    .isTimesheetSubmitted(false)
                    .build()
            ));
        }

        // 2. Tính Utilization
        Integer utilization = allocationRepository.sumEffortByEmployeeIdAndDate(employeeId, LocalDate.now());
        if (utilization == null) utilization = 0;

        // 3. Đếm số dự án Active
        Integer activeProjects = allocationRepository.countActiveProjectsByEmployee(employeeId, LocalDate.now());

        // 4. Tính giờ làm tuần này
        LocalDate now = LocalDate.now();
        LocalDate startOfWeek = now.with(ChronoField.DAY_OF_WEEK, 1);
        LocalDate endOfWeek = now.with(ChronoField.DAY_OF_WEEK, 7);
        
        Double hours = timesheetEntryRepository.sumHoursByEmployeeAndDateRange(employeeId, startOfWeek, endOfWeek);
        if (hours == null) hours = 0.0;

        return ResponseEntity.ok(ApiResponse.success(
            DashboardStatsResponse.builder()
                .utilization(utilization)
                .activeProjects(activeProjects != null ? activeProjects : 0)
                .hoursLoggedThisWeek(hours)
                .isTimesheetSubmitted(false) 
                .build()
        ));
    }
}