package com.company.rms.controller.api.v1;

import com.company.rms.dto.request.EmployeeUpdateRequest;
import com.company.rms.dto.request.SkillAssessmentRequest;
import com.company.rms.dto.response.ApiResponse; // Import theo yêu cầu của bạn
import com.company.rms.dto.response.EmployeeHistoryResponse;
import com.company.rms.dto.response.EmployeeProfileResponse;
import com.company.rms.dto.response.EmployeeSkillResponse;
import com.company.rms.service.hr.EmployeeService;
import com.company.rms.security.SecurityUtils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.company.rms.dto.request.EmployeeCreateRequest;
import org.springframework.security.core.Authentication; // <--- Dòng này đang thiếu

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    // 1. Update Profile (Đã sửa để nhận DTO trả về từ Service)
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_RM', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeProfileResponse>> updateProfile(
            @PathVariable Long id, 
            @RequestBody EmployeeUpdateRequest request) {
        
        // Gọi service (giờ trả về DTO EmployeeProfileResponse) và wrap vào ApiResponse
        EmployeeProfileResponse response = employeeService.updateEmployeeProfile(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 2. Get Profile
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_RM', 'ROLE_PM', 'ROLE_ADMIN', 'ROLE_EMP')")
    public ResponseEntity<ApiResponse<EmployeeProfileResponse>> getProfile(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getEmployeeProfile(id)));
    }

    // 3. Upsert Skill (Assess Skill)
    @PostMapping("/{id}/skills")
    @PreAuthorize("hasAnyAuthority('ROLE_EMP', 'ROLE_RM', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> assessSkill(
            @PathVariable Long id,
            @RequestBody SkillAssessmentRequest request) {
        
        // SỬA: Lấy role thực tế của người đang gọi API để quyết định isVerified
        // Nếu là Admin hoặc RM thì coi như đã Verified luôn
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isVerifier = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RM") || a.getAuthority().equals("ROLE_ADMIN"));

        employeeService.assessSkill(id, request, isVerifier);
        return ResponseEntity.ok(ApiResponse.success(null, "Skill updated successfully"));
    }

    // 4. Get Skills
    @GetMapping("/{id}/skills")
    @PreAuthorize("hasAnyAuthority('ROLE_RM', 'ROLE_PM', 'ROLE_ADMIN', 'ROLE_EMP')")
    public ResponseEntity<ApiResponse<List<EmployeeSkillResponse>>> getSkills(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getSkills(id)));
    }

    // 5. Verify Skill
    @PutMapping("/{id}/skills/{skillId}/verify")
    @PreAuthorize("hasAnyAuthority('ROLE_RM', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> verifySkill(
            @PathVariable Long id,
            @PathVariable Integer skillId) {
        
        employeeService.verifySkill(id, skillId);
        return ResponseEntity.ok(ApiResponse.success(null, "Skill verified successfully"));
    }

    // 6. Get History
    @GetMapping("/{id}/history")
    @PreAuthorize("hasAnyAuthority('ROLE_RM', 'ROLE_ADMIN','ROLE_EMP')") 
    public ResponseEntity<ApiResponse<List<EmployeeHistoryResponse>>> getHistory(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getHistory(id)));
    }
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_RM', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeProfileResponse>> createEmployee(
            @RequestBody EmployeeCreateRequest request) {
        EmployeeProfileResponse response = employeeService.createEmployee(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Employee profile created successfully"));
    }
}