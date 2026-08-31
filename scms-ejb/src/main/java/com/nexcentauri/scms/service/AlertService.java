package com.nexcentauri.scms.service;

import com.nexcentauri.scms.entity.SupplyAlert;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class AlertService {
    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager entityManager;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public SupplyAlert raise(String type, String category, String referenceKey, String title, String message) {
        SupplyAlert alert = findActive(category, referenceKey);
        if (alert == null) {
            alert = new SupplyAlert();
            alert.setType(type);
            alert.setCategory(category);
            alert.setReferenceKey(referenceKey);
            alert.setStatus("ACTIVE");
            alert.setCreatedAt(LocalDateTime.now());
            entityManager.persist(alert);
        }
        alert.setType(type);
        alert.setTitle(title);
        alert.setMessage(message);
        return alert;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void resolve(String category, String referenceKey) {
        SupplyAlert alert = findActive(category, referenceKey);
        if (alert != null) {
            alert.setStatus("RESOLVED");
            alert.setResolvedAt(LocalDateTime.now());
        }
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<SupplyAlert> active(int limit) {
        return entityManager.createQuery("SELECT a FROM SupplyAlert a WHERE a.status = 'ACTIVE' ORDER BY a.createdAt DESC", SupplyAlert.class)
                .setMaxResults(Math.max(1, Math.min(limit, 100)))
                .getResultList();
    }

    private SupplyAlert findActive(String category, String referenceKey) {
        try {
            return entityManager.createQuery("SELECT a FROM SupplyAlert a WHERE a.category = :category AND a.referenceKey = :referenceKey AND a.status = 'ACTIVE'", SupplyAlert.class)
                    .setParameter("category", category)
                    .setParameter("referenceKey", referenceKey)
                    .getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }
}
