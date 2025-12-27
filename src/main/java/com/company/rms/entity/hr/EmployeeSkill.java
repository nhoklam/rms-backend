package com.company.rms.entity.hr;

import com.company.rms.entity.masterdata.Skill;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee_skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeSkill {

    // 1. Sử dụng EmbeddedId thay vì IdClass
    // Điều này giúp code tường minh: đây là khóa chính phức hợp
    @EmbeddedId
    private EmployeeSkillId id;

    // 2. MapsId("employeeId"): 
    // Báo cho JPA biết trường "employee" này sẽ map vào field "employeeId" bên trong EmployeeSkillId
    @MapsId("employeeId") 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // 3. MapsId("skillId"):
    // Tương tự, map vào field "skillId" bên trong EmployeeSkillId
    @MapsId("skillId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(nullable = false)
    private Byte level; // 1-5

    @Column(name = "years_experience", precision = 4, scale = 1)
    private BigDecimal yearsExperience;

    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "verified_by")
    private Long verifiedBy;
    
    // Helper method để khởi tạo ID nếu dùng Builder (Quan trọng)
    @PrePersist
    private void ensureId() {
        if (this.id == null) {
            this.id = new EmployeeSkillId();
        }
        if (this.employee != null) {
            this.id.setEmployeeId(this.employee.getId());
        }
        if (this.skill != null) {
            this.id.setSkillId(this.skill.getId());
        }
    }
}