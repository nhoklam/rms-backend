package com.company.rms.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RateCardResponse {
    private Long id;
    private Long projectId;
    private Integer roleId;
    private String roleName; // Hiển thị tên Role thay vì ID
    private Integer levelId;
    private String levelName; // Hiển thị tên Level
    private BigDecimal unitPrice;
    private String currency;
    private String unitTime;
    private LocalDate effectiveStartDate;
    private LocalDate effectiveEndDate;
}