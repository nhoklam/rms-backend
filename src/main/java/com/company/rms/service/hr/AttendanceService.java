package com.company.rms.service.hr;

import com.company.rms.dto.response.AttendanceLogResponse;
import com.company.rms.entity.hr.AttendanceLog;
import com.company.rms.entity.hr.Employee;
import com.company.rms.exception.BusinessException;
import com.company.rms.repository.hr.AttendanceLogRepository;
import com.company.rms.repository.hr.EmployeeRepository;
import com.company.rms.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId; // [MỚI] Import ZoneId
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceLogRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    private static final LocalTime START_TIME = LocalTime.of(9, 0);
    private static final LocalTime END_TIME = LocalTime.of(18, 0);
    private static final double LUNCH_BREAK_HOURS = 1.0;
    
    // [MỚI] Định nghĩa Zone ID Việt Nam
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private AttendanceLogResponse mapToResponse(AttendanceLog log) {
        if (log == null) return null;
        return AttendanceLogResponse.builder()
                .id(log.getId())
                .employeeId(log.getEmployee().getId())
                .logDate(log.getLogDate())
                .checkInTime(log.getCheckInTime())
                .checkOutTime(log.getCheckOutTime())
                .totalHours(log.getTotalHours())
                .lateHours(log.getLateHours())
                .overtimeHours(log.getOvertimeHours())
                .status(log.getStatus())
                .build();
    }

    @Transactional
    public AttendanceLogResponse checkIn() {
        Long empId = SecurityUtils.getCurrentEmployeeId();
        // [MỚI] Lấy ngày hiện tại theo giờ VN
        LocalDate today = LocalDate.now(VIETNAM_ZONE);

        if (attendanceRepository.findByEmployeeIdAndLogDate(empId, today).isPresent()) {
            throw new BusinessException("Hôm nay bạn đã Check-in rồi!");
        }

        Employee employee = employeeRepository.getReferenceById(empId);
        // [MỚI] Lấy giờ hiện tại theo giờ VN
        LocalDateTime now = LocalDateTime.now(VIETNAM_ZONE);
        
        AttendanceLog log = AttendanceLog.builder()
                .employee(employee)
                .logDate(today)
                .checkInTime(now)
                .status("PRESENT")
                .totalHours(BigDecimal.ZERO)
                .lateHours(BigDecimal.ZERO)
                .overtimeHours(BigDecimal.ZERO)
                .build();

        LocalTime checkInTime = now.toLocalTime();
        if (checkInTime.isAfter(START_TIME)) {
            long lateMinutes = Duration.between(START_TIME, checkInTime).toMinutes();
            // Cho phép đi trễ 5 phút (Grace period)
            if (lateMinutes > 5) {
                double lateH = lateMinutes / 60.0;
                log.setLateHours(BigDecimal.valueOf(lateH).setScale(2, RoundingMode.HALF_UP));
                log.setStatus("LATE");
            }
        }

        return mapToResponse(attendanceRepository.save(log));
    }

    @Transactional
    public AttendanceLogResponse checkOut() {
        Long empId = SecurityUtils.getCurrentEmployeeId();
        // [MỚI] Lấy ngày hiện tại theo giờ VN
        LocalDate today = LocalDate.now(VIETNAM_ZONE);

        AttendanceLog log = attendanceRepository.findByEmployeeIdAndLogDate(empId, today)
                .orElseThrow(() -> new BusinessException("Bạn chưa Check-in hôm nay!"));
        
        // [MỚI] Lấy giờ hiện tại theo giờ VN
        LocalDateTime now = LocalDateTime.now(VIETNAM_ZONE);
        log.setCheckOutTime(now);

        long durationMinutes = Duration.between(log.getCheckInTime(), now).toMinutes();
        double grossHours = durationMinutes / 60.0;
        
        // Trừ giờ nghỉ trưa nếu làm trên 5 tiếng
        double netHours = grossHours > 5.0 ? (grossHours - LUNCH_BREAK_HOURS) : grossHours;
        if (netHours < 0) netHours = 0;
        
        log.setTotalHours(BigDecimal.valueOf(netHours).setScale(2, RoundingMode.HALF_UP));

        LocalTime checkOutTime = now.toLocalTime();
        if (checkOutTime.isAfter(END_TIME)) {
            long otMinutes = Duration.between(END_TIME, checkOutTime).toMinutes();
            // Tính OT nếu làm thêm trên 15 phút
            if (otMinutes > 15) {
                double otH = otMinutes / 60.0;
                log.setOvertimeHours(BigDecimal.valueOf(otH).setScale(2, RoundingMode.HALF_UP));
            }
        }

        return mapToResponse(attendanceRepository.save(log));
    }

    @Transactional(readOnly = true)
    public AttendanceLogResponse getTodayLog() {
        Long empId = SecurityUtils.getCurrentEmployeeId();
        if (empId == null) return null;
        // [MỚI] Lấy log theo ngày VN
        return mapToResponse(attendanceRepository.findByEmployeeIdAndLogDate(empId, LocalDate.now(VIETNAM_ZONE)).orElse(null));
    }

    @Transactional(readOnly = true)
    public List<AttendanceLogResponse> getMyHistory() {
        Long empId = SecurityUtils.getCurrentEmployeeId();
        // [MỚI] Lấy lịch sử 30 ngày gần nhất theo giờ VN
        LocalDate now = LocalDate.now(VIETNAM_ZONE);
        LocalDate start = now.minusDays(30);
        return attendanceRepository.findByEmployeeIdAndLogDateBetween(empId, start, now)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Double> getWeeklyHours(LocalDate start, LocalDate end) {
        Long empId = SecurityUtils.getCurrentEmployeeId();
        if (empId == null) return Map.of();
        List<AttendanceLog> logs = attendanceRepository.findByEmployeeIdAndLogDateBetween(empId, start, end);
        return logs.stream().collect(Collectors.toMap(
                log -> log.getLogDate().toString(),
                log -> log.getTotalHours().doubleValue()
        ));
    }
}