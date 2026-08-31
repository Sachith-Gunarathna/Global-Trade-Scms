package com.nexcentauri.scms.service;

import com.nexcentauri.scms.entity.AuditLog;
import com.nexcentauri.scms.entity.PerformanceMetric;
import com.nexcentauri.scms.entity.SupplyAlert;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Stateless
@DeclareRoles({"ADMIN", "LOGISTICS_COORDINATOR", "WAREHOUSE_MANAGER", "CUSTOMS_AGENT"})
public class MonitoringService {
    @EJB
    private AuditService auditService;
    @EJB
    private PerformanceService performanceService;
    @EJB
    private AlertService alertService;
    @EJB
    private LogisticsTimerService timerService;
    @EJB
    private RouteOptimizationService routeOptimizationService;

    @RolesAllowed({"ADMIN", "LOGISTICS_COORDINATOR", "WAREHOUSE_MANAGER", "CUSTOMS_AGENT"})
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Map<String, Object> snapshot() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("averageMethodDurationMs", Math.round(performanceService.averageDuration() * 100.0) / 100.0);
        result.put("metrics", metrics(performanceService.recent(20)));
        result.put("audit", audit(auditService.recent(25)));
        result.put("alerts", alerts(alertService.active(20)));
        result.put("timers", timerService.timerSnapshots());
        return result;
    }

    @RolesAllowed({"ADMIN", "LOGISTICS_COORDINATOR", "WAREHOUSE_MANAGER", "CUSTOMS_AGENT"})
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Map<Long, Integer> routePriorities() {
        return routeOptimizationService.calculatePriorities();
    }

    @RolesAllowed({"ADMIN", "LOGISTICS_COORDINATOR"})
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public int applyRoutePriorities() {
        return routeOptimizationService.applyPriorities();
    }

    @RolesAllowed({"ADMIN", "LOGISTICS_COORDINATOR", "WAREHOUSE_MANAGER", "CUSTOMS_AGENT"})
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public List<Map<String, Object>> timers() {
        return timerService.timerSnapshots();
    }

    private List<Map<String, Object>> metrics(List<PerformanceMetric> rows) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (PerformanceMetric row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", row.getId());
            item.put("type", row.getMetricType());
            item.put("operation", row.getOperationName());
            item.put("durationMs", row.getDurationMs());
            item.put("success", row.getSuccess());
            item.put("recordedAt", row.getRecordedAt() == null ? null : row.getRecordedAt().toString());
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> audit(List<AuditLog> rows) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (AuditLog row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", row.getId());
            item.put("action", row.getAction());
            item.put("component", row.getComponentName());
            item.put("method", row.getMethodName());
            item.put("performedBy", row.getPerformedBy());
            item.put("durationMs", row.getDurationMs());
            item.put("success", row.getSuccess());
            item.put("detail", row.getDetail());
            item.put("timestamp", row.getTimestamp() == null ? null : row.getTimestamp().toString());
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> alerts(List<SupplyAlert> rows) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (SupplyAlert row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", row.getId());
            item.put("type", row.getType());
            item.put("category", row.getCategory());
            item.put("referenceKey", row.getReferenceKey());
            item.put("title", row.getTitle());
            item.put("message", row.getMessage());
            item.put("createdAt", row.getCreatedAt() == null ? null : row.getCreatedAt().toString());
            result.add(item);
        }
        return result;
    }
}
