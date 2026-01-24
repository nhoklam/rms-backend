package com.company.rms.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class TimeOffResponse {
    private Long id;
    private String employeeName;
    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private String status;
    private String approverName;
    private Integer daysCount; // Số ngày nghỉ
}