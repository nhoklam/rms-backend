package com.company.rms.entity.masterdata;

import com.company.rms.entity.hr.Employee;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false, unique = true, length = 20)
    private String code;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(name = "cost_center_code", length = 50)
    private String costCenterCode;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Employee manager;
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
