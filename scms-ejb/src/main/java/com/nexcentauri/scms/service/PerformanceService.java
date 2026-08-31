package com.nexcentauri.scms.service;

import com.nexcentauri.scms.entity.PerformanceMetric;
import jakarta.enterprise.context.Dependent;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
@Dependent
public class PerformanceService {
    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager entityManager;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void record(String type, String operation, long durationMs, boolean success) {
        PerformanceMetric metric = new PerformanceMetric();
        metric.setMetricType(type);
        metric.setOperationName(operation);
        metric.setDurationMs(durationMs);
        metric.setSuccess(success);
        metric.setRecordedAt(LocalDateTime.now());
        entityManager.persist(metric);
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<PerformanceMetric> recent(int limit) {
        return entityManager.createQuery("SELECT p FROM PerformanceMetric p ORDER BY p.recordedAt DESC", PerformanceMetric.class)
                .setMaxResults(Math.max(1, Math.min(limit, 100)))
                .getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public double averageDuration() {
        Double value = entityManager.createQuery("SELECT AVG(p.durationMs) FROM PerformanceMetric p", Double.class).getSingleResult();
        return value == null ? 0.0 : value;
    }
}
