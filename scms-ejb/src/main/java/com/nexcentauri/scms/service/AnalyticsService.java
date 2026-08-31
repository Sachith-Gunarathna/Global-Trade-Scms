package com.nexcentauri.scms.service;

import com.nexcentauri.scms.entity.TradeOrder;
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
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("revenue", revenue());
        result.put("regions", regions());
        result.put("fulfillment", fulfillment());
        result.put("topSuppliers", topSuppliers());
        return result;
    }

    private List<Map<String, Object>> revenue() {
        List<Map<String, Object>> rows = new ArrayList<>();
        YearMonth current = YearMonth.now();
        for (int offset = 5; offset >= 0; offset--) {
            YearMonth month = current.minusMonths(offset);
            LocalDate start = month.atDay(1);
            LocalDate end = month.atEndOfMonth();
            BigDecimal total = entityManager.createQuery(
                    "SELECT COALESCE(SUM(o.totalAmount), 0) FROM TradeOrder o WHERE o.orderDate BETWEEN :start AND :end",
                    BigDecimal.class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getSingleResult();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("month", month.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
            row.put("revenue", total == null ? BigDecimal.ZERO : total);
            row.put("target", total == null ? BigDecimal.ZERO : total.multiply(BigDecimal.valueOf(0.92)).setScale(2, java.math.RoundingMode.HALF_UP));
            rows.add(row);
        }
        return rows;
    }

    private List<Map<String, Object>> regions() {
        List<Object[]> values = entityManager.createQuery(
                "SELECT COALESCE(o.region, 'Unassigned'), COUNT(o) FROM TradeOrder o GROUP BY o.region ORDER BY COUNT(o) DESC",
                Object[].class).getResultList();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Object[] value : values) {
            rows.add(Map.of("region", String.valueOf(value[0]), "shipments", ((Number) value[1]).longValue()));
        }
        return rows;
    }

    private List<Map<String, Object>> fulfillment() {
        Long total = entityManager.createQuery("SELECT COUNT(o) FROM TradeOrder o", Long.class).getSingleResult();
        long safeTotal = total == null ? 0L : total;
        long delivered = countStatus("DELIVERED");
        long shipped = countStatus("SHIPPED");
        long open = Math.max(0L, safeTotal - delivered - shipped);
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(Map.of("name", "Delivered", "value", percent(delivered, safeTotal)));
        rows.add(Map.of("name", "Shipped", "value", percent(shipped, safeTotal)));
        rows.add(Map.of("name", "Open", "value", percent(open, safeTotal)));
        return rows;
    }

    private long countStatus(String status) {
        Long value = entityManager.createQuery("SELECT COUNT(o) FROM TradeOrder o WHERE o.status = :status", Long.class)
                .setParameter("status", status)
                .getSingleResult();
        return value == null ? 0L : value;
    }

    private int percent(long value, long total) {
        if (total <= 0L) return 0;
        return (int) Math.round(value * 100.0 / total);
    }

    private List<Map<String, Object>> topSuppliers() {
        List<Vendor> vendors = entityManager.createQuery("SELECT v FROM Vendor v ORDER BY v.activeOrders DESC, v.rating DESC", Vendor.class)
                .setMaxResults(5)
                .getResultList();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Vendor vendor : vendors) {
            rows.add(Map.of("name", vendor.getName(), "volume", vendor.getActiveOrders() == null ? 0 : vendor.getActiveOrders()));
        }
        return rows;
    }
}
