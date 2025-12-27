package com.company.rms.entity.operations;

import com.company.rms.entity.hr.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "timesheets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Timesheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TimesheetStatus status = TimesheetStatus.DRAFT;

    @Column(name = "is_locked")
    @Builder.Default
    private Boolean isLocked = false;

    @OneToMany(mappedBy = "timesheet", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TimesheetEntry> entries = new ArrayList<>();

    public enum TimesheetStatus {
        DRAFT, SUBMITTED, APPROVED, REJECTED
    }
}