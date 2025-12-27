package com.company.rms.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponse {
    private Long id;
    private String code;
    private String name;
    private String clientName;
    private String projectManagerName;
    private String status; // Trả về String của Enum (ACTIVE, PIPELINE...)
}