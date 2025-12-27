package com.company.rms.entity.hr;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSkillId implements Serializable {
    
    @Column(name = "employee_id")
    private Long employeeId; // Tên biến này PHẢI KHỚP với giá trị trong @MapsId("employeeId")

    @Column(name = "skill_id")
    private Integer skillId; // Tên biến này PHẢI KHỚP với giá trị trong @MapsId("skillId")
}