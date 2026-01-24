package com.company.rms.controller.api.v1;

import com.company.rms.dto.response.ApiResponse;
import com.company.rms.dto.response.AttendanceLogResponse; // Import DTO
import com.company.rms.service.hr.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/check-in")
    public ResponseEntity<ApiResponse<AttendanceLogResponse>> checkIn() {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.checkIn(), "Check-in thành công"));
    }

    @PostMapping("/check-out")
    public ResponseEntity<ApiResponse<AttendanceLogResponse>> checkOut() {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.checkOut(), "Check-out thành công"));
    }

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<AttendanceLogResponse>> getToday() {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getTodayLog()));
    }

    @GetMapping("/weekly-summary")
    public ResponseEntity<ApiResponse<Map<String, Double>>> getWeeklySummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getWeeklyHours(startDate, endDate)));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<AttendanceLogResponse>>> getMyHistory() {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getMyHistory()));
    }
}