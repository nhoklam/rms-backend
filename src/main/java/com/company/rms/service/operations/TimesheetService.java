package com.company.rms.service.operations;

import com.company.rms.dto.request.TimesheetSubmitRequest;
import com.company.rms.dto.response.TimesheetResponse;
import com.company.rms.entity.allocation.Allocation;
import com.company.rms.entity.operations.Timesheet;
import com.company.rms.entity.operations.TimesheetEntry;
import com.company.rms.entity.project.Project;
import com.company.rms.entity.hr.Employee;
import com.company.rms.exception.BusinessException;
import com.company.rms.repository.allocation.AllocationRepository;
import com.company.rms.repository.hr.EmployeeRepository; // Thêm import
import com.company.rms.repository.operations.TimesheetRepository;
import com.company.rms.repository.project.ProjectRepository; // Thêm import
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimesheetService {

    private final TimesheetRepository timesheetRepository;
    private final AllocationRepository allocationRepository;
    private final EmployeeRepository employeeRepository; // Inject thêm
    private final ProjectRepository projectRepository;   // Inject thêm

    /**
     * Submit timesheet with validation
     */
    @Transactional
    public TimesheetResponse submitTimesheet(TimesheetSubmitRequest request, Long employeeId) {
        // Validation 1: Check if locked
        Timesheet existingTs = timesheetRepository
            .findByEmployeeIdAndPeriodStart(employeeId, request.getPeriodStart())
            .orElse(null);

        if (existingTs != null && Boolean.TRUE.equals(existingTs.getIsLocked())) {
            throw new BusinessException("Timesheet is locked. Cannot modify.");
        }

        // Validation 2: Check allocations for each entry
        // (Logic giữ nguyên như bạn cung cấp)
        for (var entryReq : request.getEntries()) {
            LocalDate workDate = entryReq.getWorkDate();
            Long projectId = entryReq.getProjectId();

            List<Allocation> activeAllocations = allocationRepository
                .findOverlappingAllocations(employeeId, workDate, workDate);

            boolean hasAllocation = activeAllocations.stream()
                .anyMatch(a -> a.getProject().getId().equals(projectId) &&
                               a.getStatus() == Allocation.AllocationStatus.ACTIVE);

            if (!hasAllocation) {
                throw new BusinessException(
                    String.format("No active allocation found for project %d on date %s",
                                projectId, workDate)
                );
            }
        }

        // --- BẮT ĐẦU LOGIC SAVE THỰC TẾ ---

        if (existingTs == null) {
            // Create New Timesheet Header
            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new BusinessException("Employee not found"));

            existingTs = Timesheet.builder()
                    .employee(employee)
                    .periodStart(request.getPeriodStart())
                    .periodEnd(request.getPeriodStart().plusDays(6)) // Giả sử tuần 7 ngày
                    .status(Timesheet.TimesheetStatus.SUBMITTED)
                    .isLocked(false)
                    .entries(new ArrayList<>())
                    .build();
        } else {
            // Update Existing: Clear old entries to replace with new ones
            // (Do entity Timesheet có orphanRemoval = true[cite: 510], nên clear list sẽ xóa record DB)
            existingTs.getEntries().clear();
            existingTs.setStatus(Timesheet.TimesheetStatus.SUBMITTED);
        }

        // Map DTO Entries -> Entity Entries
        for (var reqEntry : request.getEntries()) {
            Project project = projectRepository.findById(reqEntry.getProjectId())
                    .orElseThrow(() -> new BusinessException("Project not found: " + reqEntry.getProjectId()));

            TimesheetEntry entry = TimesheetEntry.builder()
                    .timesheet(existingTs) // Set quan hệ ngược
                    .project(project)
                    .workDate(reqEntry.getWorkDate())
                    // Convert Double (DTO) -> BigDecimal (Entity) [cite: 663, 517]
                    .hoursWorked(BigDecimal.valueOf(reqEntry.getHoursWorked())) 
                    .description(reqEntry.getDescription())
                    .build();
            
            existingTs.getEntries().add(entry);
        }
        
        // Save (Cascade sẽ lưu cả entries)
        Timesheet savedTs = timesheetRepository.save(existingTs);

        return new TimesheetResponse(savedTs.getId(), "SUBMITTED", "Timesheet submitted successfully");
    }

    /**
     * Lock timesheets for financial closing
     */
    @Transactional
    public int lockTimesheets(int year, int month) {
        List<Timesheet> unlocked = timesheetRepository.findUnlockedTimesheets(year, month);
        
        for (Timesheet ts : unlocked) {
            ts.setIsLocked(true);
        }

        timesheetRepository.saveAll(unlocked);
        
        log.info("Locked {} timesheets for {}/{}", unlocked.size(), month, year);
        return unlocked.size();
    }
}