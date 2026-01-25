package com.company.rms.service.hr;

import com.company.rms.dto.request.EmployeeCreateRequest; // [MỚI] Import DTO tạo mới
import com.company.rms.dto.request.EmployeeRequest;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final DepartmentRepository departmentRepository;
    private final JobTitleRepository jobTitleRepository;
    private final LevelRepository levelRepository;
    private final SkillRepository skillRepository;
    private final EmployeeHistoryRepository historyRepository;
    private final UserRepository userRepository;

    // ========================================================================
    // 1. CREATE EMPLOYEE (Đã sửa để dùng EmployeeCreateRequest và User có sẵn)
    // ========================================================================
    @Transactional
    public EmployeeProfileResponse createEmployee(EmployeeCreateRequest request) {
        // 1. Validate mã nhân viên
        if (employeeRepository.findByEmployeeCode(request.getEmployeeCode()).isPresent()) {
            throw new BusinessException("Mã nhân viên (Employee Code) đã tồn tại.");
        }

        // 2. Tìm User có sẵn theo ID gửi từ Frontend
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUserId()));

        // 3. Kiểm tra xem User này đã liên kết với nhân viên nào chưa
        if (employeeRepository.findByUserId(user.getId()).isPresent()) {
            throw new BusinessException("Tài khoản User này đã được liên kết với một hồ sơ nhân viên khác.");
        }

        Employee employee = new Employee();
        employee.setUser(user);
        employee.setEmployeeCode(request.getEmployeeCode());
        employee.setJoinDate(request.getJoinDate());
        employee.setSalary(request.getSalary());

        // Xử lý Status
        if (request.getStatus() != null) {
            try {
                employee.setStatus(Employee.EmployeeStatus.valueOf(request.getStatus()));
            } catch (IllegalArgumentException e) {
                employee.setStatus(Employee.EmployeeStatus.PROBATION);
            }
        } else {
            employee.setStatus(Employee.EmployeeStatus.PROBATION);
        }

        // 4. Map Master Data (Department, JobTitle, Level)
        Department dept = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        if (Boolean.FALSE.equals(dept.getIsActive())) {
            throw new BusinessException("Phòng ban này đã ngưng hoạt động.");
        }
        employee.setDepartment(dept);

        JobTitle job = jobTitleRepository.findById(request.getJobTitleId())
                .orElseThrow(() -> new ResourceNotFoundException("JobTitle not found"));
        if (Boolean.FALSE.equals(job.getIsActive())) {
            throw new BusinessException("Chức danh này đã ngưng hoạt động.");
        }
        employee.setJobTitle(job);

        Level level = levelRepository.findById(request.getLevelId())
                .orElseThrow(() -> new ResourceNotFoundException("Level not found"));
        if (Boolean.FALSE.equals(level.getIsActive())) {
            throw new BusinessException("Cấp bậc này đã ngưng hoạt động.");
        }
        employee.setCurrentLevel(level);

        // 5. Lưu Employee
        Employee savedEmp = employeeRepository.save(employee);

        // Lưu ý: Modal tạo mới ở Frontend không gửi danh sách skills, nên ta không cần xử lý skills ở đây.
        // Skills sẽ được thêm sau ở trang Profile.

        return getEmployeeProfile(savedEmp.getId());
    }

    // ========================================================================
    // 2. UPDATE EMPLOYEE (Giữ nguyên logic cập nhật Info & Skills)
    // ========================================================================
    @Transactional
    public EmployeeProfileResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        // Cập nhật thông tin cơ bản
        mapRequestToEmployee(request, emp);
        
        // Cập nhật thông tin User (FullName, Email)
        if (emp.getUser() != null) {
            emp.getUser().setFullName(request.getFullName());
            emp.getUser().setEmail(request.getEmail());
            userRepository.save(emp.getUser());
        }

        employeeRepository.save(emp);

        // Xử lý Kỹ năng: Xóa hết cái cũ -> Thêm cái mới
        if (request.getSkills() != null) {
            List<EmployeeSkill> oldSkills = employeeSkillRepository.findByEmployeeId(id);
            if (!oldSkills.isEmpty()) {
                employeeSkillRepository.deleteAll(oldSkills);
            }
            saveEmployeeSkills(emp, request.getSkills());
        }

        return getEmployeeProfile(emp.getId());
    }

    // ========================================================================
    // PRIVATE HELPER METHODS
    // ========================================================================

    private void mapRequestToEmployee(EmployeeRequest req, Employee e) {
        e.setEmployeeCode(req.getEmployeeCode());
        e.setJoinDate(req.getJoinDate());

        // Validate & Set Master Data
        if (req.getJobTitleId() != null) {
            JobTitle jobTitle = jobTitleRepository.findById(req.getJobTitleId().intValue())
                    .orElseThrow(() -> new ResourceNotFoundException("JobTitle not found"));
            if (Boolean.FALSE.equals(jobTitle.getIsActive())) {
                throw new BusinessException("Chức danh này đã ngưng hoạt động (Inactive)");
            }
            e.setJobTitle(jobTitle);
        }

        if (req.getLevelId() != null) {
            Level level = levelRepository.findById(req.getLevelId().intValue())
                    .orElseThrow(() -> new ResourceNotFoundException("Level not found"));
            if (Boolean.FALSE.equals(level.getIsActive())) {
                throw new BusinessException("Cấp bậc này đã ngưng hoạt động (Inactive)");
            }
            e.setCurrentLevel(level);
        }

        if (req.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(req.getDepartmentId().intValue())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            if (Boolean.FALSE.equals(dept.getIsActive())) {
                throw new BusinessException("Phòng ban này đã ngưng hoạt động (Inactive)");
            }
            e.setDepartment(dept);
        }
    }

    private void saveEmployeeSkills(Employee employee, List<EmployeeRequest.SkillEntry> skillEntries) {
        if (skillEntries == null || skillEntries.isEmpty()) return;

        List<EmployeeSkill> skills = skillEntries.stream().map(entry -> {
            Skill skill = skillRepository.findById(entry.getSkillId().intValue())
                    .orElseThrow(() -> new ResourceNotFoundException("Skill ID " + entry.getSkillId() + " not found"));

            if (Boolean.FALSE.equals(skill.getIsActive())) {
                throw new BusinessException("Kỹ năng " + skill.getName() + " đã ngưng hoạt động.");
            }

            return EmployeeSkill.builder()
                    .id(new EmployeeSkillId(employee.getId(), skill.getId()))
                    .employee(employee)
                    .skill(skill)
                    .level(entry.getLevel().byteValue()) 
                    .isVerified(false)
                    .build();
        }).collect(Collectors.toList());

        employeeSkillRepository.saveAll(skills);
    }

    // ========================================================================
    // READ METHODS (GET)
    // ========================================================================

    @Transactional(readOnly = true)
    public EmployeeProfileResponse getEmployeeProfile(Long id) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        // [FIX] Sửa lỗi Type Mismatch: DTO cần Integer
        Integer deptId = (emp.getDepartment() != null) ? emp.getDepartment().getId() : null;
        String deptName = (emp.getDepartment() != null) ? emp.getDepartment().getName() : null;

        Integer jobTitleId = (emp.getJobTitle() != null) ? emp.getJobTitle().getId() : null;
        String jobTitleName = (emp.getJobTitle() != null) ? emp.getJobTitle().getName() : null;

        Integer levelId = (emp.getCurrentLevel() != null) ? emp.getCurrentLevel().getId() : null;
        String levelName = (emp.getCurrentLevel() != null) ? emp.getCurrentLevel().getName() : null;
        
        // Handle Manager Info
        Long managerId = (emp.getManager() != null) ? emp.getManager().getId() : null;
        String managerName = (emp.getManager() != null && emp.getManager().getUser() != null) 
                                ? emp.getManager().getUser().getFullName() : null;

        return EmployeeProfileResponse.builder()
                .id(emp.getId())
                .employeeCode(emp.getEmployeeCode())
                .fullName(emp.getUser() != null ? emp.getUser().getFullName() : null)
                .email(emp.getUser() != null ? emp.getUser().getEmail() : null)
                
                .departmentId(deptId)
                .departmentName(deptName)
                .jobTitleId(jobTitleId)
                .jobTitleName(jobTitleName)
                .levelId(levelId)
                .levelName(levelName)
                
                .salary(emp.getSalary())
                .status(emp.getStatus() != null ? emp.getStatus().name() : null)
                .joinDate(emp.getJoinDate())
                .managerId(managerId)
                .managerName(managerName)
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
                        .level(es.getLevel() != null ? es.getLevel().intValue() : 0)
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

    // ========================================================================
    // OTHER ACTIONS (Assess, Verify)
    // ========================================================================

    @Transactional
    public void assessSkill(Long employeeId, SkillAssessmentRequest request, boolean isVerifier) {
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        Skill skill = skillRepository.findById(request.getSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));

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
}