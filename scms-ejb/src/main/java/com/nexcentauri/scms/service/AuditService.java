package com.nexcentauri.scms.service;

import com.nexcentauri.scms.entity.AuditLog;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDateTime;

@Stateless
public class AuditService {

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager entityManager;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void logAction(String action, String methodName, String performedBy){
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setMethodName(methodName);
        log.setTimestamp(LocalDateTime.now());
        log.setPerformedBy(performedBy != null ? performedBy : "SYSTEM");

        entityManager.persist(log);
    }

}
