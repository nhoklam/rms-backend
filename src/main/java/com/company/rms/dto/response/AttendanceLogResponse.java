package com.company.rms.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class AttendanceLogResponse {
    private Long id;
    private Long employeeId;
    private LocalDate logDate;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private BigDecimal totalHours;
    private BigDecimal lateHours;
    private BigDecimal overtimeHours;
    private String status;
}