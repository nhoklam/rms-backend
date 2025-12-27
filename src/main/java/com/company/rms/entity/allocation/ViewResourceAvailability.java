package com.company.rms.entity.allocation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import java.math.BigDecimal;

@Entity
@Immutable
@Subselect("SELECT * FROM view_resource_availability")
@Getter
@Setter
public class ViewResourceAvailability {
    
    @Id
    @Column(name = "employee_id")
    private Long employeeId;
    
    @Column(name = "employee_code")
    private String employeeCode;
    
    @Column(name = "full_name")
    private String fullName;
    
    @Column(name = "job_title")
    private String jobTitle;
    
    @Column(name = "level_name")
    private String levelName;
    
    @Column(name = "skills_list", columnDefinition = "TEXT")
    private String skillsList;
    
    @Column(name = "current_load")
    private BigDecimal currentLoad;
    
    @Column(name = "available_capacity")
    private BigDecimal availableCapacity;
}