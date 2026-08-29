package com.nexcentauri.scms.service;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Stateless
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class DashboardService {

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager entityManager;

    public Map<String, Object> getDashboardData(){

        Map<String, Object> dashboard = new LinkedHashMap<>();

        dashboard.put("kpis", getKpis());
        dashboard.put("shipmentStatus", getShipmentStatusCounts());
        dashboard.put("alerts", getOperationalAlerts());
        dashboard.put("recentShipments", getRecentShipments());
        dashboard.put("recentActivity", getRecentActivity());

        return dashboard;

    }

    private Map<String, Object> getKpis(){
        Map<String, Object> kpis = new LinkedHashMap<>();

        Long totalShipments = entityManager.createQuery(
                "SELECT COUNT(s) FROM Shipment s", Long.class
        ).getSingleResult();

        Long inTransitShipments = entityManager.createQuery(
                "SELECT COUNT(s) FROM Shipment s " +
                        "WHERE UPPER(s.status) IN ('IN_TRANSIT','IN_TRANSIT')", Long.class
        ).getSingleResult();

        Long lowStockItems = entityManager.createQuery(
                "SELECT COUNT(i) FROM Inventory i " +
                        "WHERE i.quantity <= i.reorderLevel", Long.class
        ).getSingleResult();

        Long totalVendors = entityManager.createQuery(
                "SELECT COUNT(v) FROM Vendor v ", Long.class
        ).getSingleResult();

        kpis.put("totalShipments", totalShipments);
        kpis.put("inTransit", inTransitShipments);
        kpis.put("lowStockItems", lowStockItems);
        kpis.put("totalSuppliers", totalVendors);

        return kpis;
    }

    private Map<String, Long> getShipmentStatusCounts(){

        Map<String, Long> statusCounts = new LinkedHashMap<>();

        statusCounts.put("pending", 0L);
        statusCounts.put("inTransit", 0L);
        statusCounts.put("delayed", 0L);
        statusCounts.put("delivered", 0L);

        List<Object[]> results = entityManager.createQuery(
                "SELECT UPPER(s.status), COUNT(s) " +
                        "FROM Shipment s " +
                        "GROUP BY UPPER(s.status)", Object[].class
        ).getResultList();

        for(Object[] row : results){

            String status = row[0] != null ? row[0].toString().replace(' ','_') : "";

            Long count = (Long) row[1];

            switch (status){
                case "PENDING":
                    statusCounts.put("pending", count);
                    break;

                case "IN_TRANSIT":
                    statusCounts.put("inTransit", count);
                    break;

                case "DELAYED":
                    statusCounts.put("delayed", count);
                    break;

                case "DELIVERED":
                    statusCounts.put("delivered", count);
                    break;

            }

        }

        return statusCounts;
    }

    private List<Map<String, Object>> getOperationalAlerts(){

        List<Map<String,Object>> alerts = new ArrayList<>();

        List<Object[]> delayedShipments = entityManager.createQuery(
                "SELECT s.id, s.trachingNumber, s.origin, s.destination FROM Shipment s " +
                        "WHERE UPPER(s.status) = 'DELAYED' " +
                        "ORDER BY s.id DESC" , Object[].class
        )       .setMaxResults(3)
                .getResultList();

        for(Object[] row : delayedShipments){

            Map<String, Object> alert = new LinkedHashMap<>();

            alert.put("type", "danger");
            alert.put("category", "SHIPMENT");
            alert.put("referenceId", row[0]);
            alert.put(
                    "title",
                    "Shipment " + row[1] + " is delayed"
            );
            alert.put(
                    "message",
                    row[2] + " → " + row[3]
            );

            alerts.add(alert);

        }

        List<Object[]> lowStockItems = entityManager.createQuery(
                "SELECT i.id, i.itemName, i.quantity, i.reorderLevel FROM Inventory i " +
                        "WHERE i.quantity <= i.reorderLevel " +
                        "ORDER BY i.quantity ASC", Object[].class
        ).getResultList();

        for(Object[] row : lowStockItems){

            Map<String, Object> alert = new LinkedHashMap<>();

            alert.put("type","warning");
            alert.put("category", "INVENTORY");
            alert.put("referenceId", row[0]);
            alert.put(
                    "title",
                    "Low stock: " + row[1]
            );
            alert.put(
                    "message",
                    row[2] + " units remaining. Reorder level is " + row[3] + "."
            );

            alerts.add(alert);

        }

        LocalDateTime deadLineLimit = LocalDateTime.now().plusDays(1);

        List<Object[]> customsDocuments = entityManager.createQuery(
                "SELECT c.id, c.documentNumber, c.deadline FROM CustomsDocument c " +
                        "WHERE UPPER(c.status) = 'PENDING' " +
                        "AND c.deadline IS NOT NULL " +
                        "AND c.deadline <= :deadLineLimit " +
                        "ORDER BY c.deadline ASC", Object[].class
        )
                .setParameter("deadLineLimit", deadLineLimit)
                .setMaxResults(3)
                .getResultList();

        for(Object[] row : customsDocuments){

            Map<String, Object> alert = new LinkedHashMap<>();

            alert.put("type", "warning");
            alert.put("category", "CUSTOMS");
            alert.put("referenceId", row[0]);
            alert.put(
                    "title",
                    "Customs deadline approaching"
            );
            alert.put(
                    "message",
                    "Document " + row[1]
                            + " is due by "
                            + row[2]
            );

            alerts.add(alert);

        }

        return alerts;

    }

    private List<Map<String, Object>> getRecentShipments(){

        List<Object[]> results = entityManager.createQuery(
                "SELECT s.id, s.trachingNumber, s.origin, s.destination, s.status, s.estimatedDeliveryDate, v.name" +
                        " FROM Shipment s " +
                        "JOIN s.vendor v " +
                        "ORDER BY s.id DESC", Object[].class
        )
                .setMaxResults(3)
                .getResultList();

        List<Map<String, Object>> shipments = new ArrayList<>();

        for(Object[] row : results){

            Map<String, Object> shipment = new LinkedHashMap<>();

            shipment.put("id", row[0]);
            shipment.put("trackingNumber", row[1]);
            shipment.put("origin", row[2]);
            shipment.put("destination", row[3]);
            shipment.put("status", row[4]);

            shipment.put(
                    "estimatedDeliveryDate",
                    row[5] != null
                            ? row[5].toString()
                            : null
            );

            shipment.put("vendor", row[6]);

            shipments.add(shipment);

        }

        return shipments;

    }

    private List<Map<String,Object>> getRecentActivity(){

        List<Object[]> results = entityManager.createQuery(
                "SELECT a.id, a.action, a.methodName, a.timestamp, a.performedBy FROM AuditLog a " +
                        "ORDER BY a.timestamp DESC", Object[].class
        )
                .setMaxResults(3)
                .getResultList();

        List<Map<String, Object>> activity = new ArrayList<>();

        for(Object[] row : results){

            Map<String, Object> item = new LinkedHashMap<>();

            item.put("id", row[0]);
            item.put("action", row[1]);
            item.put("methodName", row[2]);

            item.put(
                    "timestamp",
                    row[3] != null
                            ? row[3].toString()
                            : null
            );

            item.put("performedBy", row[4]);

            activity.add(item);
        }

        return activity;

    }

}
