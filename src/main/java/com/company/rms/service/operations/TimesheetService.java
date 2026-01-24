package com.company.rms.service.operations;

import com.company.rms.dto.request.TimesheetSubmitRequest;
import com.company.rms.dto.response.TimesheetResponse;
import com.company.rms.entity.allocation.Allocation;
import com.company.rms.entity.operations.Timesheet;
import com.company.rms.entity.operations.TimesheetEntry;
import com.company.rms.entity.project.Project;
import com.company.rms.entity.hr.Employee;
import com.company.rms.entity.iam.User;
import com.company.rms.exception.BusinessException;
import com.company.rms.repository.allocation.AllocationRepository;
import com.company.rms.repository.hr.EmployeeRepository;
import com.company.rms.repository.iam.UserRepository;
import com.company.rms.repository.operations.TimesheetRepository;
import com.company.rms.repository.project.ProjectRepository;
import com.company.rms.service.general.NotificationService; // [1. Import Service Thông báo]
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimesheetService {

    private final TimesheetRepository timesheetRepository;
    private final AllocationRepository allocationRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService; // [2. Inject Service Thông báo]

    @Transactional
    public TimesheetResponse submitTimesheet(TimesheetSubmitRequest request, Long employeeId) {
        if (employeeId == null) {
            throw new BusinessException("Employee ID not found for current user");
        }

        Timesheet existingTs = timesheetRepository
            .findByEmployeeIdAndPeriodStart(employeeId, request.getPeriodStart())
            .orElse(null);

        if (existingTs != null && Boolean.TRUE.equals(existingTs.getIsLocked())) {
            throw new BusinessException("Timesheet is locked. Cannot modify.");
        }

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
                    String.format("No active allocation found for project %d on date %s", projectId, workDate)
                );
            }
        }

        if (existingTs == null) {
            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new BusinessException("Employee not found"));

            existingTs = Timesheet.builder()
                    .employee(employee)
                    .periodStart(request.getPeriodStart())
                    .periodEnd(request.getPeriodStart().plusDays(6))
                    .status(Timesheet.TimesheetStatus.SUBMITTED)
                    .isLocked(false)
                    .entries(new ArrayList<>())
                    .build();
        } else {
            existingTs.getEntries().clear();
            existingTs.setStatus(Timesheet.TimesheetStatus.SUBMITTED);
        }

        for (var reqEntry : request.getEntries()) {
            Project project = projectRepository.findById(reqEntry.getProjectId())
                    .orElseThrow(() -> new BusinessException("Project not found"));

            TimesheetEntry entry = TimesheetEntry.builder()
                    .timesheet(existingTs)
                    .project(project)
                    .workDate(reqEntry.getWorkDate())
                    .hoursWorked(BigDecimal.valueOf(reqEntry.getHoursWorked()))
                    .description(reqEntry.getDescription())
                    .build();
            
            existingTs.getEntries().add(entry);
        }
        
        Timesheet savedTs = timesheetRepository.save(existingTs);
        return mapToResponse(savedTs);
    }

    @Transactional(readOnly = true)
    public List<TimesheetResponse> getPendingApprovals(Long userId) {
        if (userId == null) return new ArrayList<>();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));

        List<Timesheet> timesheets;

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));

        if (isAdmin) {
            timesheets = timesheetRepository.findAllPendingTimesheets();
        } else {
            timesheets = timesheetRepository.findPendingApprovalByPm(userId);
        }

        return timesheets.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void approveTimesheet(Long timesheetId, Long approverId) {
        Timesheet ts = timesheetRepository.findById(timesheetId)
                .orElseThrow(() -> new BusinessException("Timesheet not found"));

        if (ts.getStatus() != Timesheet.TimesheetStatus.SUBMITTED) {
            throw new BusinessException("Only SUBMITTED timesheets can be approved");
        }

        if (ts.getEmployee().getUser().getId().equals(approverId)) {
            throw new BusinessException("Bạn không thể tự duyệt Timesheet của chính mình.");
        }

        // Cập nhật trạng thái
        ts.setStatus(Timesheet.TimesheetStatus.APPROVED);
        ts.setApproverId(approverId);
        ts.setApprovedAt(LocalDateTime.now());
        timesheetRepository.save(ts);

        // --- [3. GỬI THÔNG BÁO CHO NHÂN VIÊN] ---
        if (ts.getEmployee().getUser() != null) {
            notificationService.createNotification(
                ts.getEmployee().getUser().getId(),
                "Timesheet đã được duyệt",
                "Timesheet tuần " + ts.getPeriodStart() + " của bạn đã được chấp thuận.",
                "SUCCESS"
            );
        }
    }

    @Transactional
    public void rejectTimesheet(Long timesheetId, Long approverId) {
        Timesheet ts = timesheetRepository.findById(timesheetId)
                .orElseThrow(() -> new BusinessException("Timesheet not found"));

        if (ts.getStatus() != Timesheet.TimesheetStatus.SUBMITTED) {
            throw new BusinessException("Only SUBMITTED timesheets can be rejected");
        }

        // Cập nhật trạng thái
        ts.setStatus(Timesheet.TimesheetStatus.REJECTED);
        ts.setApproverId(approverId);
        ts.setApprovedAt(LocalDateTime.now());
        timesheetRepository.save(ts);

        // --- [3. GỬI THÔNG BÁO CHO NHÂN VIÊN] ---
        if (ts.getEmployee().getUser() != null) {
            notificationService.createNotification(
                ts.getEmployee().getUser().getId(),
                "Timesheet bị từ chối",
                "Timesheet tuần " + ts.getPeriodStart() + " cần chỉnh sửa lại.",
                "ERROR"
            );
        }
    }

    @Transactional
    public int lockTimesheets(int year, int month) {
        List<Timesheet> unlocked = timesheetRepository.findUnlockedTimesheets(year, month);
        for (Timesheet ts : unlocked) {
            ts.setIsLocked(true);
        }
        timesheetRepository.saveAll(unlocked);
        return unlocked.size();
    }

    private TimesheetResponse mapToResponse(Timesheet ts) {
        BigDecimal totalHours = ts.getEntries().stream()
                .map(TimesheetEntry::getHoursWorked)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<TimesheetResponse.EntryDetail> details = ts.getEntries().stream()
                .map(e -> TimesheetResponse.EntryDetail.builder()
                        .projectName(e.getProject().getName())
                        .workDate(e.getWorkDate())
                        .hours(e.getHoursWorked())
                        .description(e.getDescription())
                        .build())
                .collect(Collectors.toList());

        return TimesheetResponse.builder()
                .id(ts.getId())
                .status(ts.getStatus().name())
                .employeeId(ts.getEmployee().getId())
                .employeeName(ts.getEmployee().getUser().getFullName())
                .periodStart(ts.getPeriodStart())
                .periodEnd(ts.getPeriodEnd())
                .totalHours(totalHours)
                .details(details)
                .message("Success")
                .build();
    }
}