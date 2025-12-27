package com.company.rms.entity.masterdata;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "job_titles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobTitle {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String name;
    
    @Column(length = 20)
    private String code;
    
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}



