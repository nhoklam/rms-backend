package com.company.rms.repository.hr;

import com.company.rms.entity.hr.EmployeeSkill;
import com.company.rms.entity.hr.EmployeeSkillId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeSkillRepository extends JpaRepository<EmployeeSkill, EmployeeSkillId> {
    // [BỔ SUNG] Tìm tất cả kỹ năng của một nhân viên
    List<EmployeeSkill> findByEmployeeId(Long employeeId);
}