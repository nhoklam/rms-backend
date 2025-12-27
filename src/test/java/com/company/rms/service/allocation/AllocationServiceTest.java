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
import com.company.rms.repository.project.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.company.rms.entity.iam.User;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

// Import static quan trọng để sửa lỗi "method undefined"
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AllocationServiceTest {
    
    @Mock
    private AllocationRepository allocationRepository;

    @Mock
    private EmployeeRepository employeeRepository;
    
    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private AllocationService allocationService;
    
    @Test
    void createAllocation_Success() {
        // Arrange
        AllocationRequest request = AllocationRequest.builder()
            .employeeId(1L)
            .projectId(1L)
            .effortPercentage(50)
            .startDate(LocalDate.now().plusDays(1))
            .endDate(LocalDate.now().plusMonths(3))
            .version(0L)
            .build();
        User user = new User();
        user.setId(100L);
        user.setFullName("Test Employee Name"); // Quan trọng: set tên để tránh lỗi null tiếp theo

        Employee employee = new Employee();
        employee.setId(1L);
        employee.setEmployeeCode("EMP001");
        employee.setStatus(Employee.EmployeeStatus.OFFICIAL);
        employee.setVersion(0L);
        employee.setUser(user);
        
        Project project = new Project();
        project.setId(1L);
        project.setName("Test Project"); // Thêm tên project cho chắc
        project.setCode("PRJ001");       // Thêm mã project
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(allocationRepository.findOverlappingAllocations(any(), any(), any()))
            .thenReturn(Collections.emptyList());
        when(allocationRepository.save(any(Allocation.class))).thenAnswer(i -> {
            Allocation savedAlloc = i.getArgument(0);
            savedAlloc.setId(123L); // Giả lập ID DB tạo ra
            return savedAlloc;
        });

        // Act
        AllocationResponse result = allocationService.createAllocation(request, 1L);

        // Assert
        assertNotNull(result);
        assertEquals("Test Employee Name", result.getEmployeeName());
        verify(allocationRepository).save(any(Allocation.class));
    }
    
    @Test
    void createAllocation_OptimisticLockException() {
        // Arrange
        AllocationRequest request = AllocationRequest.builder()
            .employeeId(1L)
            .projectId(1L)
            .effortPercentage(50)
            .startDate(LocalDate.now().plusDays(1))
            .endDate(LocalDate.now().plusMonths(3))
            .version(0L)
            .build();

        Employee employee = new Employee();
        employee.setId(1L);
        employee.setStatus(Employee.EmployeeStatus.OFFICIAL);
        employee.setVersion(1L); // Version mismatch
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(new Project())); // Mock project để qua bước check project

        // Act & Assert
        assertThrows(OptimisticLockException.class, () -> {
            allocationService.createAllocation(request, 1L);
        });
    }
    
    @Test
    void createAllocation_CapacityExceeded() {
        // Arrange
        AllocationRequest request = AllocationRequest.builder()
            .employeeId(1L)
            .projectId(1L)
            .effortPercentage(60)
            .startDate(LocalDate.now().plusDays(1))
            .endDate(LocalDate.now().plusMonths(3))
            .version(0L)
            .build();

        Employee employee = new Employee();
        employee.setId(1L);
        employee.setStatus(Employee.EmployeeStatus.OFFICIAL);
        employee.setVersion(0L);
        
        Project project = new Project();
        project.setId(1L);

        // Existing allocation: 50%
        Allocation existing = new Allocation();
        existing.setEffortPercentage(50);
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(allocationRepository.findOverlappingAllocations(any(), any(), any()))
            .thenReturn(Collections.singletonList(existing));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            allocationService.createAllocation(request, 1L);
        });
    }
}