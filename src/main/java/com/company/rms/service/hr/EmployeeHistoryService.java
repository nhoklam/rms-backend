package com.company.rms.service.hr;

import com.company.rms.entity.hr.Employee;
import com.company.rms.entity.hr.EmployeeHistory;
import com.company.rms.repository.hr.EmployeeHistoryRepository;
import com.company.rms.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // ✅ Import cái này

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeHistoryService {

    private final EmployeeHistoryRepository historyRepository;

    // ✅ FIX LỖI 500: Chỉ dùng @Transactional mặc định. 
    // TUYỆT ĐỐI KHÔNG DÙNG: (propagation = Propagation.REQUIRES_NEW)
    @Transactional 
    public void trackEmployeeChange(Employee employee, 
                                    EmployeeHistory.ChangeType changeType, 
                                    Map<String, Object> oldValue, 
                                    Map<String, Object> newValue, 
                                    LocalDate effectiveDate) {
        
        EmployeeHistory history = EmployeeHistory.builder()
                .employee(employee)
                .changeType(changeType)
                .oldValue(oldValue)
                .newValue(newValue)
                .effectiveDate(effectiveDate)
                .changedBy(SecurityUtils.getCurrentUsername())
                .build();
        
        historyRepository.save(history);
        log.info("Logged employee history change: {} for EmpID: {}", changeType, employee.getId());
    }
}