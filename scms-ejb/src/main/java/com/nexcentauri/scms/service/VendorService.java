package com.nexcentauri.scms.service;

import com.nexcentauri.scms.entity.Vendor;
import com.nexcentauri.scms.exception.SupplyChainApplicationException;
import com.nexcentauri.scms.exception.VendorNotFoundException;
import com.nexcentauri.scms.interceptor.binding.AuditTrail;
import com.nexcentauri.scms.interceptor.binding.Monitored;
import com.nexcentauri.scms.interceptor.binding.VendorValidated;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
@DeclareRoles({"ADMIN", "LOGISTICS_COORDINATOR", "WAREHOUSE_MANAGER", "CUSTOMS_AGENT", "VENDOR_REP"})
@AuditTrail
@Monitored
public class VendorService {
    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager entityManager;

    @RolesAllowed({"ADMIN", "LOGISTICS_COORDINATOR", "WAREHOUSE_MANAGER", "CUSTOMS_AGENT", "VENDOR_REP"})
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Vendor> getAll() {
        return entityManager.createQuery("SELECT v FROM Vendor v ORDER BY v.name", Vendor.class).getResultList();
    }

    @RolesAllowed({"ADMIN", "LOGISTICS_COORDINATOR", "WAREHOUSE_MANAGER"})
    @VendorValidated
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Vendor create(Vendor vendor) throws SupplyChainApplicationException {
        if (vendor == null) throw new SupplyChainApplicationException("Vendor details are required.");
        if (vendor.getCode() == null || vendor.getCode().isBlank()) vendor.setCode(nextCode());
        if (vendor.getRating() == null) vendor.setRating(4.0);
        if (vendor.getPerformanceScore() == null) vendor.setPerformanceScore(vendor.getRating() * 20.0);
        if (vendor.getOnTimeRate() == null) vendor.setOnTimeRate(90.0);
        if (vendor.getActiveOrders() == null) vendor.setActiveOrders(0);
        if (vendor.getStatus() == null || vendor.getStatus().isBlank()) vendor.setStatus("ACTIVE");
        vendor.setCreatedAt(LocalDateTime.now());
        entityManager.persist(vendor);
        return vendor;
    }

    @RolesAllowed({"ADMIN", "LOGISTICS_COORDINATOR"})
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Vendor updateScore(Long vendorId, double score) throws VendorNotFoundException, SupplyChainApplicationException {
        if (score < 0.0 || score > 100.0) throw new SupplyChainApplicationException("Performance score must be between 0 and 100.");
        Vendor vendor = require(vendorId);
        vendor.setPerformanceScore(score);
        vendor.setRating(Math.max(1.0, Math.min(5.0, score / 20.0)));
        return vendor;
    }

    @RolesAllowed({"ADMIN", "LOGISTICS_COORDINATOR"})
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void delete(Long vendorId) throws VendorNotFoundException, SupplyChainApplicationException {
        Vendor vendor = require(vendorId);
        Long shipmentCount = entityManager.createQuery("SELECT COUNT(s) FROM Shipment s WHERE s.vendor.id = :id", Long.class).setParameter("id", vendorId).getSingleResult();
        Long inventoryCount = entityManager.createQuery("SELECT COUNT(i) FROM Inventory i WHERE i.vendor.id = :id", Long.class).setParameter("id", vendorId).getSingleResult();
        if (shipmentCount > 0 || inventoryCount > 0) throw new SupplyChainApplicationException("Vendor cannot be deleted while shipments or inventory records are linked to it.");
        entityManager.remove(vendor);
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Vendor require(Long id) throws VendorNotFoundException {
        Vendor vendor = entityManager.find(Vendor.class, id);
        if (vendor == null) throw new VendorNotFoundException("Vendor with ID " + id + " was not found.");
        return vendor;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void recalculatePerformance() {
        List<Vendor> vendors = entityManager.createQuery("SELECT v FROM Vendor v", Vendor.class).getResultList();
        for (Vendor vendor : vendors) {
            List<String> statuses = entityManager.createQuery("SELECT s.status FROM Shipment s WHERE s.vendor.id = :id", String.class).setParameter("id", vendor.getId()).getResultList();
            if (statuses.isEmpty()) continue;
            double total = 0.0;
            for (String status : statuses) {
                String value = normalizeStatus(status);
                if ("DELIVERED".equals(value)) total += 100.0;
                else if ("IN_TRANSIT".equals(value)) total += 82.0;
                else if ("PENDING".equals(value)) total += 65.0;
                else if ("DELAYED".equals(value)) total += 40.0;
                else total += 55.0;
            }
            double score = Math.round((total / statuses.size()) * 100.0) / 100.0;
            vendor.setPerformanceScore(score);
            vendor.setRating(Math.round(Math.max(1.0, Math.min(5.0, score / 20.0)) * 10.0) / 10.0);
        }
    }

    private String nextCode() {
        Long count = entityManager.createQuery("SELECT COUNT(v) FROM Vendor v", Long.class).getSingleResult();
        return String.format("SUP-%03d", count + 1);
    }

    private String normalizeStatus(String status) {
        return status == null ? "" : status.trim().toUpperCase().replace(' ', '_');
    }
}
