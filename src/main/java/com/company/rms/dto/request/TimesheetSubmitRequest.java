package com.company.rms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimesheetSubmitRequest {
    @NotNull
    private LocalDate periodStart;
    
    @NotNull
    private List<TimesheetEntryRequest> entries;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimesheetEntryRequest {
        private Long projectId;
        private LocalDate workDate;
        private Double hoursWorked;
        private String description;
    }
}