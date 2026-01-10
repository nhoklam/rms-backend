package com.company.rms.controller.api.v1;

import com.company.rms.dto.request.RateCardRequest;
import com.company.rms.dto.response.ApiResponse;
import com.company.rms.dto.response.RateCardResponse;
import com.company.rms.service.project.ProjectRateCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectRateCardController {

    private final ProjectRateCardService rateCardService;

    @GetMapping("/{projectId}/rate-cards")
    @PreAuthorize("hasAnyAuthority('ROLE_PM', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<RateCardResponse>>> getRateCards(@PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.success(rateCardService.getRateCardsByProject(projectId)));
    }

    @PostMapping("/{projectId}/rate-cards")
    @PreAuthorize("hasAnyAuthority('ROLE_PM', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<RateCardResponse>> createRateCard(
            @PathVariable Long projectId,
            @Valid @RequestBody RateCardRequest request) {
        request.setProjectId(projectId);
        return ResponseEntity.ok(ApiResponse.success(rateCardService.createRateCard(request)));
    }
    @PutMapping("/rate-cards/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_PM', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<RateCardResponse>> updateRateCard(
            @PathVariable Long id,
            @Valid @RequestBody RateCardRequest request) {
        return ResponseEntity.ok(ApiResponse.success(rateCardService.updateRateCard(id, request)));
    }
    @DeleteMapping("/rate-cards/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_PM', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRateCard(@PathVariable Long id) {
        rateCardService.deleteRateCard(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã xóa Rate Card"));
    }
}