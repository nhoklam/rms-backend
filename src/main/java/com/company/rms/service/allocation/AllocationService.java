package com.company.rms.service.allocation;

import com.company.rms.dto.request.AllocationRequest;
import com.company.rms.dto.request.AllocationUpdateRequest; // [Import DTO vừa tạo]
import com.company.rms.dto.response.AllocationResponse;
import com.company.rms.entity.allocation.Allocation;
import com.company.rms.entity.hr.Employee;
import com.company.rms.entity.project.Project;
import com.company.rms.exception.BusinessException;
import com.company.rms.exception.OptimisticLockException;
import com.company.rms.repository.allocation.AllocationRepository;
import com.company.rms.repository.hr.EmployeeRepository;
import com.company.rms.repository.project.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AllocationService {
    
    private final AllocationRepository allocationRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    
    @Transactional
    public AllocationResponse createAllocation(AllocationRequest request, Long createdBy) {
        log.info("Creating allocation for employee {} to project {}", 
                 request.getEmployeeId(), request.getProjectId());

        Employee employee = employeeRepository.findById(request.getEmployeeId())
            .orElseThrow(() -> new BusinessException("Employee not found"));

        Project project = projectRepository.findById(request.getProjectId())
            .orElseThrow(() -> new BusinessException("Project not found"));

        if (employee.getStatus() != Employee.EmployeeStatus.OFFICIAL && 
            employee.getStatus() != Employee.EmployeeStatus.PROBATION) {
            throw new BusinessException("Employee is not available for allocation");
        }
        
        if (request.getVersion() != null && !request.getVersion().equals(employee.getVersion())) {
            throw new OptimisticLockException("Dữ liệu nhân sự đã thay đổi. Vui lòng tải lại trang.");
        }
        
        List<Allocation> overlapping = allocationRepository.findOverlappingAllocations(
            request.getEmployeeId(),
            request.getStartDate(),
            request.getEndDate()
        );

        int totalEffort = overlapping.stream()
            .mapToInt(Allocation::getEffortPercentage)
            .sum() + request.getEffortPercentage();

        if (totalEffort > 100) {
            throw new BusinessException(
                String.format("Capacity exceeded. Current: %d%%, Requested: %d%%, Total: %d%%",
                    totalEffort - request.getEffortPercentage(),
                    request.getEffortPercentage(),
                    totalEffort)
            );
        }
        
        Allocation allocation = Allocation.builder()
            .project(project)
            .employee(employee)
            .roleInProject(request.getRoleInProject())
            .effortPercentage(request.getEffortPercentage())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .isShadow(request.getIsShadow() != null ? request.getIsShadow() : false)
            .overrideBillRate(request.getOverrideBillRate())
            .status(Allocation.AllocationStatus.ACTIVE)
            .createdBy(createdBy)
            .build();

        try {
            Allocation saved = allocationRepository.save(allocation);
            log.info("Allocation created successfully: {}", saved.getId());
            return mapToResponse(saved);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new OptimisticLockException("Dữ liệu nhân sự đã thay đổi bởi người dùng khác. Vui lòng tải lại trang.");
        }
    }
    
    @Transactional(readOnly = true)
    public List<AllocationResponse> getCurrentAllocations(Long employeeId) {
        List<Allocation> allocations = allocationRepository.findCurrentAllocations(employeeId);
        return allocations.stream().map(this::mapToResponse).toList();
    }
    
    @Transactional(readOnly = true)
    public BigDecimal calculateCapacity(Long employeeId, LocalDate startDate, LocalDate endDate) {
        List<Allocation> overlapping = allocationRepository.findOverlappingAllocations(employeeId, startDate, endDate);
        int totalEffort = overlapping.stream().mapToInt(Allocation::getEffortPercentage).sum();
        return BigDecimal.valueOf(100 - totalEffort);
    }

    @Transactional(readOnly = true)
    public List<AllocationResponse> searchAllocations(Long projectId, Long employeeId, String status) {
        Allocation.AllocationStatus allocStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                allocStatus = Allocation.AllocationStatus.valueOf(status);
            } catch (IllegalArgumentException e) { /* Ignore */ }
        }

        return allocationRepository.searchAllocations(projectId, employeeId, allocStatus)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // [FIX] Thêm method updateAllocation bị thiếu
    @Transactional
    public AllocationResponse updateAllocation(Long id, AllocationUpdateRequest request) {
        Allocation allocation = allocationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Allocation not found"));

        // 1. Check Optimistic Locking
        if (request.getVersion() != null && !request.getVersion().equals(allocation.getVersion())) {
            throw new OptimisticLockException("Dữ liệu đã bị thay đổi bởi người khác. Vui lòng tải lại.");
        }

        // 2. Update logic
        if (request.getEffortPercentage() != null) {
            allocation.setEffortPercentage(request.getEffortPercentage());
        }

        if (request.getEndDate() != null) {
            if (request.getEndDate().isBefore(allocation.getStartDate())) {
                throw new BusinessException("End date cannot be before Start date");
            }
            allocation.setEndDate(request.getEndDate());
        }

        Allocation saved = allocationRepository.save(allocation);
        return mapToResponse(saved);
    }

    @Transactional
    public void terminateAllocation(Long id) {
        Allocation allocation = allocationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Allocation not found"));
        
        allocation.setEndDate(LocalDate.now()); 
        allocation.setStatus(Allocation.AllocationStatus.TERMINATED);
        allocationRepository.save(allocation);
    }
    @Transactional(readOnly = true)
    public List<AllocationResponse> getAllocationsForTimesheet(Long employeeId, LocalDate fromDate, LocalDate toDate) {
        // Nếu không truyền ngày, mặc định lấy tuần hiện tại
        LocalDate start = (fromDate != null) ? fromDate : LocalDate.now();
        LocalDate end = (toDate != null) ? toDate : LocalDate.now();

        List<Allocation> allocations = allocationRepository.findAllocationsByRange(employeeId, start, end);
        return allocations.stream().map(this::mapToResponse).toList();
    }
    
    private AllocationResponse mapToResponse(Allocation allocation) {
        return AllocationResponse.builder()
            .id(allocation.getId())
            .projectId(allocation.getProject().getId())
            .projectCode(allocation.getProject().getCode())
            .projectName(allocation.getProject().getName())
            .employeeId(allocation.getEmployee().getId())
            .employeeCode(allocation.getEmployee().getEmployeeCode())
            .employeeName(allocation.getEmployee().getUser().getFullName())
            .roleInProject(allocation.getRoleInProject())
            .effortPercentage(allocation.getEffortPercentage())
            .startDate(allocation.getStartDate())
            .endDate(allocation.getEndDate())
            .isShadow(allocation.getIsShadow())
            .status(allocation.getStatus().name())
            .version(allocation.getVersion())
            .build();
    }
}