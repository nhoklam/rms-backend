package com.company.rms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;

@Data
public class EmployeeCreateRequest {
    @NotNull(message = "User ID is required")
    private Long userId; // Link tới tài khoản đã tạo ở Module A

    @NotBlank(message = "Employee Code is required")
    private String employeeCode;

    @NotNull(message = "Department is required")
    private Integer departmentId;

    @NotNull(message = "Job Title is required")
    private Integer jobTitleId;

    @NotNull(message = "Level is required")
    private Integer levelId;

    @NotNull(message = "Join Date is required")
    private LocalDate joinDate;

    private BigDecimal salary;
    private String status; // OFFICIAL, PROBATION
}