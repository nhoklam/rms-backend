package com.company.rms.service.allocation;

import com.company.rms.dto.request.AllocationRequest;
import com.company.rms.dto.response.AllocationResponse;
import com.company.rms.entity.allocation.Allocation;
import com.company.rms.entity.hr.Employee;
import com.company.rms.entity.iam.User;
import com.company.rms.entity.project.Project;
import com.company.rms.exception.BusinessException;
import com.company.rms.exception.OptimisticLockException;
import com.company.rms.repository.allocation.AllocationRepository;
import com.company.rms.repository.hr.EmployeeRepository;
import com.company.rms.repository.project.ProjectRepository;
import com.company.rms.service.general.NotificationService; // [QUAN TRỌNG] Import Service thông báo

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

// [QUAN TRỌNG] Static imports cho Assertions và Mockito
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AllocationServiceTest {
    
    @Mock
    private AllocationRepository allocationRepository;

    @Mock
    private EmployeeRepository employeeRepository;
    
    @Mock
    private ProjectRepository projectRepository;

    // [FIX LỖI NPE] Phải mock NotificationService vì AllocationService có gọi nó
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AllocationService allocationService;
    
    @Test
    void createAllocation_Success() {
        // --- Arrange ---
        AllocationRequest request = AllocationRequest.builder()
            .employeeId(1L)
            .projectId(1L)
            .effortPercentage(50)
            .startDate(LocalDate.now().plusDays(1))
            .endDate(LocalDate.now().plusMonths(3))
            .version(0L)
            .roleInProject("Developer") // Thêm role
            .build();

        // 1. Mock User (để tránh NPE khi lấy user.getId() gửi thông báo)
        User user = new User();
        user.setId(100L);
        user.setFullName("Nguyen Van A"); 

        // 2. Mock Employee
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setEmployeeCode("EMP001");
        employee.setStatus(Employee.EmployeeStatus.OFFICIAL);
        employee.setVersion(0L);
        employee.setUser(user); // Gắn user vào employee
        
        // 3. Mock Project
        Project project = new Project();
        project.setId(1L);
        project.setName("Project Alpha"); 
        project.setCode("PRJ001");
        
        // 4. Define Mock Behavior
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        
        // Giả lập không có allocation trùng lặp
        when(allocationRepository.findOverlappingAllocations(any(), any(), any()))
            .thenReturn(Collections.emptyList());
        
        // Giả lập lưu thành công và trả về entity có ID
        when(allocationRepository.save(any(Allocation.class))).thenAnswer(invocation -> {
            Allocation savedAlloc = invocation.getArgument(0);
            savedAlloc.setId(123L); 
            return savedAlloc;
        });

        // --- Act ---
        AllocationResponse result = allocationService.createAllocation(request, 999L);

        // --- Assert ---
        assertNotNull(result);
        assertEquals(123L, result.getId());
        assertEquals("Nguyen Van A", result.getEmployeeName());
        assertEquals("Project Alpha", result.getProjectName());
        
        // Verify: Kiểm tra xem hàm lưu DB có được gọi không
        verify(allocationRepository).save(any(Allocation.class));
        
        // [QUAN TRỌNG] Verify: Kiểm tra xem hàm gửi thông báo có được gọi không
        // Logic service gọi: createNotification(user.getId(), ...) -> user id là 100L
        verify(notificationService, times(1)).createNotification(eq(100L), anyString(), anyString(), anyString());
    }
    
    @Test
    void createAllocation_OptimisticLockException() {
        // --- Arrange ---
        AllocationRequest request = AllocationRequest.builder()
            .employeeId(1L)
            .projectId(1L)
            .effortPercentage(50)
            .startDate(LocalDate.now().plusDays(1))
            .endDate(LocalDate.now().plusMonths(3))
            .version(0L) // Request version 0
            .build();

        Employee employee = new Employee();
        employee.setId(1L);
        employee.setStatus(Employee.EmployeeStatus.OFFICIAL);
        employee.setVersion(1L); // DB version 1 (Mismatch -> Lỗi)
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(new Project()));

        // --- Act & Assert ---
        assertThrows(OptimisticLockException.class, () -> {
            allocationService.createAllocation(request, 1L);
        });
        
        // Đảm bảo không gọi save hay notification khi lỗi
        verify(allocationRepository, never()).save(any());
        verify(notificationService, never()).createNotification(any(), any(), any(), any());
    }
    
    @Test
    void createAllocation_CapacityExceeded() {
        // --- Arrange ---
        // Request 60%
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

        // Đã có allocation 50%
        Allocation existing = new Allocation();
        existing.setEffortPercentage(50);
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        
        // Trả về list chứa allocation cũ -> Tổng 50 + 60 = 110 > 100 -> Lỗi
        when(allocationRepository.findOverlappingAllocations(any(), any(), any()))
            .thenReturn(Collections.singletonList(existing));

        // --- Act & Assert ---
        assertThrows(BusinessException.class, () -> {
            allocationService.createAllocation(request, 1L);
        });
        
        verify(allocationRepository, never()).save(any());
    }
}