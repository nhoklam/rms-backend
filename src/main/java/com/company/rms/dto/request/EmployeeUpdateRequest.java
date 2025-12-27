package com.company.rms.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class EmployeeUpdateRequest {
    private Integer departmentId;
    private Integer jobTitleId;
    private Integer levelId;
    private BigDecimal salary;
    private String status; // OFFICIAL, PROBATION, etc.
}