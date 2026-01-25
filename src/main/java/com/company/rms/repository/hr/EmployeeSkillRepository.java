package com.company.rms.repository.hr;

import com.company.rms.entity.hr.EmployeeSkill;
import com.company.rms.entity.hr.EmployeeSkillId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeSkillRepository extends JpaRepository<EmployeeSkill, EmployeeSkillId> {
    // [BỔ SUNG] Tìm tất cả kỹ năng của một nhân viên
    List<EmployeeSkill> findByEmployeeId(Long employeeId);

    @Query("SELECT s.name, COUNT(DISTINCT es.employee.id) " +
           "FROM EmployeeSkill es " +
           "JOIN es.skill s " +
           "JOIN es.employee e " +
           "WHERE e.status = 'OFFICIAL' " +
           "GROUP BY s.name " +
           "ORDER BY COUNT(DISTINCT es.employee.id) DESC")
    List<Object[]> countEmployeesBySkill();
}