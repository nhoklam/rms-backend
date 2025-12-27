package com.company.rms.controller.api.v1;

import com.company.rms.dto.response.ApiResponse;
import com.company.rms.dto.response.ProjectResponse;
import com.company.rms.entity.project.Project;
import com.company.rms.repository.project.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectRepository projectRepository;

    /**
     * GET /api/v1/projects
     * Lấy danh sách dự án (Cho phép cả EMP xem để hiển thị Dashboard)
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_PM', 'ROLE_RM', 'ROLE_ADMIN', 'ROLE_EMP')")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getAllProjects() {
        List<Project> projects = projectRepository.findAll();
        
        // Map Entity -> DTO để tránh lỗi vòng lặp JSON
        List<ProjectResponse> response = projects.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private ProjectResponse mapToResponse(Project project) {
        return ProjectResponse.builder()
            .id(project.getId())
            .code(project.getCode())
            .name(project.getName())
            // Xử lý null-safe cho các quan hệ
            .clientName(project.getClient() != null ? project.getClient().getName() : null)
            .projectManagerName(project.getProjectManager() != null ? 
                                project.getProjectManager().getUser().getFullName() : null)
            .status(project.getStatus().name())
            .build();
    }
}