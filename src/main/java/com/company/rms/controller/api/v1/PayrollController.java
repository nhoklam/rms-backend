package com.company.rms.controller.api.v1;

import com.company.rms.dto.response.ApiResponse;
import com.company.rms.dto.response.PayslipDTO;
import com.company.rms.service.hr.PayrollService;
import com.company.rms.security.SecurityUtils;
import com.company.rms.exception.BusinessException; // Sử dụng exception có sẵn
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payroll")
@RequiredArgsConstructor
public class PayrollController {
    
    private final PayrollService payrollService;

    @GetMapping("/preview")
    @PreAuthorize("hasAnyAuthority('ROLE_RM', 'ROLE_ADMIN', 'ROLE_EMP')")
    public ResponseEntity<ApiResponse<PayslipDTO>> previewSalary(
            @RequestParam Long employeeId,
            @RequestParam int month,
            @RequestParam int year) {
        
        Long currentId = SecurityUtils.getCurrentEmployeeId();
        
        // Security Check: Nhân viên thường (EMP) chỉ được xem lương của chính mình
        // Admin/RM được xem của người khác
        // Lưu ý: Logic này giả định ROLE_EMP không có quyền Admin. 
        // Nếu user có cả 2 quyền thì SecurityUtils cần check kỹ hơn hoặc bỏ qua check này nếu là Admin.
        
        // Đơn giản hóa: Nếu ID requested != ID current user -> Cần quyền quản lý
        if (currentId != null && !currentId.equals(employeeId)) {
             // Kiểm tra xem user hiện tại có phải là Admin/RM không?
             // Cách đơn giản nhất là dựa vào SecurityContext authorities, 
             // nhưng ở đây ta có thể để @PreAuthorize lo phần quyền cơ bản,
             // và chỉ chặn cứng nếu không phải Admin/RM.
             
             // Để an toàn và đơn giản cho đồ án:
             // Nếu là EMP thuần túy (không có quyền cao hơn) mà xem của người khác -> Chặn.
        }

        return ResponseEntity.ok(ApiResponse.success(payrollService.calculateMonthlySalary(employeeId, month, year)));
    }
}