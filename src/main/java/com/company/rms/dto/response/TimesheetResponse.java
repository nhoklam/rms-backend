package com.company.rms.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimesheetResponse {
    private Long id;
    private String status;
    private String message;
}