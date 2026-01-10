package com.company.rms.service.project;

import com.company.rms.dto.request.RateCardRequest;
import com.company.rms.dto.response.RateCardResponse;
import com.company.rms.entity.masterdata.JobTitle;
import com.company.rms.entity.masterdata.Level;
import com.company.rms.entity.project.Project;
import com.company.rms.entity.project.ProjectRateCard;
import com.company.rms.exception.BusinessException;
import com.company.rms.repository.masterdata.JobTitleRepository;
import com.company.rms.repository.masterdata.LevelRepository;
import com.company.rms.repository.project.ProjectRateCardRepository;
import com.company.rms.repository.project.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectRateCardService {

    private final ProjectRateCardRepository rateCardRepository;
    // [FIX] Inject thêm các repo này để tránh lỗi Null Pointer và logic new Entity() sai
    private final ProjectRepository projectRepository;
    private final JobTitleRepository jobTitleRepository;
    private final LevelRepository levelRepository;

    @Transactional(readOnly = true)
    public List<RateCardResponse> getRateCardsByProject(Long projectId) {
        return rateCardRepository.findByProjectId(projectId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RateCardResponse createRateCard(RateCardRequest request) {
        log.info("Creating rate card for project {}", request.getProjectId());

        LocalDate endDate = request.getEffectiveEndDate() != null ?
                request.getEffectiveEndDate() : LocalDate.of(9999, 12, 31);

        List<ProjectRateCard> overlapping = rateCardRepository.findOverlappingRateCards(
            request.getProjectId(),
            request.getRoleId(),
            request.getLevelId(),
            request.getEffectiveStartDate(),
            endDate
        );

        if (!overlapping.isEmpty()) {
            throw new BusinessException("Trùng lặp thời gian (Time Overlap) cho Role/Level này.");
        }

        // [FIX] Phải findById để lấy entity thật sự từ DB
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new BusinessException("Project not found"));
        JobTitle role = jobTitleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new BusinessException("Role not found"));
        Level level = levelRepository.findById(request.getLevelId())
                .orElseThrow(() -> new BusinessException("Level not found"));

        ProjectRateCard rateCard = ProjectRateCard.builder()
                .project(project)
                .role(role)
                .level(level)
                .unitPrice(request.getUnitPrice())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .unitTime(request.getUnitTime() != null ? ProjectRateCard.UnitTime.valueOf(request.getUnitTime()) : ProjectRateCard.UnitTime.MONTHLY)
                .effectiveStartDate(request.getEffectiveStartDate())
                .effectiveEndDate(request.getEffectiveEndDate())
                .build();

        ProjectRateCard saved = rateCardRepository.save(rateCard);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public BigDecimal getEffectiveRate(Long projectId, Integer roleId, Integer levelId, LocalDate date) {
        return rateCardRepository.findEffectiveRateCard(projectId, roleId, levelId, date)
            .map(ProjectRateCard::getUnitPrice)
            .orElse(BigDecimal.ZERO);
    }

    private RateCardResponse mapToResponse(ProjectRateCard entity) {
        return RateCardResponse.builder()
                .id(entity.getId())
                .projectId(entity.getProject().getId())
                .roleId(entity.getRole().getId())
                .roleName(entity.getRole().getName())
                .levelId(entity.getLevel().getId())
                .levelName(entity.getLevel().getName())
                .unitPrice(entity.getUnitPrice())
                .currency(entity.getCurrency())
                .unitTime(entity.getUnitTime().name())
                .effectiveStartDate(entity.getEffectiveStartDate())
                .effectiveEndDate(entity.getEffectiveEndDate())
                .build();
    }
    @Transactional
    public RateCardResponse updateRateCard(Long id, RateCardRequest request) {
        ProjectRateCard rateCard = rateCardRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Rate card not found"));

        // Update các trường cho phép
        rateCard.setUnitPrice(request.getUnitPrice());
        if (request.getEffectiveEndDate() != null) {
            rateCard.setEffectiveEndDate(request.getEffectiveEndDate());
        }
        // Lưu ý: Không cho sửa StartDate dễ dàng vì ảnh hưởng logic overlap phức tạp. 
        // Nếu muốn đổi StartDate, tốt nhất là Xóa đi tạo lại.

        ProjectRateCard saved = rateCardRepository.save(rateCard);
        return mapToResponse(saved);
    }
    @Transactional
    public void deleteRateCard(Long id) {
        if (!rateCardRepository.existsById(id)) {
            throw new BusinessException("Rate card not found");
        }
        rateCardRepository.deleteById(id);
    }
}