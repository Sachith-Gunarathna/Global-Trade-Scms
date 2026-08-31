package com.nexcentauri.scms.service;

import com.nexcentauri.scms.entity.AuditLog;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.Dependent;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
@Dependent
public class AuditService {
    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager entityManager;
    @Resource
    private SessionContext sessionContext;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void logAction(String component, String method, String action, long durationMs, boolean success, String detail) {
        AuditLog log = new AuditLog();
        log.setComponentName(component);
        log.setMethodName(method);
        log.setAction(action);
        log.setTimestamp(LocalDateTime.now());
        log.setPerformedBy(currentCaller());
        log.setDurationMs(durationMs);
        log.setSuccess(success);
        log.setDetail(trim(detail, 500));
        entityManager.persist(log);
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<AuditLog> recent(int limit) {
        return entityManager.createQuery("SELECT a FROM AuditLog a ORDER BY a.timestamp DESC", AuditLog.class)
                .setMaxResults(Math.max(1, Math.min(limit, 100)))
                .getResultList();
    }

    private String currentCaller() {
        try {
            if (sessionContext.getCallerPrincipal() != null && sessionContext.getCallerPrincipal().getName() != null) {
                String name = sessionContext.getCallerPrincipal().getName();
                if (!"anonymous".equalsIgnoreCase(name)) {
                    return name;
                }
            }
        } catch (Exception ignored) {
            return "SYSTEM";
        }
        return "SYSTEM";
    }

    private String trim(String value, int max) {
        if (value == null) return null;
        return value.length() <= max ? value : value.substring(0, max);
    }
}
