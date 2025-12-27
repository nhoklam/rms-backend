package com.company.rms.entity.allocation;

import com.company.rms.entity.base.BaseEntity;
import com.company.rms.entity.hr.Employee;
import com.company.rms.entity.project.Project;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "allocations", indexes = {
    @Index(name = "idx_alloc_overlap", columnList = "employee_id, start_date, end_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Allocation extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_request_id")
    private ResourceRequest resourceRequest;
    
    @Column(name = "role_in_project", length = 50)
    private String roleInProject;
    
    @Column(name = "effort_percentage", nullable = false)
    @Builder.Default
    private Integer effortPercentage = 100;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    @Column(name = "is_shadow")
    @Builder.Default
    private Boolean isShadow = false;
    
    @Column(name = "override_bill_rate", precision = 10, scale = 2)
    private BigDecimal overrideBillRate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AllocationStatus status = AllocationStatus.ACTIVE;
    
    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    public enum AllocationStatus {
        ACTIVE, COMPLETED, TERMINATED
    }
}