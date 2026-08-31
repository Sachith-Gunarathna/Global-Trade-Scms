package com.nexcentauri.scms.service;

import com.nexcentauri.scms.entity.Shipment;
import com.nexcentauri.scms.entity.Vendor;
import com.nexcentauri.scms.exception.ShipmentNotFoundException;
import com.nexcentauri.scms.exception.SupplyChainApplicationException;
import com.nexcentauri.scms.interceptor.binding.AuditTrail;
import com.nexcentauri.scms.interceptor.binding.ComplianceChecked;
import com.nexcentauri.scms.interceptor.binding.Monitored;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
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
@DeclareRoles({"ADMIN", "LOGISTICS_COORDINATOR", "WAREHOUSE_MANAGER", "CUSTOMS_AGENT", "VENDOR_REP"})
@AuditTrail
@Monitored
public class ShipmentService {
    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager entityManager;
    @EJB
    private VendorService vendorService;

    @RolesAllowed({"ADMIN", "LOGISTICS_COORDINATOR", "WAREHOUSE_MANAGER", "CUSTOMS_AGENT", "VENDOR_REP"})
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Shipment> getAll() {
        return entityManager.createQuery("SELECT s FROM Shipment s JOIN FETCH s.vendor ORDER BY s.createdAt DESC", Shipment.class).getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Shipment> getAllForAutomation() {
        return entityManager.createQuery("SELECT s FROM Shipment s JOIN FETCH s.vendor ORDER BY s.createdAt DESC", Shipment.class).getResultList();
    }

    @RolesAllowed({"ADMIN", "LOGISTICS_COORDINATOR", "WAREHOUSE_MANAGER", "CUSTOMS_AGENT", "VENDOR_REP"})
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Shipment get(Long id) throws ShipmentNotFoundException {
        return require(id);
    }

    @RolesAllowed({"ADMIN", "LOGISTICS_COORDINATOR"})
    @ComplianceChecked
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Shipment create(Shipment shipment, Long vendorId) throws SupplyChainApplicationException {
        validate(shipment);
        if (findByTrackingNumber(shipment.getTrackingNumber()) != null) throw new SupplyChainApplicationException("Tracking number already exists.");
        Vendor vendor = vendorService.require(vendorId);
        shipment.setVendor(vendor);
        shipment.setStatus(normalizeStatus(shipment.getStatus()));
        shipment.setProgress(clamp(shipment.getProgress() == null ? 0 : shipment.getProgress()));
        shipment.setRoutePriority(20);
        shipment.setCreatedAt(LocalDateTime.now());
        shipment.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(shipment);
        return shipment;
    }

    @RolesAllowed({"ADMIN", "LOGISTICS_COORDINATOR", "CUSTOMS_AGENT"})
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Shipment updateStatus(Long id, String newStatus) throws ShipmentNotFoundException, SupplyChainApplicationException {
        String status = normalizeStatus(newStatus);
        if (!List.of("PENDING", "IN_TRANSIT", "DELAYED", "DELIVERED", "CANCELLED", "CUSTOMS_HOLD").contains(status)) {
            throw new SupplyChainApplicationException("Unsupported shipment status.");
        }
        Shipment shipment = require(id);
        shipment.setStatus(status);
        if ("DELIVERED".equals(status)) shipment.setProgress(100);
        shipment.setUpdatedAt(LocalDateTime.now());
        return shipment;
    }

    @RolesAllowed({"ADMIN", "LOGISTICS_COORDINATOR"})
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void delete(Long id) throws ShipmentNotFoundException {
        Shipment shipment = require(id);
        entityManager.createQuery("DELETE FROM CustomsDocument c WHERE c.shipment.id = :id").setParameter("id", id).executeUpdate();
        entityManager.remove(shipment);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public int markOverdueShipmentsDelayed(LocalDateTime now) {
        List<Shipment> shipments = entityManager.createQuery("SELECT s FROM Shipment s WHERE s.estimatedDeliveryDate IS NOT NULL AND s.estimatedDeliveryDate < :now AND s.status NOT IN ('DELIVERED','DELAYED','CANCELLED')", Shipment.class)
                .setParameter("now", now)
                .getResultList();
        for (Shipment shipment : shipments) {
            shipment.setStatus("DELAYED");
            shipment.setUpdatedAt(now);
        }
        return shipments.size();
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Shipment> activeForRouteOptimization() {
        return entityManager.createQuery("SELECT s FROM Shipment s JOIN FETCH s.vendor WHERE s.status IN ('IN_TRANSIT','DELAYED','CUSTOMS_HOLD')", Shipment.class).getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void updateRoutePriority(Long shipmentId, int priority) throws ShipmentNotFoundException {
        Shipment shipment = require(shipmentId);
        shipment.setRoutePriority(clamp(priority));
        shipment.setUpdatedAt(LocalDateTime.now());
    }

    private Shipment require(Long id) throws ShipmentNotFoundException {
        Shipment shipment = entityManager.find(Shipment.class, id);
        if (shipment == null) throw new ShipmentNotFoundException("Shipment with ID " + id + " was not found.");
        return shipment;
    }

    private Shipment findByTrackingNumber(String trackingNumber) {
        try {
            return entityManager.createQuery("SELECT s FROM Shipment s WHERE s.trackingNumber = :tracking", Shipment.class).setParameter("tracking", trackingNumber).getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    private void validate(Shipment shipment) throws SupplyChainApplicationException {
        if (shipment == null) throw new SupplyChainApplicationException("Shipment details are required.");
        if (shipment.getTrackingNumber() == null || shipment.getTrackingNumber().isBlank()) throw new SupplyChainApplicationException("Tracking number is required.");
        if (shipment.getOrigin() == null || shipment.getOrigin().isBlank()) throw new SupplyChainApplicationException("Shipment origin is required.");
        if (shipment.getDestination() == null || shipment.getDestination().isBlank()) throw new SupplyChainApplicationException("Shipment destination is required.");
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) return "PENDING";
        return status.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
    }

    private int clamp(int value) { return Math.max(0, Math.min(100, value)); }
}
