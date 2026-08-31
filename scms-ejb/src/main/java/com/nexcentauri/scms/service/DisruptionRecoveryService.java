package com.nexcentauri.scms.service;

import com.nexcentauri.scms.entity.Shipment;
import com.nexcentauri.scms.exception.ShipmentNotFoundException;
import com.nexcentauri.scms.exception.SupplyChainApplicationException;
import com.nexcentauri.scms.interceptor.binding.AuditTrail;
import com.nexcentauri.scms.interceptor.binding.Monitored;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import java.time.LocalDateTime;
import java.util.Locale;

@Stateless
@AuditTrail
@Monitored
public class DisruptionRecoveryService {
    @EJB
    private ShipmentService shipmentService;

    @EJB
    private AlertService alertService;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Shipment reportWeatherDisruption(Long shipmentId, String severity) throws SupplyChainApplicationException {
        Shipment shipment = shipmentService.get(shipmentId);
        String currentStatus = normalize(shipment.getStatus());
        if ("DELIVERED".equals(currentStatus) || "CANCELLED".equals(currentStatus)) {
            throw new SupplyChainApplicationException("Weather disruption recovery applies only to active shipments.");
        }
        String normalizedSeverity = normalizeSeverity(severity);
        int routePriority = Math.max(basePriority(normalizedSeverity), RoutePriorityCalculator.calculate(currentStatus, shipment.getEstimatedDeliveryDate(), LocalDateTime.now()));
        shipmentService.updateRoutePriority(shipmentId, routePriority);
        if (!"DELAYED".equals(currentStatus)) shipmentService.updateStatus(shipmentId, "DELAYED");
        alertService.raise(
                "danger",
                "WEATHER",
                shipment.getTrackingNumber(),
                "Weather disruption on active route",
                normalizedSeverity + " weather disruption reported for " + shipment.getOrigin() + " to " + shipment.getDestination() + ". Route priority was raised for recovery handling."
        );
        return shipmentService.get(shipmentId);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Shipment resolveWeatherDisruption(Long shipmentId) throws ShipmentNotFoundException {
        Shipment shipment = shipmentService.get(shipmentId);
        alertService.resolve("WEATHER", shipment.getTrackingNumber());
        int priority = RoutePriorityCalculator.calculate(shipment.getStatus(), shipment.getEstimatedDeliveryDate(), LocalDateTime.now());
        shipmentService.updateRoutePriority(shipmentId, priority);
        return shipmentService.get(shipmentId);
    }

    private String normalizeSeverity(String severity) {
        if (severity == null || severity.isBlank()) return "MEDIUM";
        String value = severity.trim().toUpperCase(Locale.ROOT);
        return switch (value) {
            case "LOW", "MEDIUM", "HIGH", "CRITICAL" -> value;
            default -> "MEDIUM";
        };
    }

    private int basePriority(String severity) {
        return switch (severity) {
            case "CRITICAL" -> 100;
            case "HIGH" -> 90;
            case "LOW" -> 60;
            default -> 75;
        };
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
    }
}
