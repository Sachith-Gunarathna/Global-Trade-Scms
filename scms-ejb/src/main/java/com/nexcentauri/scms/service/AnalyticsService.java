package com.nexcentauri.scms.service;

import com.nexcentauri.scms.entity.Vendor;
import jakarta.annotation.security.DeclareRoles;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Stateless
@DeclareRoles({"ADMIN", "LOGISTICS_COORDINATOR", "WAREHOUSE_MANAGER", "CUSTOMS_AGENT", "VENDOR_REP"})
public class AnalyticsService {
    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager entityManager;

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Map<String, Object> snapshot() {
        return snapshot(null);
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Map<String, Object> snapshotForVendor(Long vendorId) {
        return snapshot(vendorId);
    }

    private Map<String, Object> snapshot(Long vendorId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("revenue", revenue(vendorId));
        result.put("regions", regions(vendorId));
        result.put("fulfillment", fulfillment(vendorId));
        result.put("topSuppliers", topSuppliers(vendorId));
        return result;
    }

    private List<Map<String, Object>> revenue(Long vendorId) {
        List<Map<String, Object>> rows = new ArrayList<>();
        YearMonth current = YearMonth.now();
        for (int offset = 5; offset >= 0; offset--) {
            YearMonth month = current.minusMonths(offset);
            LocalDate start = month.atDay(1);
            LocalDate end = month.atEndOfMonth();
            BigDecimal total;
            if (vendorId == null) {
                total = entityManager.createQuery("SELECT COALESCE(SUM(o.totalAmount), 0) FROM TradeOrder o WHERE o.orderDate BETWEEN :start AND :end", BigDecimal.class)
                        .setParameter("start", start)
                        .setParameter("end", end)
                        .getSingleResult();
            } else {
                total = entityManager.createQuery("SELECT COALESCE(SUM(o.totalAmount), 0) FROM TradeOrder o WHERE o.vendor.id = :vendorId AND o.orderDate BETWEEN :start AND :end", BigDecimal.class)
                        .setParameter("vendorId", vendorId)
                        .setParameter("start", start)
                        .setParameter("end", end)
                        .getSingleResult();
            }
            BigDecimal safeTotal = total == null ? BigDecimal.ZERO : total;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("month", month.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
            row.put("revenue", safeTotal);
            row.put("target", safeTotal.multiply(BigDecimal.valueOf(0.92)).setScale(2, java.math.RoundingMode.HALF_UP));
            rows.add(row);
        }
        return rows;
    }

    private List<Map<String, Object>> regions(Long vendorId) {
        List<Object[]> values;
        if (vendorId == null) {
            values = entityManager.createQuery("SELECT COALESCE(o.region, 'Unassigned'), COUNT(o) FROM TradeOrder o GROUP BY o.region ORDER BY COUNT(o) DESC", Object[].class).getResultList();
        } else {
            values = entityManager.createQuery("SELECT COALESCE(o.region, 'Unassigned'), COUNT(o) FROM TradeOrder o WHERE o.vendor.id = :vendorId GROUP BY o.region ORDER BY COUNT(o) DESC", Object[].class)
                    .setParameter("vendorId", vendorId)
                    .getResultList();
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Object[] value : values) rows.add(Map.of("region", String.valueOf(value[0]), "shipments", ((Number) value[1]).longValue()));
        return rows;
    }

    private List<Map<String, Object>> fulfillment(Long vendorId) {
        long total = orderCount(vendorId, null);
        long delivered = orderCount(vendorId, "DELIVERED");
        long shipped = orderCount(vendorId, "SHIPPED");
        long open = Math.max(0L, total - delivered - shipped);
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(Map.of("name", "Delivered", "value", percent(delivered, total)));
        rows.add(Map.of("name", "Shipped", "value", percent(shipped, total)));
        rows.add(Map.of("name", "Open", "value", percent(open, total)));
        return rows;
    }

    private long orderCount(Long vendorId, String status) {
        StringBuilder query = new StringBuilder("SELECT COUNT(o) FROM TradeOrder o WHERE 1 = 1");
        if (vendorId != null) query.append(" AND o.vendor.id = :vendorId");
        if (status != null) query.append(" AND o.status = :status");
        var typed = entityManager.createQuery(query.toString(), Long.class);
        if (vendorId != null) typed.setParameter("vendorId", vendorId);
        if (status != null) typed.setParameter("status", status);
        Long value = typed.getSingleResult();
        return value == null ? 0L : value;
    }

    private int percent(long value, long total) {
        if (total <= 0L) return 0;
        return (int) Math.round(value * 100.0 / total);
    }

    private List<Map<String, Object>> topSuppliers(Long vendorId) {
        List<Vendor> vendors;
        if (vendorId == null) {
            vendors = entityManager.createQuery("SELECT v FROM Vendor v ORDER BY v.activeOrders DESC, v.rating DESC", Vendor.class).setMaxResults(5).getResultList();
        } else {
            vendors = entityManager.createQuery("SELECT v FROM Vendor v WHERE v.id = :vendorId", Vendor.class).setParameter("vendorId", vendorId).getResultList();
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Vendor vendor : vendors) rows.add(Map.of("name", vendor.getName(), "volume", vendor.getActiveOrders() == null ? 0 : vendor.getActiveOrders()));
        return rows;
    }
}
