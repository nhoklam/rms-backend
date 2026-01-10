package com.company.rms.repository.project;

import com.company.rms.entity.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    // [FIX] Thêm Query này để fetch luôn Client và User của ProjectManager
    // Giúp tránh lỗi LazyInitializationException
    @Query("SELECT p FROM Project p " +
           "LEFT JOIN FETCH p.client " +
           "LEFT JOIN FETCH p.projectManager pm " +
           "LEFT JOIN FETCH pm.user")
    List<Project> findAllWithDetails();
}