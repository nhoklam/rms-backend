package com.company.rms.controller.api.v1;

import com.company.rms.dto.request.EmployeeRequest;
import com.company.rms.dto.request.SkillAssessmentRequest;
import com.company.rms.dto.response.ApiResponse;
import com.company.rms.dto.response.EmployeeHistoryResponse;
import com.company.rms.dto.response.EmployeeProfileResponse;
import com.company.rms.dto.response.EmployeeSkillResponse;
import com.company.rms.service.hr.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.company.rms.dto.request.EmployeeCreateRequest;
import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    // 1. Create Employee (Gộp logic tạo mới)
// [Tìm hàm createEmployee và thay đổi tham số đầu vào]
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_RM', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeProfileResponse>> createEmployee(
            @RequestBody EmployeeCreateRequest request) { // ĐỔI TỪ EmployeeRequest SANG EmployeeCreateRequest
        EmployeeProfileResponse response = employeeService.createEmployee(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Employee created successfully"));
    }
    // 2. Update Employee Profile (Info + Skills)
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_RM', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeProfileResponse>> updateEmployee(
            @PathVariable Long id,
            @RequestBody EmployeeRequest request) {
        EmployeeProfileResponse response = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Employee updated successfully"));
    }

    // 3. Get Profile
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_RM', 'ROLE_PM', 'ROLE_ADMIN', 'ROLE_EMP')")
    public ResponseEntity<ApiResponse<EmployeeProfileResponse>> getProfile(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getEmployeeProfile(id)));
    }

    // 4. Upsert Skill (Assess Skill)
    @PostMapping("/{id}/skills")
    @PreAuthorize("hasAnyAuthority('ROLE_EMP', 'ROLE_RM', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> assessSkill(
            @PathVariable Long id,
            @RequestBody SkillAssessmentRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isVerifier = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RM") || a.getAuthority().equals("ROLE_ADMIN"));

        employeeService.assessSkill(id, request, isVerifier);
        return ResponseEntity.ok(ApiResponse.success(null, "Skill updated successfully"));
    }

    // 5. Get Skills
    @GetMapping("/{id}/skills")
    @PreAuthorize("hasAnyAuthority('ROLE_RM', 'ROLE_PM', 'ROLE_ADMIN', 'ROLE_EMP')")
    public ResponseEntity<ApiResponse<List<EmployeeSkillResponse>>> getSkills(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getSkills(id)));
    }

    // 6. Verify Skill
    @PutMapping("/{id}/skills/{skillId}/verify")
    @PreAuthorize("hasAnyAuthority('ROLE_RM', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> verifySkill(
            @PathVariable Long id,
            @PathVariable Integer skillId) {

        employeeService.verifySkill(id, skillId);
        return ResponseEntity.ok(ApiResponse.success(null, "Skill verified successfully"));
    }

    // 7. Get History
    @GetMapping("/{id}/history")
    @PreAuthorize("hasAnyAuthority('ROLE_RM', 'ROLE_ADMIN','ROLE_EMP')")
    public ResponseEntity<ApiResponse<List<EmployeeHistoryResponse>>> getHistory(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getHistory(id)));
    }
}