package com.company.rms.service.general;

import com.company.rms.dto.response.ResourceHealthResponse;
import com.company.rms.repository.allocation.ViewResourceAvailabilityRepository;
import com.company.rms.repository.hr.EmployeeSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final ViewResourceAvailabilityRepository viewRepository;
    private final EmployeeSkillRepository skillRepository;

    @Transactional(readOnly = true)
    public ResourceHealthResponse getResourceHealthStats() {
        // 1. Lấy thống kê Utilization & Bench từ View
        Double avgUtil = viewRepository.getAverageUtilization();
        Long totalEmp = viewRepository.countTotalResources();
        Long benchEmp = viewRepository.countBenchResources();
        
        // Xử lý null
        if (avgUtil == null) avgUtil = 0.0;
        if (totalEmp == null) totalEmp = 0L;
        if (benchEmp == null) benchEmp = 0L;

        Long activeEmp = totalEmp - benchEmp;

        // 2. Lấy thống kê Skill (Top 10 skill phổ biến nhất)
        List<Object[]> skillData = skillRepository.countEmployeesBySkill();
        
        // Convert List<Object[]> sang Map<String, Long>
        // Dùng LinkedHashMap để giữ thứ tự sort từ Query
        Map<String, Long> skillMap = skillData.stream()
                .limit(10) // Chỉ lấy top 10 skill để biểu đồ đỡ rối
                .collect(Collectors.toMap(
                        obj -> (String) obj[0],      // Skill Name
                        obj -> ((Number) obj[1]).longValue(), // Count
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        return ResourceHealthResponse.builder()
                .avgUtilization(Math.round(avgUtil * 100.0) / 100.0) // Làm tròn 2 số thập phân
                .totalEmployees(totalEmp)
                .activeCount(activeEmp)
                .benchCount(benchEmp)
                .skillDistribution(skillMap)
                .build();
    }
}