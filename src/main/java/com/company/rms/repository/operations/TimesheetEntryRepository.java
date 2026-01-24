package com.company.rms.repository.operations;

import com.company.rms.entity.operations.TimesheetEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface TimesheetEntryRepository extends JpaRepository<TimesheetEntry, Long> {

    @Query("SELECT SUM(e.hoursWorked) FROM TimesheetEntry e " +
           "WHERE e.timesheet.employee.id = :employeeId " +
           "AND e.workDate BETWEEN :startDate AND :endDate")
    Double sumHoursByEmployeeAndDateRange(@Param("employeeId") Long employeeId,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);
}