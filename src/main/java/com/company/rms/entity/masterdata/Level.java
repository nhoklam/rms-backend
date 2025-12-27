package com.company.rms.entity.masterdata;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "levels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Level {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false, unique = true, length = 10)
    private String code;
    
    @Column(nullable = false, length = 50)
    private String name;
    
    @Column(name = "rank_order", nullable = false)
    private Integer rankOrder;
    
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
