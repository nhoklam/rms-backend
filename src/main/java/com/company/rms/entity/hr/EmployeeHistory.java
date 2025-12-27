package com.company.rms.entity.hr;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "employee_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false)
    private ChangeType changeType;

    @Type(JsonType.class)
    @Column(name = "old_value", columnDefinition = "json")
    private Map<String, Object> oldValue;

    @Type(JsonType.class)
    @Column(name = "new_value", columnDefinition = "json")
    private Map<String, Object> newValue;

    // SỬA: Đảm bảo tên field này thống nhất
    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate; 

    // SỬA: Đổi từ createdBy (Long) sang changedBy (String) để lưu Username người sửa
    @Column(name = "changed_by")
    private String changedBy; 
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum ChangeType {
        PROMOTION, DEMOTION, TRANSFER, SALARY_ADJUSTMENT, UPDATE
    }
}