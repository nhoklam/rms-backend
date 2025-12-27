package com.company.rms.dto.request;

import lombok.Data;

@Data
public class SkillAssessmentRequest {
    private Integer skillId;
    private Integer level; // 1-5
}