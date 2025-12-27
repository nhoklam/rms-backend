package com.company.rms.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class EmployeeProfileResponse {
    private Long id;
    private String employeeCode;
    private String fullName;
    private String email;
    
    private Integer departmentId;
    private String departmentName;
    
    private Integer jobTitleId;
    private String jobTitleName;
    
    private Integer levelId;
    private String levelName;
    
    private BigDecimal salary;
    private String status;
    private LocalDate joinDate;
    
    // Thông tin quản lý (nếu có)
    private Long managerId;
    private String managerName;
}