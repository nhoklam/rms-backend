package com.company.rms.service.project;

import com.company.rms.dto.request.RateCardRequest; // Đã có thể import
import com.company.rms.entity.project.ProjectRateCard;
import com.company.rms.exception.BusinessException;
import com.company.rms.repository.project.ProjectRateCardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectRateCardService {

    private final ProjectRateCardRepository rateCardRepository;

    /**
     * Create rate card with overlap validation
     */
    @Transactional
    public ProjectRateCard createRateCard(RateCardRequest request) {
        log.info("Creating rate card for project {}", request.getProjectId());

        // Validate: Check time overlap
        LocalDate endDate = request.getEffectiveEndDate() != null ?
                request.getEffectiveEndDate() : LocalDate.of(9999, 12, 31);

        // FIX: Thêm <ProjectRateCard> để sửa lỗi Raw Type
        List<ProjectRateCard> overlapping = rateCardRepository.findOverlappingRateCards(
            request.getProjectId(),
            request.getRoleId(),
            request.getLevelId(),
            request.getEffectiveStartDate(),
            endDate
        );

        if (!overlapping.isEmpty()) {
            throw new BusinessException(
                "Rate card time overlap detected. Cannot have multiple rate cards " +
                "for same Role+Level+Project with overlapping date ranges."
            );
        }

        // Create entity (Mapping cơ bản)
        // Lưu ý: Trong thực tế bạn cần query Project/JobTitle/Level từ DB bằng ID để set vào
        ProjectRateCard rateCard = new ProjectRateCard();
        rateCard.setUnitPrice(request.getUnitPrice());
        rateCard.setEffectiveStartDate(request.getEffectiveStartDate());
        rateCard.setEffectiveEndDate(request.getEffectiveEndDate());
        rateCard.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        // ... set các field ID khác ...

        // FIX: save() giờ trả về ProjectRateCard nên hết lỗi Type mismatch
        return rateCardRepository.save(rateCard);
    }

    /**
     * Get effective rate at a specific date - for revenue calculation
     */
    @Transactional(readOnly = true)
    public BigDecimal getEffectiveRate(Long projectId, Integer roleId,
                                      Integer levelId, LocalDate date) {
        // FIX: findEffectiveRateCard trả về Optional<ProjectRateCard> nên gọi được getUnitPrice
        return rateCardRepository.findEffectiveRateCard(projectId, roleId, levelId, date)
            .map(ProjectRateCard::getUnitPrice)
            .orElse(BigDecimal.ZERO);
    }
}