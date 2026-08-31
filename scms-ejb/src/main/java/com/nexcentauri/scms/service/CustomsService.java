package com.nexcentauri.scms.service;

import com.nexcentauri.scms.entity.CustomsDocument;
import com.nexcentauri.scms.entity.Shipment;
import com.nexcentauri.scms.exception.CustomsComplianceException;
import com.nexcentauri.scms.exception.ShipmentNotFoundException;
import com.nexcentauri.scms.interceptor.binding.AuditTrail;
import com.nexcentauri.scms.interceptor.binding.ComplianceChecked;
import com.nexcentauri.scms.interceptor.binding.Monitored;
import jakarta.annotation.Resource;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Stateless
@DeclareRoles({"ADMIN", "LOGISTICS_COORDINATOR", "CUSTOMS_AGENT", "VENDOR_REP"})
@AuditTrail
@Monitored
public class CustomsService {
    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager entityManager;
    @Resource
    private SessionContext sessionContext;

    @RolesAllowed({"ADMIN", "LOGISTICS_COORDINATOR", "CUSTOMS_AGENT", "VENDOR_REP"})
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<CustomsDocument> getAll() {
        return entityManager.createQuery("SELECT c FROM CustomsDocument c JOIN FETCH c.shipment s JOIN FETCH s.vendor ORDER BY c.deadline", CustomsDocument.class).getResultList();
    }

    @RolesAllowed({"ADMIN", "LOGISTICS_COORDINATOR", "CUSTOMS_AGENT"})
    @ComplianceChecked
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public CustomsDocument create(CustomsDocument document, Long shipmentId) throws CustomsComplianceException, ShipmentNotFoundException {
        if (document == null) throw new CustomsComplianceException("Customs document details are required.");
        if (document.getDocumentNumber() == null || document.getDocumentNumber().isBlank()) throw new CustomsComplianceException("Document number is required.");
        if (findByNumber(document.getDocumentNumber()) != null) throw new CustomsComplianceException("Customs document number already exists.");
        Shipment shipment = entityManager.find(Shipment.class, shipmentId);
        if (shipment == null) throw new ShipmentNotFoundException("Shipment with ID " + shipmentId + " was not found.");
        document.setShipment(shipment);
        if (document.getDocumentType() == null || document.getDocumentType().isBlank()) document.setDocumentType("EXPORT_DECLARATION");
        document.setStatus(normalizeStatus(document.getStatus()));
        document.setCreatedAt(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(document);
        return document;
    }

    @RolesAllowed({"ADMIN", "CUSTOMS_AGENT"})
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public CustomsDocument approve(Long id, String notes) throws CustomsComplianceException {
        requireCustomsAuthority();
        CustomsDocument document = require(id);
        document.setStatus("APPROVED");
        document.setNotes(notes);
        document.setUpdatedAt(LocalDateTime.now());
        return document;
    }

    @RolesAllowed({"ADMIN", "CUSTOMS_AGENT"})
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public CustomsDocument reject(Long id, String notes) throws CustomsComplianceException {
        requireCustomsAuthority();
        if (notes == null || notes.isBlank()) throw new CustomsComplianceException("A rejection reason is required.");
        CustomsDocument document = require(id);
        document.setStatus("REJECTED");
        document.setNotes(notes.trim());
        document.setUpdatedAt(LocalDateTime.now());
        Shipment shipment = document.getShipment();
        shipment.setStatus("CUSTOMS_HOLD");
        shipment.setUpdatedAt(LocalDateTime.now());
        return document;
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<CustomsDocument> dueBefore(LocalDateTime deadline) {
        return entityManager.createQuery("SELECT c FROM CustomsDocument c JOIN FETCH c.shipment s WHERE c.status = 'PENDING' AND c.deadline IS NOT NULL AND c.deadline <= :deadline", CustomsDocument.class)
                .setParameter("deadline", deadline)
                .getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public CustomsDocument require(Long id) throws CustomsComplianceException {
        CustomsDocument document = entityManager.find(CustomsDocument.class, id);
        if (document == null) throw new CustomsComplianceException("Customs document with ID " + id + " was not found.");
        return document;
    }

    private CustomsDocument findByNumber(String number) {
        try {
            return entityManager.createQuery("SELECT c FROM CustomsDocument c WHERE c.documentNumber = :number", CustomsDocument.class).setParameter("number", number).getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    private void requireCustomsAuthority() throws CustomsComplianceException {
        if (!sessionContext.isCallerInRole("CUSTOMS_AGENT") && !sessionContext.isCallerInRole("ADMIN")) {
            throw new CustomsComplianceException("Programmatic authorization denied this customs action.");
        }
    }

    private String normalizeStatus(String status) {
        return status == null || status.isBlank() ? "PENDING" : status.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
    }
}
