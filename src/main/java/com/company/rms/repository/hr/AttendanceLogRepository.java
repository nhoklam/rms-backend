package com.company.rms.repository.hr;

import com.company.rms.entity.hr.AttendanceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, Long> {
    Optional<AttendanceLog> findByEmployeeIdAndLogDate(Long employeeId, LocalDate logDate);
    List<AttendanceLog> findByEmployeeIdAndLogDateBetween(Long employeeId, LocalDate startDate, LocalDate endDate);
}