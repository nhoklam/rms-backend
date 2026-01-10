package com.company.rms.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllocationRequest {
    
    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Project ID is required")
    private Long projectId;
    
    private Long resourceRequestId;

    @Size(max = 50, message = "Role name too long")
    private String roleInProject;

    @NotNull(message = "Effort percentage is required")
    @Min(value = 1, message = "Effort must be at least 1%")
    @Max(value = 100, message = "Effort cannot exceed 100%")
    private Integer effortPercentage;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be in the future")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;
    
    private Boolean isShadow;

    @DecimalMin(value = "0.0", message = "Bill rate must be positive")
    private BigDecimal overrideBillRate;

    private Long version;

    @AssertTrue(message = "End date must be after start date")
    private boolean isValidDateRange() {
        if (startDate == null || endDate == null) return true;
        return endDate.isAfter(startDate);
    }
}