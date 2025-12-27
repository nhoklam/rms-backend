package com.company.rms.repository.masterdata;
import com.company.rms.entity.masterdata.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Integer> {}