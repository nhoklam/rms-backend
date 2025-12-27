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

@RestController
@RequestMapping("/api/v1/timesheets")
@RequiredArgsConstructor
public class TimesheetController {
    
    private final TimesheetService timesheetService;

    /**
     * POST /api/v1/timesheets - Submit timesheet
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('EMP', 'RM', 'PM', 'ADMIN')")
    public ResponseEntity<ApiResponse<TimesheetResponse>> submitTimesheet(
            @Valid @RequestBody TimesheetSubmitRequest request) {
        
        Long employeeId = SecurityUtils.getCurrentEmployeeId();
        TimesheetResponse response = timesheetService.submitTimesheet(request, employeeId);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Timesheet submitted"));
    }
    
    /**
     * POST /api/v1/timesheets/lock - Lock timesheets for a month
     */
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
}