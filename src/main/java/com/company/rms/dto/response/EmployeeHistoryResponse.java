package com.company.rms.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
public class EmployeeHistoryResponse {
    private Long id;
    private String changeType;
    private Map<String, Object> oldValue; // Map để Frontend render JSON
    private Map<String, Object> newValue; // Map để Frontend render JSON
    private LocalDate effectiveDate;
    private String changedBy;
}