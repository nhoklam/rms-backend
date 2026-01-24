package com.company.rms.repository.hr;

import com.company.rms.entity.hr.TimeOffRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TimeOffRequestRepository extends JpaRepository<TimeOffRequest, Long> {
    
    // [FIX] Có JOIN FETCH user
    @Query("SELECT t FROM TimeOffRequest t " +
           "JOIN FETCH t.employee e " +
           "JOIN FETCH e.user u " +
           "WHERE e.id = :employeeId " +
           "ORDER BY t.startDate DESC")
    List<TimeOffRequest> findByEmployeeIdOrderByStartDateDesc(@Param("employeeId") Long employeeId);

    // [FIX] Có JOIN FETCH user
    @Query("SELECT t FROM TimeOffRequest t " +
           "JOIN FETCH t.employee e " +
           "JOIN FETCH e.user u " +
           "WHERE t.status = 'PENDING' " +
           "AND t.employee.id <> :currentEmpId " +
           "ORDER BY t.startDate ASC")
    List<TimeOffRequest> findAllPendingExceptSelf(@Param("currentEmpId") Long currentEmpId);
}