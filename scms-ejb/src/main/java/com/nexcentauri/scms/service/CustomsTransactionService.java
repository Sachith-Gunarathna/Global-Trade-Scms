package com.nexcentauri.scms.service;

import com.nexcentauri.scms.entity.CustomsDocument;
import com.nexcentauri.scms.entity.Shipment;
import com.nexcentauri.scms.exception.CustomsComplianceException;
import com.nexcentauri.scms.interceptor.binding.AuditTrail;
import com.nexcentauri.scms.interceptor.binding.Monitored;
import jakarta.annotation.Resource;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.UserTransaction;
import java.time.LocalDateTime;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@DeclareRoles({"ADMIN", "CUSTOMS_AGENT"})
@AuditTrail
@Monitored
public class CustomsTransactionService {
    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager entityManager;
    @Resource
    private UserTransaction userTransaction;

    @RolesAllowed({"ADMIN", "CUSTOMS_AGENT"})
    public CustomsDocument releaseShipment(Long documentId) throws CustomsComplianceException {
        try {
            userTransaction.begin();
            CustomsDocument document = entityManager.find(CustomsDocument.class, documentId);
            if (document == null) throw new CustomsComplianceException("Customs document was not found.");
            if (!"APPROVED".equals(document.getStatus())) throw new CustomsComplianceException("Only an approved customs document can release a shipment.");
            Shipment shipment = document.getShipment();
            shipment.setStatus("IN_TRANSIT");
            shipment.setUpdatedAt(LocalDateTime.now());
            document.setStatus("CLEARED");
            document.setUpdatedAt(LocalDateTime.now());
            userTransaction.commit();
            return document;
        } catch (CustomsComplianceException exception) {
            rollback();
            throw exception;
        } catch (Exception exception) {
            rollback();
            throw new CustomsComplianceException("Customs release transaction failed and was rolled back.");
        }
    }

    private void rollback() {
        try { userTransaction.rollback(); } catch (Exception ignored) { }
    }
}
