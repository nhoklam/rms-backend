package com.company.rms.service.hr;

import com.company.rms.dto.request.EmployeeCreateRequest;
import com.company.rms.dto.request.EmployeeUpdateRequest;
import com.company.rms.dto.request.SkillAssessmentRequest;
import com.company.rms.dto.response.EmployeeHistoryResponse;
import com.company.rms.dto.response.EmployeeProfileResponse;
import com.company.rms.dto.response.EmployeeSkillResponse;
import com.company.rms.entity.hr.*;
import com.company.rms.entity.iam.User;
import com.company.rms.entity.masterdata.*;
import com.company.rms.exception.BusinessException;
import com.company.rms.exception.ResourceNotFoundException;
import com.company.rms.repository.hr.EmployeeHistoryRepository;
import com.company.rms.repository.hr.EmployeeRepository;
import com.company.rms.repository.hr.EmployeeSkillRepository;
import com.company.rms.repository.iam.UserRepository;
import com.company.rms.repository.masterdata.*;
import com.company.rms.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeHistoryService historyService; 
    private final EmployeeSkillRepository employeeSkillRepository;
    private final DepartmentRepository departmentRepository;
    private final JobTitleRepository jobTitleRepository;
    private final LevelRepository levelRepository;
    private final SkillRepository skillRepository;
    private final EmployeeHistoryRepository historyRepository;
    private final UserRepository userRepository; 

    // 1. Update Profile (Đã thêm check Inactive)
    @Transactional
    public EmployeeProfileResponse updateEmployeeProfile(Long id, EmployeeUpdateRequest request) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        Map<String, Object> oldValue = new HashMap<>();
        Map<String, Object> newValue = new HashMap<>();
        boolean isSensitiveChange = false;

        // --- Logic Check Level ---
        if (request.getLevelId() != null && (emp.getCurrentLevel() == null || !request.getLevelId().equals(emp.getCurrentLevel().getId()))) {
            oldValue.put("levelId", emp.getCurrentLevel() != null ? emp.getCurrentLevel().getId() : null);
            newValue.put("levelId", request.getLevelId());
            
            Level newLevel = levelRepository.findById(request.getLevelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Level not found"));
            
            // [FIX] Kiểm tra Active
            if (Boolean.FALSE.equals(newLevel.getIsActive())) {
                throw new BusinessException("Cấp bậc (Level) đã chọn đang ngưng hoạt động (Inactive)");
            }

            emp.setCurrentLevel(newLevel);
            isSensitiveChange = true;
        }

        // --- Logic Check Job Title ---
        if (request.getJobTitleId() != null && (emp.getJobTitle() == null || !request.getJobTitleId().equals(emp.getJobTitle().getId()))) {
            oldValue.put("jobTitleId", emp.getJobTitle() != null ? emp.getJobTitle().getId() : null);
            newValue.put("jobTitleId", request.getJobTitleId());
            
            JobTitle newTitle = jobTitleRepository.findById(request.getJobTitleId())
                    .orElseThrow(() -> new ResourceNotFoundException("JobTitle not found"));
            
            // [FIX] Kiểm tra Active
            if (Boolean.FALSE.equals(newTitle.getIsActive())) {
                throw new BusinessException("Chức danh (Job Title) đã chọn đang ngưng hoạt động (Inactive)");
            }

            emp.setJobTitle(newTitle);
            isSensitiveChange = true;
        }

        // --- Logic Check Salary ---
        if (request.getSalary() != null && 
           (emp.getSalary() == null || request.getSalary().compareTo(emp.getSalary()) != 0)) {
            oldValue.put("salary", emp.getSalary());
            newValue.put("salary", request.getSalary());
            
            emp.setSalary(request.getSalary());
            isSensitiveChange = true;
        }

        // --- Logic Check Status ---
        if (request.getStatus() != null && (emp.getStatus() == null || !request.getStatus().equals(emp.getStatus().name()))) {
            oldValue.put("status", emp.getStatus());
            newValue.put("status", request.getStatus());
            try {
                emp.setStatus(Employee.EmployeeStatus.valueOf(request.getStatus()));
                isSensitiveChange = true;
            } catch (IllegalArgumentException e) {}
        }

        // --- Update Department ---
        if (request.getDepartmentId() != null && (emp.getDepartment() == null || !request.getDepartmentId().equals(emp.getDepartment().getId()))) {
             Department newDept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
             
             // [FIX] Kiểm tra Active
             if (Boolean.FALSE.equals(newDept.getIsActive())) {
                throw new BusinessException("Phòng ban (Department) đã chọn đang ngưng hoạt động (Inactive)");
             }

             emp.setDepartment(newDept);
        }

        if (isSensitiveChange) {
            historyService.trackEmployeeChange(
                emp, 
                EmployeeHistory.ChangeType.SALARY_ADJUSTMENT, 
                oldValue, 
                newValue, 
                LocalDate.now()
            );
        }

        Employee savedEmp = employeeRepository.save(emp);
        return getEmployeeProfile(savedEmp.getId());
    }

    @Transactional(readOnly = true)
    public EmployeeProfileResponse getEmployeeProfile(Long id) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        return EmployeeProfileResponse.builder()
                .id(emp.getId())
                .employeeCode(emp.getEmployeeCode())
                .fullName(emp.getUser() != null ? emp.getUser().getFullName() : null)
                .email(emp.getUser() != null ? emp.getUser().getEmail() : null)
                .departmentId(emp.getDepartment() != null ? emp.getDepartment().getId() : null)
                .departmentName(emp.getDepartment() != null ? emp.getDepartment().getName() : null)
                .jobTitleId(emp.getJobTitle() != null ? emp.getJobTitle().getId() : null)
                .jobTitleName(emp.getJobTitle() != null ? emp.getJobTitle().getName() : null)
                .levelId(emp.getCurrentLevel() != null ? emp.getCurrentLevel().getId() : null)
                .levelName(emp.getCurrentLevel() != null ? emp.getCurrentLevel().getName() : null)
                .salary(emp.getSalary())
                .status(emp.getStatus() != null ? emp.getStatus().name() : null)
                .joinDate(emp.getJoinDate())
                .managerId(emp.getManager() != null ? emp.getManager().getId() : null)
                .managerName(emp.getManager() != null && emp.getManager().getUser() != null 
                        ? emp.getManager().getUser().getFullName() : null)
                .build();
    }

    @Transactional(readOnly = true)
    public List<EmployeeSkillResponse> getSkills(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found");
        }
        return employeeSkillRepository.findByEmployeeId(employeeId).stream()
                .map(es -> EmployeeSkillResponse.builder()
                        .skillId(es.getSkill().getId())
                        .skillName(es.getSkill().getName())
                        .level(es.getLevel().intValue())
                        .isVerified(es.getIsVerified())
                        .verifiedAt(es.getVerifiedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmployeeHistoryResponse> getHistory(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found");
        }
        return historyRepository.findByEmployeeIdOrderByEffectiveDateDesc(employeeId).stream()
                .map(h -> {
                    Map<String, Object> oldMap = h.getOldValue() != null ? h.getOldValue() : Collections.emptyMap();
                    Map<String, Object> newMap = h.getNewValue() != null ? h.getNewValue() : Collections.emptyMap();
                    return EmployeeHistoryResponse.builder()
                            .id(h.getId())
                            .changeType(h.getChangeType() != null ? h.getChangeType().name() : "UPDATE")
                            .oldValue(oldMap)
                            .newValue(newMap)
                            .effectiveDate(h.getEffectiveDate()) 
                            .changedBy(h.getChangedBy()) 
                            .build();
                })
                .collect(Collectors.toList());
    }

    // 2. Assess Skill (Đã thêm check Inactive)
    @Transactional
    public void assessSkill(Long employeeId, SkillAssessmentRequest request, boolean isVerifier) {
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        
        Skill skill = skillRepository.findById(request.getSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));

        // [FIX] Kiểm tra Active
        if (Boolean.FALSE.equals(skill.getIsActive())) {
            throw new BusinessException("Kỹ năng (Skill) này đã ngưng sử dụng (Inactive)");
        }

        EmployeeSkillId id = new EmployeeSkillId(employeeId, request.getSkillId());

        EmployeeSkill employeeSkill = employeeSkillRepository.findById(id)
                .orElseGet(() -> EmployeeSkill.builder()
                        .id(id)
                        .employee(emp)
                        .skill(skill)
                        .isVerified(false)
                        .build());

        employeeSkill.setLevel(request.getLevel().byteValue());

        if (isVerifier) {
            employeeSkill.setIsVerified(true);
            employeeSkill.setVerifiedBy(SecurityUtils.getCurrentUserId());
            employeeSkill.setVerifiedAt(LocalDateTime.now());
        } else {
            if (!Boolean.TRUE.equals(employeeSkill.getIsVerified())) {
               employeeSkill.setIsVerified(false);
               employeeSkill.setVerifiedBy(null);
               employeeSkill.setVerifiedAt(null);
            }
        }
        employeeSkillRepository.save(employeeSkill);
    }

    @Transactional
    public void verifySkill(Long employeeId, Integer skillId) {
        EmployeeSkill employeeSkill = employeeSkillRepository.findById(new EmployeeSkillId(employeeId, skillId))
                .orElseThrow(() -> new ResourceNotFoundException("Employee Skill not found"));
        
        employeeSkill.setIsVerified(true);
        employeeSkill.setVerifiedBy(SecurityUtils.getCurrentUserId());
        employeeSkill.setVerifiedAt(LocalDateTime.now());
        
        employeeSkillRepository.save(employeeSkill);
    }

    // 3. Create Employee (Đã thêm check Inactive)
    @Transactional
    public EmployeeProfileResponse createEmployee(EmployeeCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (employeeRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new BusinessException("User is already linked to an employee profile");
        }

        if (employeeRepository.findByEmployeeCode(request.getEmployeeCode()).isPresent()) {
            throw new BusinessException("Employee code already exists");
        }

        // --- Fetch & Check Master Data ---
        Department dept = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        // [FIX] Check Active
        if (Boolean.FALSE.equals(dept.getIsActive())) {
            throw new BusinessException("Phòng ban này đã ngưng hoạt động (Inactive)");
        }

        JobTitle jobTitle = jobTitleRepository.findById(request.getJobTitleId())
                .orElseThrow(() -> new ResourceNotFoundException("JobTitle not found"));
        // [FIX] Check Active
        if (Boolean.FALSE.equals(jobTitle.getIsActive())) {
            throw new BusinessException("Chức danh này đã ngưng hoạt động (Inactive)");
        }

        Level level = levelRepository.findById(request.getLevelId())
                .orElseThrow(() -> new ResourceNotFoundException("Level not found"));
        // [FIX] Check Active
        if (Boolean.FALSE.equals(level.getIsActive())) {
            throw new BusinessException("Cấp bậc này đã ngưng hoạt động (Inactive)");
        }

        Employee employee = Employee.builder()
                .user(user)
                .employeeCode(request.getEmployeeCode())
                .department(dept)
                .jobTitle(jobTitle)
                .currentLevel(level)
                .joinDate(request.getJoinDate())
                .salary(request.getSalary())
                .status(request.getStatus() != null ? Employee.EmployeeStatus.valueOf(request.getStatus()) : Employee.EmployeeStatus.PROBATION)
                .build();

        Employee savedEmp = employeeRepository.save(employee);
        return getEmployeeProfile(savedEmp.getId());
    }
}