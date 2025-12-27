package com.company.rms.entity.hr;

import java.math.BigDecimal;
import com.company.rms.entity.base.BaseEntity;
import com.company.rms.entity.iam.User;
import com.company.rms.entity.masterdata.Department;
import com.company.rms.entity.masterdata.JobTitle;
import com.company.rms.entity.masterdata.Level;
import com.fasterxml.jackson.annotation.JsonIgnore; 
import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // 1. Import mới
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// 2. [FIX QUAN TRỌNG] Bỏ qua các field rác của Hibernate Proxy khi convert JSON
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) 
public class Employee extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    @ToString.Exclude
    @JsonIgnore // Ngăn Jackson chui vào User
    private User user;

    @Column(name = "employee_code", nullable = false, unique = true, length = 20)
    private String employeeCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore // Ngăn Jackson chui vào Department
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_title_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore // Ngăn Jackson chui vào JobTitle
    private JobTitle jobTitle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_level_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore // Ngăn Jackson chui vào Level
    private Level currentLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    @ToString.Exclude
    @JsonIgnore // [QUAN TRỌNG] Ngăn Jackson chui vào Manager (vòng lặp)
    private Employee manager;

    @Column(name = "join_date", nullable = false)
    private LocalDate joinDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EmployeeStatus status = EmployeeStatus.PROBATION;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @JsonIgnore // Ngăn load list skills
    private List<EmployeeSkill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "manager")
    @Builder.Default
    @ToString.Exclude
    @JsonIgnore // Ngăn load list subordinates
    private List<Employee> subordinates = new ArrayList<>();

    @Column(name = "salary", precision = 15, scale = 2)
    private BigDecimal salary;

    public enum EmployeeStatus {
        PROBATION, OFFICIAL, TERMINATED, ON_LEAVE
    }
}