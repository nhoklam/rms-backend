package com.company.rms.repository.operations;

import com.company.rms.entity.operations.Timesheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
// FIX: Thêm <Timesheet, Long> vào đây là HẾT LỖI
public interface TimesheetRepository extends JpaRepository<Timesheet, Long> {

    // FIX: Thêm <Timesheet>
    Optional<Timesheet> findByEmployeeIdAndPeriodStart(Long employeeId, LocalDate periodStart);

    @Query("SELECT t FROM Timesheet t " +
           "WHERE t.employee.id = :employeeId " +
           "AND YEAR(t.periodStart) = :year " +
           "AND MONTH(t.periodStart) = :month")
    // FIX: Thêm <Timesheet>
    List<Timesheet> findByEmployeeAndYearMonth(
        @Param("employeeId") Long employeeId,
        @Param("year") int year,
        @Param("month") int month
    );

    @Query("SELECT t FROM Timesheet t " +
           "WHERE t.isLocked = false " +
           "AND t.status = 'APPROVED' " +
           "AND YEAR(t.periodStart) = :year " +
           "AND MONTH(t.periodStart) = :month")
    // FIX: Thêm <Timesheet>
    List<Timesheet> findUnlockedTimesheets(
        @Param("year") int year,
        @Param("month") int month
    );
@Query("SELECT DISTINCT t FROM Timesheet t " +
           "JOIN t.entries e " +
           "WHERE e.project.projectManager.user.id = :userId " +
           "AND t.status = 'SUBMITTED' " +
           "AND t.employee.user.id <> :userId") 
    List<Timesheet> findPendingApprovalByPm(@Param("userId") Long userId);

    // [MỚI] Hàm dành cho Admin: Lấy TẤT CẢ Timesheet đang chờ duyệt
    @Query("SELECT t FROM Timesheet t WHERE t.status = 'SUBMITTED'")
    List<Timesheet> findAllPendingTimesheets();
}