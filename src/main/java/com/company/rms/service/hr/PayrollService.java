package com.company.rms.service.hr;

import com.company.rms.dto.response.PayslipDTO;
import com.company.rms.entity.hr.AttendanceLog;
import com.company.rms.entity.hr.Employee;
import com.company.rms.repository.hr.AttendanceLogRepository;
import com.company.rms.repository.hr.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PayrollService {

    private final EmployeeRepository employeeRepository;
    private final AttendanceLogRepository attendanceRepository;

    // --- CẤU HÌNH TÍNH LƯƠNG (CONSTANTS) ---
    private static final int STANDARD_WORK_DAYS = 22; // 22 ngày công chuẩn
    private static final BigDecimal HOURS_PER_DAY = BigDecimal.valueOf(8);
    
    // Hệ số
    private static final BigDecimal OT_MULTIPLIER = BigDecimal.valueOf(1.5);   // OT nhân 1.5
    private static final BigDecimal LATE_MULTIPLIER = BigDecimal.valueOf(1.0); // Phạt muộn trừ 1.0 (trừ đúng số giờ muộn)
    
    // Khấu trừ cố định
    private static final BigDecimal INSURANCE_RATE = BigDecimal.valueOf(0.105); // 10.5% (BHXH, BHYT, BHTN)
    private static final BigDecimal FIXED_TAX_AMOUNT = BigDecimal.valueOf(500000); // Thuế cố định 500k
    private static final BigDecimal TAX_THRESHOLD = BigDecimal.valueOf(11000000);  // Lương > 11tr mới đóng thuế

    @Transactional(readOnly = true)
    public PayslipDTO calculateMonthlySalary(Long employeeId, int month, int year) {
        // 1. Lấy thông tin nhân viên
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        
        BigDecimal baseSalary = emp.getSalary() != null ? emp.getSalary() : BigDecimal.ZERO;

        // 2. Tính Lương theo giờ (Hourly Rate)
        // Công thức: Lương giờ = Lương cứng / (22 ngày * 8 giờ)
        BigDecimal standardMonthlyHours = BigDecimal.valueOf(STANDARD_WORK_DAYS).multiply(HOURS_PER_DAY);
        BigDecimal hourlyRate = BigDecimal.ZERO;
        
        if (standardMonthlyHours.compareTo(BigDecimal.ZERO) > 0) {
            hourlyRate = baseSalary.divide(standardMonthlyHours, 2, RoundingMode.HALF_UP);
        }

        // 3. Truy vấn dữ liệu chấm công trong tháng
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        
        List<AttendanceLog> logs = attendanceRepository.findByEmployeeIdAndLogDateBetween(employeeId, start, end);

        // 4. Tổng hợp giờ làm
        BigDecimal totalWorkHours = BigDecimal.ZERO;
        BigDecimal totalOtHours = BigDecimal.ZERO;
        BigDecimal totalLateHours = BigDecimal.ZERO;

        for (AttendanceLog log : logs) {
            if (log.getTotalHours() != null) totalWorkHours = totalWorkHours.add(log.getTotalHours());
            if (log.getOvertimeHours() != null) totalOtHours = totalOtHours.add(log.getOvertimeHours());
            if (log.getLateHours() != null) totalLateHours = totalLateHours.add(log.getLateHours());
        }

        // 5. Tính toán thành tiền
        // (+) Tiền OT
        BigDecimal otPay = totalOtHours.multiply(hourlyRate).multiply(OT_MULTIPLIER);
        
        // (-) Tiền Phạt muộn
        BigDecimal lateDeduction = totalLateHours.multiply(hourlyRate).multiply(LATE_MULTIPLIER);

        // (-) Bảo hiểm (10.5% trên lương cứng)
        BigDecimal insuranceDeduction = baseSalary.multiply(INSURANCE_RATE);

        // (-) Thuế TNCN (Logic: Nếu lương cứng > 11tr thì trừ 500k, ngược lại là 0)
        BigDecimal taxDeduction = BigDecimal.ZERO;
        if (baseSalary.compareTo(TAX_THRESHOLD) >= 0) {
            taxDeduction = FIXED_TAX_AMOUNT;
        }

        // 6. Tính Lương Thực Lĩnh (Final Salary)
        // Final = Lương Cứng + OT - Phạt Muộn - Bảo Hiểm - Thuế
        BigDecimal finalSalary = baseSalary
                .add(otPay)
                .subtract(lateDeduction)
                .subtract(insuranceDeduction)
                .subtract(taxDeduction);

        // Đảm bảo lương không bị âm
        if (finalSalary.compareTo(BigDecimal.ZERO) < 0) {
            finalSalary = BigDecimal.ZERO;
        }

        // 7. Trả về DTO
        return PayslipDTO.builder()
                .employeeName(emp.getUser() != null ? emp.getUser().getFullName() : "N/A")
                .employeeCode(emp.getEmployeeCode())
                .jobTitle(emp.getJobTitle() != null ? emp.getJobTitle().getName() : "")
                .standardWorkDays(STANDARD_WORK_DAYS)
                .baseSalary(baseSalary)
                .hourlyRate(hourlyRate)
                .totalWorkHours(totalWorkHours)
                .totalOtHours(totalOtHours)
                .totalLateHours(totalLateHours)
                .otPay(otPay.setScale(0, RoundingMode.HALF_UP))
                .lateDeduction(lateDeduction.setScale(0, RoundingMode.HALF_UP))
                .insuranceDeduction(insuranceDeduction.setScale(0, RoundingMode.HALF_UP))
                .taxDeduction(taxDeduction.setScale(0, RoundingMode.HALF_UP))
                .finalSalary(finalSalary.setScale(0, RoundingMode.HALF_UP))
                .build();
    }
}