package com.company.rms.repository.project;

import com.company.rms.entity.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
// FIX: Thêm <Project, Long> vào đây
public interface ProjectRepository extends JpaRepository<Project, Long> {
}   