package com.company.rms.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllocationResponse {
    private Long id;
    private Long projectId;
    private String projectCode;
    private String projectName;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private String roleInProject;
    private Integer effortPercentage;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isShadow;
    private BigDecimal overrideBillRate;
    private String status;
    private Long version;
    private String projectManagerName;
}