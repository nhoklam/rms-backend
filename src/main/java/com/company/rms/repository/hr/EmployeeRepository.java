package com.company.rms.repository.hr;

import com.company.rms.entity.hr.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
// FIX: Thêm <Employee, Long> vào đây
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    // FIX: Thêm <Employee>
    Optional<Employee> findByEmployeeCode(String employeeCode);
    
    // FIX: Thêm <Employee>
    Optional<Employee> findByUserId(Long userId);

    @Query("SELECT e FROM Employee e " +
           "LEFT JOIN FETCH e.currentLevel " +
           "LEFT JOIN FETCH e.jobTitle " +
           "LEFT JOIN FETCH e.department " +
           "WHERE e.id = :id")
    // FIX: Thêm <Employee>
    Optional<Employee> findByIdWithDetails(@Param("id") Long id);
}