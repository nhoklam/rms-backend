package com.company.rms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class TimeOffCreateRequest {
    @NotNull(message = "Loại nghỉ phép không được để trống")
    private String type; // ANNUAL, SICK...

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate endDate;

    private String reason;
}