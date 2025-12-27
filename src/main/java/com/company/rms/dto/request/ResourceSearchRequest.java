package com.company.rms.dto.request;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceSearchRequest {
    private String jobTitle;
    private String levelName;
    private List skillIds;
    private Byte minSkillLevel;
    private BigDecimal minCapacity;
}