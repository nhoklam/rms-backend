package com.company.rms.controller.api.v1;

import com.company.rms.dto.request.ResourceSearchRequest;
import com.company.rms.dto.response.ApiResponse;
import com.company.rms.dto.response.ResourceAvailabilityResponse;
import com.company.rms.service.resource.ResourceSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
@Slf4j
public class ResourceController {

    private final ResourceSearchService resourceSearchService;

    /**
     * POST /api/v1/resources/search - Smart search available resources
     * Cho phép RM, PM, ADMIN và cả EMP (để hiển thị số liệu Dashboard)
     * Lưu ý: required = false để xử lý trường hợp load trang lần đầu không gửi body
     */
    @PostMapping("/search")
    @PreAuthorize("hasAnyAuthority('ROLE_RM', 'ROLE_PM', 'ROLE_ADMIN', 'ROLE_EMP')")
    public ResponseEntity<ApiResponse<List<ResourceAvailabilityResponse>>> searchResources(
            @Valid @RequestBody(required = false) ResourceSearchRequest request) {
        
        // Nếu request null (frontend không gửi gì), khởi tạo object rỗng để tìm tất cả
        if (request == null) {
            request = new ResourceSearchRequest();
        }

        log.info("Searching resources with criteria: {}", request);
        
        List<ResourceAvailabilityResponse> results = 
            resourceSearchService.searchAvailableResources(request);
            
        return ResponseEntity.ok(ApiResponse.success(results, 
            String.format("Found %d available resources", results.size())));
    }

    /**
     * GET /api/v1/resources/{id}/availability - Get resource availability detail
     */
    @GetMapping("/{employeeId}/availability")
    @PreAuthorize("hasAnyAuthority('ROLE_RM', 'ROLE_PM', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ResourceAvailabilityResponse>> getResourceAvailability(
            @PathVariable Long employeeId) {
        
        ResourceAvailabilityResponse response = 
            resourceSearchService.getResourceAvailability(employeeId);
            
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}