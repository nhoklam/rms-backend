package com.company.rms.repository.project;

import com.company.rms.entity.project.ProjectRateCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRateCardRepository extends JpaRepository<ProjectRateCard, Long> {

    // [MỚI] Hàm này thiếu trong code cũ của bạn
    @Query("SELECT r FROM ProjectRateCard r WHERE r.project.id = :projectId ORDER BY r.effectiveStartDate DESC")
    List<ProjectRateCard> findByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT r FROM ProjectRateCard r " +
           "WHERE r.project.id = :projectId " +
           "AND r.role.id = :roleId " +
           "AND r.level.id = :levelId " +
           "AND r.effectiveStartDate <= :endDate " +
           "AND (r.effectiveEndDate IS NULL OR r.effectiveEndDate >= :startDate)")
    List<ProjectRateCard> findOverlappingRateCards(
        @Param("projectId") Long projectId,
        @Param("roleId") Integer roleId,
        @Param("levelId") Integer levelId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT r FROM ProjectRateCard r " +
           "WHERE r.project.id = :projectId " +
           "AND r.role.id = :roleId " +
           "AND r.level.id = :levelId " +
           "AND r.effectiveStartDate <= :date " +
           "AND (r.effectiveEndDate IS NULL OR r.effectiveEndDate >= :date)")
    Optional<ProjectRateCard> findEffectiveRateCard(
        @Param("projectId") Long projectId,
        @Param("roleId") Integer roleId,
        @Param("levelId") Integer levelId,
        @Param("date") LocalDate date
    );
}