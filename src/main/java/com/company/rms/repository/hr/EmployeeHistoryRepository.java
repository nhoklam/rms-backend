package com.company.rms.repository.hr;

import com.company.rms.entity.hr.EmployeeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmployeeHistoryRepository extends JpaRepository<EmployeeHistory, Long> {
    // Sửa ValidFrom -> EffectiveDate cho khớp với Entity
    List<EmployeeHistory> findByEmployeeIdOrderByEffectiveDateDesc(Long employeeId);
}