package com.company.rms.entity.iam;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "system_audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemAuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_name", nullable = false, length = 50)
    private String entityName;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    @Type(JsonType.class)
    @Column(name = "old_value", columnDefinition = "json")
    // FIX: Thêm <String, Object>
    private Map<String, Object> oldValue;

    @Type(JsonType.class)
    @Column(name = "new_value", columnDefinition = "json")
    // FIX: Thêm <String, Object>
    private Map<String, Object> newValue;

    @Column(name = "changed_by")
    private Long changedBy;
    
    @Column(name = "changed_at")
    private LocalDateTime changedAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @PrePersist
    protected void onCreate() {
        changedAt = LocalDateTime.now();
    }
    
    public enum AuditAction {
        CREATE, UPDATE, DELETE
    }
}