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

    /**
     * Smart Search - Tìm available resources với filters
     */
    @Transactional(readOnly = true)
    public List<ResourceAvailabilityResponse> searchAvailableResources(ResourceSearchRequest request) {
        log.info("Searching resources with filters: {}", request);
        
        List<ViewResourceAvailability> results;
        
        if (request.getSkillIds() != null && !request.getSkillIds().isEmpty()) {
            // Search by skills
            results = availabilityRepository.searchBySkills(
                request.getSkillIds(),
                request.getMinSkillLevel() != null ? request.getMinSkillLevel() : (byte) 1
            );
        } else {
            // Search by basic criteria
            results = availabilityRepository.searchAvailableResources(
                request.getJobTitle(),
                request.getLevelName(),
                request.getMinCapacity() != null ? request.getMinCapacity() : BigDecimal.ZERO
            );
        }
        
        return results.stream()
            .map(this::mapToResponse)
            .toList();
    }
    
    /**
     * Get resource availability by employee ID
     */
    @Transactional(readOnly = true)
    public ResourceAvailabilityResponse getResourceAvailability(Long employeeId) {
        ViewResourceAvailability view = availabilityRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));
        return mapToResponse(view);
    }
    
    // Helper method: Map Entity -> DTO
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
            // [FIX] Map version từ View (Entity) sang Response (DTO)
            // Nếu null thì trả về 0
            .version(view.getVersion() != null ? view.getVersion() : 0L)
            .build();
    }
}