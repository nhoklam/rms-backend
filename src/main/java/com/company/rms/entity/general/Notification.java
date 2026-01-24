package com.company.rms.entity.general;

import com.company.rms.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_read")
    @Builder.Default
    private Boolean isRead = false;

    private String type; // INFO, SUCCESS, WARNING, ERROR
    
    @Column(name = "related_entity_id")
    private Long relatedEntityId;
    
    @Column(name = "related_entity_type")
    private String relatedEntityType; 
}