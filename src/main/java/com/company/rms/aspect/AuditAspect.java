package com.company.rms.aspect;

import com.company.rms.entity.iam.SystemAuditLog;
import com.company.rms.repository.iam.SystemAuditLogRepository;
import com.company.rms.security.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.proxy.HibernateProxy; // [QUAN TRỌNG]
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final SystemAuditLogRepository auditLogRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    @Around("execution(* com.company.rms.repository..*.save(..)) && args(entity)")
    public Object auditSave(ProceedingJoinPoint joinPoint, Object entity) throws Throwable {
        if (!isAuditableEntity(entity)) {
            return joinPoint.proceed();
        }

        Map<String, Object> oldValue = null;
        boolean isUpdate = false;
        Long entityId = getEntityId(entity);

        // 1. Lấy dữ liệu cũ (Old Value)
        if (entityId != null && entityId > 0) {
            try {
                Object existingEntity = entityManager.find(entity.getClass(), entityId);
                if (existingEntity != null) {
                    isUpdate = true;
                    oldValue = entityToMap(existingEntity);
                    entityManager.detach(existingEntity); // Detach để không ảnh hưởng luồng chính
                }
            } catch (Exception e) {
                log.warn("Could not retrieve old value for audit: {}", e.getMessage());
            }
        }

        // 2. Thực hiện Save chính
        Object result = joinPoint.proceed();

        // 3. Ghi Log (New Value)
        try {
            SystemAuditLog.AuditAction action = isUpdate ?
                    SystemAuditLog.AuditAction.UPDATE : SystemAuditLog.AuditAction.CREATE;

            Long resultId = getEntityId(result);
            Map<String, Object> newValue = entityToMap(result);

            SystemAuditLog auditLog = SystemAuditLog.builder()
                    .entityName(entity.getClass().getSimpleName())
                    .entityId(resultId != null ? resultId : 0L)
                    .action(action)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .changedBy(SecurityUtils.getCurrentUserId())
                    .ipAddress(getClientIp())
                    .build();
            
            auditLogRepository.save(auditLog);
            
        } catch (Exception e) {
            log.error("Failed to create audit log", e);
        }

        return result;
    }

    @Around("execution(* com.company.rms.repository..*.delete*(..)) && args(entity)")
    public Object auditDelete(ProceedingJoinPoint joinPoint, Object entity) throws Throwable {
        if (!isAuditableEntity(entity)) {
            return joinPoint.proceed();
        }

        Map<String, Object> oldValue = entityToMap(entity);
        Long entityId = getEntityId(entity);

        Object result = joinPoint.proceed();

        try {
            SystemAuditLog auditLog = SystemAuditLog.builder()
                    .entityName(entity.getClass().getSimpleName())
                    .entityId(entityId != null ? entityId : 0L)
                    .action(SystemAuditLog.AuditAction.DELETE)
                    .oldValue(oldValue)
                    .newValue(null)
                    .changedBy(SecurityUtils.getCurrentUserId())
                    .ipAddress(getClientIp())
                    .build();
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to create audit log for delete", e);
        }

        return result;
    }

    // --- Helper Methods ---

    private boolean isAuditableEntity(Object entity) {
        return entity != null && entity.getClass().isAnnotationPresent(Entity.class) &&
                (entity.getClass().getSimpleName().equals("Allocation") ||
                 entity.getClass().getSimpleName().equals("Employee") ||
                 entity.getClass().getSimpleName().equals("ProjectRateCard") ||
                 entity.getClass().getSimpleName().equals("Department") ||
                 entity.getClass().getSimpleName().equals("JobTitle"));
    }

    private Long getEntityId(Object entity) {
        if (entity == null) return null;
        
        // [FIX] Xử lý Hibernate Proxy: Lấy ID trực tiếp không cần initialize
        if (entity instanceof HibernateProxy) {
            Object id = ((HibernateProxy) entity).getHibernateLazyInitializer().getIdentifier();
            if (id instanceof Long) return (Long) id;
            if (id instanceof Integer) return ((Integer) id).longValue();
            return null; 
        }

        try {
            Class<?> clazz = entity.getClass();
            while (clazz != null) {
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Id.class)) {
                        field.setAccessible(true);
                        Object value = field.get(entity);
                        return value != null ? ((Number) value).longValue() : null;
                    }
                }
                clazz = clazz.getSuperclass();
            }
        } catch (Exception e) {
            log.error("Failed to get entity ID", e);
        }
        return null;
    }

    private Map<String, Object> entityToMap(Object entity) {
        Map<String, Object> map = new HashMap<>();
        if (entity == null) return map;

        try {
            // [FIX] Lấy class thực sự (kể cả khi là Proxy)
            Class<?> clazz = (entity instanceof HibernateProxy) 
                           ? ((HibernateProxy) entity).getHibernateLazyInitializer().getPersistentClass() 
                           : entity.getClass();

            while (clazz != null && !clazz.equals(Object.class)) {
                for (Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    if (Modifier.isStatic(field.getModifiers())) continue;

                    String fieldName = field.getName();
                    if (fieldName.toLowerCase().contains("password") || 
                        fieldName.toLowerCase().contains("hash") || 
                        fieldName.toLowerCase().contains("token")) {
                        continue;
                    }
                    
                    Object value = field.get(entity);
                    if (value instanceof Collection) {
                        continue;
                    }

                    // [FIX] Kiểm tra xem value có phải là Entity/Proxy không
                    boolean isEntity = false;
                    if (value != null) {
                        if (value instanceof HibernateProxy) {
                            isEntity = true; 
                        } else {
                            isEntity = value.getClass().isAnnotationPresent(Entity.class);
                        }
                    }

                    if (isEntity) {
                        // Nếu là Entity liên kết, chỉ lấy ID để tránh Lazy Init Error
                        Long relatedId = getEntityId(value);
                        map.put(fieldName + "Id", relatedId);
                    } else {
                        map.put(fieldName, value);
                    }
                }
                clazz = clazz.getSuperclass();
            }
        } catch (Exception e) {
            log.error("Failed to convert entity to map manually", e);
        }
        return map;
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                return ip != null ? ip : request.getRemoteAddr();
            }
        } catch (Exception e) {
            // Ignore
        }
        return "Unknown";
    }
}