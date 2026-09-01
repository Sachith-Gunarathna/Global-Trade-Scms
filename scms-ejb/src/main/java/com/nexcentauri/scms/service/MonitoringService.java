package com.nexcentauri.scms.service;

import com.nexcentauri.scms.entity.AuditLog;
import com.nexcentauri.scms.entity.SupplyAlert;
import jakarta.annotation.security.DeclareRoles;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Stateless
@DeclareRoles({"ADMIN", "LOGISTICS_COORDINATOR", "WAREHOUSE_MANAGER", "CUSTOMS_AGENT"})
public class MonitoringService {

    @EJB
    private AuditService auditService;

    @EJB
    private AlertService alertService;

    @EJB
    private LogisticsTimerService timerService;

    @Inject
    private TimerExecutionMonitor timerExecutionMonitor;

    @Inject
    private InterceptorExecutionMonitor interceptorExecutionMonitor;

    @EJB
    private RouteOptimizationService routeOptimizationService;

    @EJB
    private CarrierGateway carrierGateway;

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Map<String, Object> snapshot() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put(
                "averageMethodDurationMs",
                Math.round(interceptorExecutionMonitor.averagePerformanceDuration() * 100.0) / 100.0
        );
        result.put("metrics", operationalMetrics());
        result.put("interceptorExecutions", interceptorExecutionMonitor.recent());
        result.put("timerExecutions", timerExecutionMonitor.recent());
        result.put("audit", audit(auditService.recent(25)));
        result.put("alerts", alerts(alertService.active(20)));
        result.put("timers", timerService.timerSnapshots());
        result.put("integrations", carrierGateway.health());
        return result;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Map<Long, Integer> routePriorities() {
        return routeOptimizationService.calculatePriorities();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public int applyRoutePriorities() {
        return routeOptimizationService.applyPriorities();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public List<Map<String, Object>> timers() {
        return timerService.timerSnapshots();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public List<Map<String, Object>> recentTimerExecutions() {
        return timerExecutionMonitor.recent();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public List<Map<String, Object>> recentInterceptorExecutions() {
        return interceptorExecutionMonitor.recent();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public List<Map<String, Object>> recentOperationalMetrics() {
        return operationalMetrics();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Map<String, Object> carrierIntegration() {
        return carrierGateway.health();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Map<String, Object> synchronizeCarriers() {
        return carrierGateway.synchronizeActiveShipments();
    }

    private List<Map<String, Object>> operationalMetrics() {
        List<Map<String, Object>> result = new ArrayList<>();
        result.addAll(interceptorExecutionMonitor.recent());
        result.addAll(timerExecutionMonitor.recent());

        result.sort(Comparator.comparing(
                item -> String.valueOf(item.get("recordedAt")),
                Comparator.reverseOrder()
        ));

        if (result.size() > 50) {
            return new ArrayList<>(result.subList(0, 50));
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
