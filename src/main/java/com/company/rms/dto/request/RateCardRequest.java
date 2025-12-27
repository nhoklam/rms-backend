package com.company.rms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RateCardRequest {

    @NotNull(message = "Project ID is required")
    private Long projectId;

    private Integer roleId;

    private Integer levelId;

    @NotNull(message = "Unit price is required")
    private BigDecimal unitPrice;

    private String unitTime; // "MONTHLY", "HOURLY"

    private String currency;

    @NotNull(message = "Effective start date is required")
    private LocalDate effectiveStartDate;

    private LocalDate effectiveEndDate;
}