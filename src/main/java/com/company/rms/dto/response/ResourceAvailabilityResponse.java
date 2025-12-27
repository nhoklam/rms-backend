package com.company.rms.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceAvailabilityResponse {
    private Long employeeId;
    
    private String employeeCode;
    
    private String fullName;
    
    private String jobTitle;
    
    private String levelName;
    
    // Danh sách kỹ năng dạng chuỗi (VD: "Java, Spring Boot, React")
    private String skillsList; 
    
    // Tổng effort hiện tại (%)
    private BigDecimal currentLoad; 
    
    // Capacity còn lại (%) = 100 - currentLoad
    private BigDecimal availableCapacity; 
}