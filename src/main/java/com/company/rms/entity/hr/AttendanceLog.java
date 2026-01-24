package com.company.rms.entity.hr;

import com.company.rms.entity.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceLog extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnore
    private Employee employee;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @Column(name = "total_hours")
    @Builder.Default
    private BigDecimal totalHours = BigDecimal.ZERO;

    // [MỚI]
    @Column(name = "late_hours")
    @Builder.Default
    private BigDecimal lateHours = BigDecimal.ZERO;

    // [MỚI]
    @Column(name = "overtime_hours")
    @Builder.Default
    private BigDecimal overtimeHours = BigDecimal.ZERO;

    private String status; // PRESENT, LATE, LEAVE_EARLY
}