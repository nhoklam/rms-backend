package com.company.rms.dto.request;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class EmployeeRequest {
    // Thông tin cơ bản
    private String fullName;
    private String email;
    private String employeeCode;
    private LocalDate joinDate;
    private Long jobTitleId;
    private Long levelId;
    private Long departmentId; // Nếu có

    // Danh sách kỹ năng (Quan trọng)
    private List<SkillEntry> skills;

    @Data
    public static class SkillEntry {
        private Long skillId;
        private Integer level; // 1-5
        // private String notes; // Nếu cần
    }
}