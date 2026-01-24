package com.company.rms.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DashboardStatsResponse {
    private Integer utilization;       
    private Integer activeProjects;    
    private Double hoursLoggedThisWeek; 
    private boolean isTimesheetSubmitted; 
}