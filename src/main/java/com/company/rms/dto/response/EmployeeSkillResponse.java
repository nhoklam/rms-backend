package com.company.rms.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class EmployeeSkillResponse {
    private Integer skillId;
    private String skillName;
    private Integer level;
    private Boolean isVerified;
    private LocalDateTime verifiedAt;
}