package com.company.rms.repository.allocation;

import com.company.rms.entity.allocation.ViewResourceAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ViewResourceAvailabilityRepository extends JpaRepository<ViewResourceAvailability, Long> {
    
    // Hàm tìm kiếm cơ bản (Giữ nguyên)
    @Query("SELECT v FROM ViewResourceAvailability v " +
           "WHERE v.availableCapacity > 0 " +
           "AND (:jobTitle IS NULL OR v.jobTitle LIKE %:jobTitle%) " +
           "AND (:levelName IS NULL OR v.levelName = :levelName) " +
           "AND (:minCapacity IS NULL OR v.availableCapacity >= :minCapacity) " +
           "ORDER BY v.availableCapacity DESC")
    List<ViewResourceAvailability> searchAvailableResources(
        @Param("jobTitle") String jobTitle,
        @Param("levelName") String levelName,
        @Param("minCapacity") BigDecimal minCapacity
    );

    // [FIX] Cập nhật hàm tìm theo Skill: Thêm điều kiện minCapacity
    @Query("SELECT DISTINCT v FROM ViewResourceAvailability v " +
           "JOIN Employee e ON e.id = v.employeeId " +
           "JOIN e.skills es " +
           "WHERE v.availableCapacity > 0 " +
           // Thêm dòng check này:
           "AND (:minCapacity IS NULL OR v.availableCapacity >= :minCapacity) " + 
           "AND es.skill.id IN :skillIds " +
           "AND es.level >= :minLevel " +
           "AND es.isVerified = true")
    List<ViewResourceAvailability> searchBySkills(
        @Param("skillIds") List<Integer> skillIds,
        @Param("minLevel") Byte minLevel,
        @Param("minCapacity") BigDecimal minCapacity // <-- Thêm tham số này
    );

    // [MỚI] Tính trung bình Utilization (currentLoad) của tất cả nhân viên
    @Query("SELECT AVG(v.currentLoad) FROM ViewResourceAvailability v")
    Double getAverageUtilization();

    // [MỚI] Đếm số nhân viên đang rảnh rỗi hoàn toàn (Bench)
    @Query("SELECT COUNT(v) FROM ViewResourceAvailability v WHERE v.availableCapacity = 100")
    Long countBenchResources();
    
    // [MỚI] Đếm tổng số nhân viên khả dụng trong View
    @Query("SELECT COUNT(v) FROM ViewResourceAvailability v")
    Long countTotalResources();
}