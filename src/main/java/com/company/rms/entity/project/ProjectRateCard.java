package com.company.rms.entity.project;

import com.company.rms.entity.masterdata.JobTitle;
import com.company.rms.entity.masterdata.Level;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "project_rate_cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectRateCard {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private JobTitle role;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id")
    private Level level;
    
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "unit_time")
    @Builder.Default
    private UnitTime unitTime = UnitTime.MONTHLY;
    
    @Column(length = 10)
    @Builder.Default
    private String currency = "USD";
    
    @Column(name = "effective_start_date", nullable = false)
    private LocalDate effectiveStartDate;
    
    @Column(name = "effective_end_date")
    private LocalDate effectiveEndDate;
    
    public enum UnitTime {
        HOURLY, MONTHLY, DAILY
    }
}   