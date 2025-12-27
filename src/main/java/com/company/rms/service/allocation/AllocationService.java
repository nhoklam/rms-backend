package com.company.rms.service.allocation;

import com.company.rms.dto.request.AllocationRequest;
import com.company.rms.dto.response.AllocationResponse;
import com.company.rms.entity.allocation.Allocation;
import com.company.rms.entity.hr.Employee;
import com.company.rms.entity.project.Project;
import com.company.rms.exception.BusinessException;
import com.company.rms.exception.OptimisticLockException;
import com.company.rms.repository.allocation.AllocationRepository;
import com.company.rms.repository.hr.EmployeeRepository;
import com.company.rms.repository.project.ProjectRepository; // Đã có thể import
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AllocationService {
    
    private final AllocationRepository allocationRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    
    /**
     * CORE: Create Booking with all validations
     */
    @Transactional
    public AllocationResponse createAllocation(AllocationRequest request, Long createdBy) {
        log.info("Creating allocation for employee {} to project {}", 
                 request.getEmployeeId(), request.getProjectId());

        // 1. Fetch entities with lock (FIX: Chỉ định rõ kiểu Employee và Project)
        Employee employee = employeeRepository.findById(request.getEmployeeId())
            .orElseThrow(() -> new BusinessException("Employee not found"));

        Project project = projectRepository.findById(request.getProjectId())
            .orElseThrow(() -> new BusinessException("Project not found"));

        // 2. Validate employee status
        if (employee.getStatus() != Employee.EmployeeStatus.OFFICIAL && 
            employee.getStatus() != Employee.EmployeeStatus.PROBATION) {
            throw new BusinessException("Employee is not available for allocation");
        }
        
        // 3. Check version for optimistic locking
        if (request.getVersion() != null && !request.getVersion().equals(employee.getVersion())) {
            throw new OptimisticLockException(
                "Dữ liệu nhân sự đã thay đổi. Vui lòng tải lại trang."
            );
        }
        
        // 4. Check capacity overlap (FIX: Thêm <Allocation>)
        List<Allocation> overlapping = allocationRepository.findOverlappingAllocations(
            request.getEmployeeId(),
            request.getStartDate(),
            request.getEndDate()
        );

        // FIX: mapToInt giờ sẽ hiểu Allocation::getEffortPercentage vì List đã có kiểu
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
        
        // 5. Create allocation
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
            // FIX: save trả về Allocation, không phải Object
            Allocation saved = allocationRepository.save(allocation);
            log.info("Allocation created successfully: {}", saved.getId());
            return mapToResponse(saved);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new OptimisticLockException(
                "Dữ liệu nhân sự đã thay đổi bởi người dùng khác. Vui lòng tải lại trang."
            );
        }
    }
    
    /**
     * Get current allocations of an employee
     */
    @Transactional(readOnly = true)
    // FIX: Thêm <AllocationResponse>
    public List<AllocationResponse> getCurrentAllocations(Long employeeId) {
        // FIX: Thêm <Allocation>
        List<Allocation> allocations = allocationRepository.findCurrentAllocations(employeeId);
        
        return allocations.stream()
            .map(this::mapToResponse) // Giờ map sẽ hiểu kiểu dữ liệu đầu vào
            .toList();
    }
    
    /**
     * Calculate total capacity for an employee in a period
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateCapacity(Long employeeId, 
                                       java.time.LocalDate startDate, 
                                       java.time.LocalDate endDate) {
        // FIX: Thêm <Allocation>
        List<Allocation> overlapping = allocationRepository.findOverlappingAllocations(
            employeeId, startDate, endDate
        );
        
        int totalEffort = overlapping.stream()
            .mapToInt(Allocation::getEffortPercentage)
            .sum();
            
        return BigDecimal.valueOf(100 - totalEffort);
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