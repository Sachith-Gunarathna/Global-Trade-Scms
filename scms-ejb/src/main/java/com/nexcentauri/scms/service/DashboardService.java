package com.nexcentauri.scms.service;

import com.nexcentauri.scms.entity.AuditLog;
import com.nexcentauri.scms.entity.SupplyAlert;
import jakarta.annotation.security.DeclareRoles;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Stateless
@DeclareRoles({"ADMIN", "LOGISTICS_COORDINATOR", "WAREHOUSE_MANAGER", "CUSTOMS_AGENT", "VENDOR_REP"})
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class DashboardService {
    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager entityManager;

    public Map<String, Object> getDashboardData() {
        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("kpis", kpis());
        dashboard.put("shipmentStatus", shipmentStatus());
        dashboard.put("alerts", alerts());
        dashboard.put("recentShipments", recentShipments());
        dashboard.put("recentActivity", recentActivity());
        return dashboard;
    }

    public Map<String, Object> getDashboardDataForVendor(Long vendorId, String userEmail) {
        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("kpis", vendorKpis(vendorId));
        dashboard.put("shipmentStatus", vendorShipmentStatus(vendorId));
        dashboard.put("alerts", vendorAlerts(vendorId));
        dashboard.put("recentShipments", recentShipmentsForVendor(vendorId));
        dashboard.put("recentActivity", recentActivityForUser(userEmail));
        return dashboard;
    }

    private Map<String, Object> kpis() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalShipments", count("SELECT COUNT(s) FROM Shipment s"));
        result.put("inTransit", count("SELECT COUNT(s) FROM Shipment s WHERE s.status = 'IN_TRANSIT'"));
        result.put("lowStockItems", count("SELECT COUNT(i) FROM Inventory i WHERE i.quantity <= i.reorderLevel"));
        result.put("totalSuppliers", count("SELECT COUNT(v) FROM Vendor v WHERE v.status = 'ACTIVE'"));
        return result;
    }

    private Map<String, Object> vendorKpis(Long vendorId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalShipments", countForVendor("SELECT COUNT(s) FROM Shipment s WHERE s.vendor.id = :vendorId", vendorId));
        result.put("inTransit", countForVendor("SELECT COUNT(s) FROM Shipment s WHERE s.vendor.id = :vendorId AND s.status = 'IN_TRANSIT'", vendorId));
        result.put("lowStockItems", countForVendor("SELECT COUNT(i) FROM Inventory i WHERE i.vendor.id = :vendorId AND i.quantity <= i.reorderLevel", vendorId));
        result.put("totalSuppliers", countForVendor("SELECT COUNT(v) FROM Vendor v WHERE v.id = :vendorId AND v.status = 'ACTIVE'", vendorId));
        return result;
    }

    private Map<String, Long> shipmentStatus() {
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("pending", statusCount("PENDING"));
        result.put("inTransit", statusCount("IN_TRANSIT"));
        result.put("delayed", statusCount("DELAYED"));
        result.put("delivered", statusCount("DELIVERED"));
        return result;
    }

    private Map<String, Long> vendorShipmentStatus(Long vendorId) {
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("pending", statusCountForVendor("PENDING", vendorId));
        result.put("inTransit", statusCountForVendor("IN_TRANSIT", vendorId));
        result.put("delayed", statusCountForVendor("DELAYED", vendorId));
        result.put("delivered", statusCountForVendor("DELIVERED", vendorId));
        return result;
    }

    private List<Map<String, Object>> alerts() {
        List<SupplyAlert> rows = entityManager.createQuery("SELECT a FROM SupplyAlert a WHERE a.status = 'ACTIVE' ORDER BY a.createdAt DESC", SupplyAlert.class).setMaxResults(8).getResultList();
        return mapAlerts(rows);
    }

    private List<Map<String, Object>> vendorAlerts(Long vendorId) {
        Set<String> references = new LinkedHashSet<>();
        references.addAll(entityManager.createQuery("SELECT s.trackingNumber FROM Shipment s WHERE s.vendor.id = :vendorId", String.class).setParameter("vendorId", vendorId).getResultList());
        references.addAll(entityManager.createQuery("SELECT i.sku FROM Inventory i WHERE i.vendor.id = :vendorId", String.class).setParameter("vendorId", vendorId).getResultList());
        references.addAll(entityManager.createQuery("SELECT c.documentNumber FROM CustomsDocument c WHERE c.shipment.vendor.id = :vendorId", String.class).setParameter("vendorId", vendorId).getResultList());
        if (references.isEmpty()) return List.of();
        List<SupplyAlert> rows = entityManager.createQuery("SELECT a FROM SupplyAlert a WHERE a.status = 'ACTIVE' AND a.referenceKey IN :references ORDER BY a.createdAt DESC", SupplyAlert.class)
                .setParameter("references", references)
                .setMaxResults(8)
                .getResultList();
        return mapAlerts(rows);
    }

    private List<Map<String, Object>> mapAlerts(List<SupplyAlert> rows) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (SupplyAlert row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", row.getType());
            item.put("category", row.getCategory());
            item.put("referenceId", row.getReferenceKey());
            item.put("title", row.getTitle());
            item.put("message", row.getMessage());
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> recentShipments() {
        List<Object[]> rows = entityManager.createQuery("SELECT s.id, s.trackingNumber, s.origin, s.destination, s.status, s.estimatedDeliveryDate, v.name FROM Shipment s JOIN s.vendor v ORDER BY s.createdAt DESC", Object[].class).setMaxResults(5).getResultList();
        return mapShipments(rows);
    }

    private List<Map<String, Object>> recentShipmentsForVendor(Long vendorId) {
        List<Object[]> rows = entityManager.createQuery("SELECT s.id, s.trackingNumber, s.origin, s.destination, s.status, s.estimatedDeliveryDate, v.name FROM Shipment s JOIN s.vendor v WHERE v.id = :vendorId ORDER BY s.createdAt DESC", Object[].class)
                .setParameter("vendorId", vendorId)
                .setMaxResults(5)
                .getResultList();
        return mapShipments(rows);
    }

    private List<Map<String, Object>> mapShipments(List<Object[]> rows) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", row[0]);
            item.put("trackingNumber", row[1]);
            item.put("origin", row[2]);
            item.put("destination", row[3]);
            item.put("status", row[4]);
            item.put("estimatedDeliveryDate", row[5] == null ? null : row[5].toString());
            item.put("vendor", row[6]);
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> recentActivity() {
        List<AuditLog> rows = entityManager.createQuery("SELECT a FROM AuditLog a ORDER BY a.timestamp DESC", AuditLog.class).setMaxResults(6).getResultList();
        return mapActivity(rows);
    }

    private List<Map<String, Object>> recentActivityForUser(String email) {
        if (email == null || email.isBlank()) return List.of();
        List<AuditLog> rows = entityManager.createQuery("SELECT a FROM AuditLog a WHERE LOWER(a.performedBy) = LOWER(:email) ORDER BY a.timestamp DESC", AuditLog.class)
                .setParameter("email", email)
                .setMaxResults(6)
                .getResultList();
        return mapActivity(rows);
    }

    private List<Map<String, Object>> mapActivity(List<AuditLog> rows) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (AuditLog row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", row.getId());
            item.put("action", row.getAction());
            item.put("methodName", value(row.getComponentName()) + "." + value(row.getMethodName()));
            item.put("timestamp", row.getTimestamp() == null ? null : row.getTimestamp().toString());
            item.put("performedBy", row.getPerformedBy());
            result.add(item);
        }
        return result;
    }

    private long count(String query) {
        Long value = entityManager.createQuery(query, Long.class).getSingleResult();
        return value == null ? 0L : value;
    }

    private long countForVendor(String query, Long vendorId) {
        Long value = entityManager.createQuery(query, Long.class).setParameter("vendorId", vendorId).getSingleResult();
        return value == null ? 0L : value;
    }

    private long statusCount(String status) {
        Long value = entityManager.createQuery("SELECT COUNT(s) FROM Shipment s WHERE s.status = :status", Long.class).setParameter("status", status).getSingleResult();
        return value == null ? 0L : value;
    }

    private long statusCountForVendor(String status, Long vendorId) {
        Long value = entityManager.createQuery("SELECT COUNT(s) FROM Shipment s WHERE s.status = :status AND s.vendor.id = :vendorId", Long.class)
                .setParameter("status", status)
                .setParameter("vendorId", vendorId)
                .getSingleResult();
        return value == null ? 0L : value;
    }

    private String value(String value) {
        return value == null ? "" : value;
    }
}
