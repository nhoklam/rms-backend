package com.company.rms.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationUpdateRequest {
    // Cho phép cập nhật Effort
    private Integer effortPercentage;
    
    // Cho phép cập nhật ngày kết thúc (Extend/Shorten)
    private LocalDate endDate;
    
    // Bắt buộc phải có version để check Optimistic Lock
    private Long version;
}