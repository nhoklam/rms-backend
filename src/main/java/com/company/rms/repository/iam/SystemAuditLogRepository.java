package com.company.rms.repository.iam;

import com.company.rms.entity.iam.SystemAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemAuditLogRepository extends JpaRepository<SystemAuditLog, Long> {
}