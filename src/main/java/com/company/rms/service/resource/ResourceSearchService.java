package com.company.rms.service.resource;

import com.company.rms.dto.request.ResourceSearchRequest;
import com.company.rms.dto.response.ResourceAvailabilityResponse;
import com.company.rms.entity.allocation.ViewResourceAvailability;
import com.company.rms.repository.allocation.ViewResourceAvailabilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceSearchService {
    
    private final ViewResourceAvailabilityRepository availabilityRepository;

    @Transactional(readOnly = true)
    public List<ResourceAvailabilityResponse> searchAvailableResources(ResourceSearchRequest request) {
        log.info("Searching resources with filters: {}", request);
        List<ViewResourceAvailability> results;
        
        // Lấy giá trị minCapacity từ request, nếu null thì mặc định là 0
        BigDecimal minCapacity = request.getMinCapacity() != null ? request.getMinCapacity() : BigDecimal.ZERO;

        if (request.getSkillIds() != null && !request.getSkillIds().isEmpty()) {
            // [FIX] Truyền thêm minCapacity vào hàm searchBySkills
            results = availabilityRepository.searchBySkills(
                request.getSkillIds(),
                request.getMinSkillLevel() != null ? request.getMinSkillLevel() : (byte) 1,
                minCapacity // <-- Truyền vào đây
            );
        } else {
            // Search by basic criteria
            results = availabilityRepository.searchAvailableResources(
                request.getJobTitle(),
                request.getLevelName(),
                minCapacity
            );
        }
        
        return results.stream()
            .map(this::mapToResponse)
            .toList();
    }
    
    // ... (Giữ nguyên các phần còn lại: getResourceAvailability, mapToResponse)
    @Transactional(readOnly = true)
    public ResourceAvailabilityResponse getResourceAvailability(Long employeeId) {
        ViewResourceAvailability view = availabilityRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));
        return mapToResponse(view);
    }
    
    private ResourceAvailabilityResponse mapToResponse(ViewResourceAvailability view) {
        return ResourceAvailabilityResponse.builder()
            .employeeId(view.getEmployeeId())
            .employeeCode(view.getEmployeeCode())
            .fullName(view.getFullName())
            .jobTitle(view.getJobTitle())
            .levelName(view.getLevelName())
            .skillsList(view.getSkillsList())
            .currentLoad(view.getCurrentLoad())
            .availableCapacity(view.getAvailableCapacity())
            .version(view.getVersion() != null ? view.getVersion() : 0L)
            .build();
    }
}