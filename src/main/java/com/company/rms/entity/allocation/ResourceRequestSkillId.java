package com.company.rms.entity.allocation;

import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ResourceRequestSkillId implements Serializable {
    private Long request;
    private Integer skill;
}