package com.company.rms.entity.masterdata;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Skill {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    
    @Column(length = 50)
    private String category;
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
