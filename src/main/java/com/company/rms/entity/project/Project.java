package com.company.rms.entity.project;

import com.company.rms.entity.base.BaseEntity;
import com.company.rms.entity.hr.Employee;
import com.company.rms.entity.masterdata.Department;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 20)
    private String code;
    
    @Column(nullable = false, length = 150)
    private String name;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_manager_id")
    private Employee projectManager;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_unit_id")
    private Department deliveryUnit;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProjectStatus status = ProjectStatus.PIPELINE;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    public enum ProjectType {
        FIXED_PRICE, T_AND_M, ODC
    }
    
    public enum ProjectStatus {
        PIPELINE, ACTIVE, CLOSED, HOLD
    }
}