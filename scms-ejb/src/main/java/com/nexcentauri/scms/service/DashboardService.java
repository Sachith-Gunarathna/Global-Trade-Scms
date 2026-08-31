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
import java.util.List;
import java.util.Map;

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

    private Map<String, Object> kpis() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalShipments", count("SELECT COUNT(s) FROM Shipment s"));
        result.put("inTransit", count("SELECT COUNT(s) FROM Shipment s WHERE s.status = 'IN_TRANSIT'"));
        result.put("lowStockItems", count("SELECT COUNT(i) FROM Inventory i WHERE i.quantity <= i.reorderLevel"));
        result.put("totalSuppliers", count("SELECT COUNT(v) FROM Vendor v WHERE v.status = 'ACTIVE'"));
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

    private List<Map<String, Object>> alerts() {
        List<SupplyAlert> rows = entityManager.createQuery("SELECT a FROM SupplyAlert a WHERE a.status = 'ACTIVE' ORDER BY a.createdAt DESC", SupplyAlert.class).setMaxResults(8).getResultList();
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
        List<Map<String, Object>> result = new ArrayList<>();
        for (AuditLog row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", row.getId());
            item.put("action", row.getAction());
            item.put("methodName", row.getComponentName() + "." + row.getMethodName());
            item.put("timestamp", row.getTimestamp() == null ? null : row.getTimestamp().toString());
            item.put("performedBy", row.getPerformedBy());
            result.add(item);
        }
        return result;
    }

    private long count(String query) { return entityManager.createQuery(query, Long.class).getSingleResult(); }
    private long statusCount(String status) { return entityManager.createQuery("SELECT COUNT(s) FROM Shipment s WHERE s.status = :status", Long.class).setParameter("status", status).getSingleResult(); }
}
