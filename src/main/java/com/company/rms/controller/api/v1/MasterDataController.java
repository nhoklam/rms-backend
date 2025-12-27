package com.company.rms.controller.api.v1;

import com.company.rms.dto.response.ApiResponse;
import com.company.rms.entity.masterdata.Department;
import com.company.rms.entity.masterdata.JobTitle;
import com.company.rms.entity.masterdata.Level;
import com.company.rms.entity.masterdata.Skill;
import com.company.rms.repository.masterdata.DepartmentRepository;
import com.company.rms.repository.masterdata.JobTitleRepository;
import com.company.rms.repository.masterdata.LevelRepository;
import com.company.rms.repository.masterdata.SkillRepository;
import com.company.rms.exception.BusinessException;
import com.company.rms.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/master-data")
@RequiredArgsConstructor
public class MasterDataController {

    private final DepartmentRepository departmentRepository;
    private final JobTitleRepository jobTitleRepository;
    private final LevelRepository levelRepository;
    private final SkillRepository skillRepository;

    // --- DEPARTMENTS ---
    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<List<Department>>> getDepartments() {
        return ResponseEntity.ok(ApiResponse.success(departmentRepository.findAll()));
    }

    @PostMapping("/departments")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Department>> createDepartment(@RequestBody Department department) {
        if (departmentRepository.findAll().stream().anyMatch(d -> d.getCode().equals(department.getCode()))) {
            throw new BusinessException("Department code already exists");
        }
        return ResponseEntity.ok(ApiResponse.success(departmentRepository.save(department)));
    }

    @PutMapping("/departments/{id}/toggle-status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggleDepartmentStatus(@PathVariable Integer id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        dept.setIsActive(dept.getIsActive() == null || !dept.getIsActive());
        departmentRepository.save(dept);
        return ResponseEntity.ok(ApiResponse.success(null, "Updated department status"));
    }

    // --- JOB TITLES ---
    @GetMapping("/job-titles")
    public ResponseEntity<ApiResponse<List<JobTitle>>> getJobTitles() {
        return ResponseEntity.ok(ApiResponse.success(jobTitleRepository.findAll()));
    }

    @PostMapping("/job-titles")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<JobTitle>> createJobTitle(@RequestBody JobTitle jobTitle) {
        return ResponseEntity.ok(ApiResponse.success(jobTitleRepository.save(jobTitle)));
    }

    @PutMapping("/job-titles/{id}/toggle-status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggleJobTitleStatus(@PathVariable Integer id) {
        JobTitle jobTitle = jobTitleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job title not found"));
        jobTitle.setIsActive(jobTitle.getIsActive() == null || !jobTitle.getIsActive());
        jobTitleRepository.save(jobTitle);
        return ResponseEntity.ok(ApiResponse.success(null, "Updated job title status"));
    }

    // --- LEVELS ---
    @GetMapping("/levels")
    public ResponseEntity<ApiResponse<List<Level>>> getLevels() {
        // Nên sort theo rankOrder để hiển thị đúng thứ tự cấp bậc
        // Tuy nhiên ở đây dùng findAll mặc định, frontend sẽ sort hoặc DB trả về ngẫu nhiên
        return ResponseEntity.ok(ApiResponse.success(levelRepository.findAll()));
    }
    @PostMapping("/levels")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Level>> createLevel(@RequestBody Level level) {
        return ResponseEntity.ok(ApiResponse.success(levelRepository.save(level)));
    }
    @PutMapping("/levels/{id}/toggle-status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggleLevelStatus(@PathVariable Integer id) {
        Level level = levelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Level not found"));
        level.setIsActive(level.getIsActive() == null || !level.getIsActive());
        levelRepository.save(level);
        return ResponseEntity.ok(ApiResponse.success(null, "Updated level status"));
    }

    // --- SKILLS ---
    @GetMapping("/skills")
    public ResponseEntity<ApiResponse<List<Skill>>> getSkills() {
        return ResponseEntity.ok(ApiResponse.success(skillRepository.findAll()));
    }

    @PostMapping("/skills")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Skill>> createSkill(@RequestBody Skill skill) {
        return ResponseEntity.ok(ApiResponse.success(skillRepository.save(skill)));
    }

    @PutMapping("/skills/{id}/toggle-status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggleSkillStatus(@PathVariable Integer id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));
        skill.setIsActive(skill.getIsActive() == null || !skill.getIsActive());
        skillRepository.save(skill);
        return ResponseEntity.ok(ApiResponse.success(null, "Updated skill status"));
    }
}