package com.company.rms.entity.allocation;

import com.company.rms.entity.masterdata.Skill;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resource_request_skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(ResourceRequestSkillId.class)
public class ResourceRequestSkill {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private ResourceRequest request;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(name = "min_level")
    private Byte minLevel;

    @Column(name = "is_mandatory")
    @Builder.Default
    private Boolean isMandatory = true;
}