package com.company.rms.repository.allocation;

import com.company.rms.entity.allocation.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
// FIX: Thêm <Allocation, Long> để không bị lỗi Raw Type
public interface AllocationRepository extends JpaRepository<Allocation, Long> {
    
    @Query("SELECT a FROM Allocation a " +
           "WHERE a.employee.id = :employeeId " +
           "AND a.status = 'ACTIVE' " +
           "AND a.startDate <= :endDate " +
           "AND a.endDate >= :startDate")
    // FIX: Sửa List thành List<Allocation>
    List<Allocation> findOverlappingAllocations(
        @Param("employeeId") Long employeeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT a FROM Allocation a " +
           "WHERE a.employee.id = :employeeId " +
           "AND a.status = 'ACTIVE' " +
           "AND CURRENT_DATE BETWEEN a.startDate AND a.endDate")
    // FIX: Sửa List thành List<Allocation>
    List<Allocation> findCurrentAllocations(@Param("employeeId") Long employeeId);

    @Query("SELECT a FROM Allocation a " +
           "WHERE a.project.id = :projectId " +
           "AND a.status = 'ACTIVE' " +
           "AND ((YEAR(a.startDate) = :year AND MONTH(a.startDate) = :month) " +
           "OR (YEAR(a.endDate) = :year AND MONTH(a.endDate) = :month) " +
           "OR (a.startDate < :firstDay AND a.endDate > :lastDay))")
    // FIX: Sửa List thành List<Allocation>
    List<Allocation> findByProjectAndMonth(
        @Param("projectId") Long projectId,
        @Param("year") int year,
        @Param("month") int month,
        @Param("firstDay") LocalDate firstDay,
        @Param("lastDay") LocalDate lastDay
    );
}