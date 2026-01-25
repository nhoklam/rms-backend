package com.company.rms.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class ResourceHealthResponse {
    // 1. Tỷ lệ Utilization
    private Double avgUtilization; // Trung bình % effort của toàn công ty
    
    // 2. Số lượng nhân viên
    private Long totalEmployees;   // Tổng nhân viên
    private Long activeCount;      // Đang có dự án
    private Long benchCount;       // Đang rảnh (Capacity = 100%)
    
    // 3. Phân bổ kỹ năng (Key: Tên Skill, Value: Số lượng người)
    private Map<String, Long> skillDistribution;
}