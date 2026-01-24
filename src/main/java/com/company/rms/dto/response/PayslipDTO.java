package com.company.rms.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class PayslipDTO {
    // Thông tin nhân viên
    private String employeeName;
    private String employeeCode;
    private String jobTitle;
    
    // Thông số cơ bản
    private BigDecimal baseSalary;      // Lương cứng
    private BigDecimal hourlyRate;      // Lương theo giờ
    private Integer standardWorkDays;   // Số ngày công chuẩn (VD: 22)
    
    // Tổng hợp thời gian
    private BigDecimal totalWorkHours;  // Tổng giờ làm thực tế
    private BigDecimal totalOtHours;    // Tổng giờ tăng ca
    private BigDecimal totalLateHours;  // Tổng giờ đi muộn
    
    // Các khoản Cộng (+)
    private BigDecimal otPay;           // Tiền OT
    
    // Các khoản Trừ (-)
    private BigDecimal lateDeduction;      // Trừ đi muộn
    private BigDecimal insuranceDeduction; // Trừ bảo hiểm (Fixed %)
    private BigDecimal taxDeduction;       // Trừ thuế (Fixed Amount)
    
    // Thực lĩnh
    private BigDecimal finalSalary;
}