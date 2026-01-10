package com.company.rms.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimesheetResponse {
    private Long id;
    private String status;
    private String message;
    
    // [MỚI] Các field bổ sung cho màn hình Approval
    private Long employeeId;
    private String employeeName;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal totalHours;
    private List<EntryDetail> details;

    @Getter
    @Setter
    @Builder
    public static class EntryDetail {
        private String projectName;
        private LocalDate workDate;
        private BigDecimal hours;
        private String description;
    }
}