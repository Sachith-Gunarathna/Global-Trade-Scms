package com.nexcentauri.scms.service;

import com.nexcentauri.scms.entity.Shipment;
import com.nexcentauri.scms.exception.CarrierIntegrationException;
import com.nexcentauri.scms.exception.ShipmentNotFoundException;
import com.nexcentauri.scms.exception.SupplyChainApplicationException;
import com.nexcentauri.scms.interceptor.binding.AuditTrail;
import com.nexcentauri.scms.interceptor.binding.Monitored;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Stateless
@AuditTrail
@Monitored
public class CarrierIntegrationService implements CarrierGateway {
    private static final int MAX_ATTEMPTS = 3;

    @EJB
    private ShipmentService shipmentService;

    @EJB
    private AlertService alertService;

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Map<String, Object> synchronizeActiveShipments() {
        int checked = 0;
        int updated = 0;
        int failures = 0;
        List<Shipment> shipments = shipmentService.getAllForAutomation();
        for (Shipment shipment : shipments) {
            if (!isActive(shipment.getStatus())) continue;
            checked++;
            try {
                String carrierStatus = pollWithRetry(shipment);
                if (!carrierStatus.equals(normalize(shipment.getStatus()))) {
                    shipmentService.updateStatus(shipment.getId(), carrierStatus);
                    updated++;
                }
                alertService.resolve("CARRIER", shipment.getTrackingNumber());
            } catch (SupplyChainApplicationException exception) {
                failures++;
                alertService.raise("danger", "CARRIER", shipment.getTrackingNumber(), "Carrier status synchronization failed", safeMessage(exception));
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("checked", checked);
        result.put("updated", updated);
        result.put("failures", failures);
        result.put("healthy", failures == 0);
        return result;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Map<String, Object> health() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("adapter", "CarrierGateway");
        result.put("mode", "prototype-adapter");
        result.put("maxAttempts", MAX_ATTEMPTS);
        result.put("outageSimulation", outageEnabled());
        result.put("healthy", !outageEnabled());
        return result;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public String pollShipment(Long shipmentId) throws CarrierIntegrationException {
        try {
            Shipment shipment = shipmentService.get(shipmentId);
            return pollWithRetry(shipment);
        } catch (ShipmentNotFoundException exception) {
            throw new CarrierIntegrationException(exception.getMessage(), exception);
        }
    }

    private String pollWithRetry(Shipment shipment) throws CarrierIntegrationException {
        CarrierIntegrationException last = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return poll(shipment);
            } catch (CarrierIntegrationException exception) {
                last = exception;
            }
        }
        throw new CarrierIntegrationException("Carrier " + value(shipment.getCarrier()) + " did not respond after " + MAX_ATTEMPTS + " attempts.", last);
    }

    private String poll(Shipment shipment) throws CarrierIntegrationException {
        if (outageEnabled()) throw new CarrierIntegrationException("Simulated carrier gateway outage is active.");
        if (shipment.getCarrier() == null || shipment.getCarrier().isBlank()) throw new CarrierIntegrationException("Shipment " + shipment.getTrackingNumber() + " has no carrier configured.");
        String current = normalize(shipment.getStatus());
        if ("CUSTOMS_HOLD".equals(current) || "CANCELLED".equals(current) || "DELIVERED".equals(current)) return current;
        if (shipment.getEstimatedDeliveryDate() != null && shipment.getEstimatedDeliveryDate().isBefore(LocalDateTime.now())) return "DELAYED";
        if ("PENDING".equals(current) && shipment.getProgress() != null && shipment.getProgress() > 0) return "IN_TRANSIT";
        return current;
    }

    private boolean outageEnabled() {
        return "true".equalsIgnoreCase(System.getenv().getOrDefault("SCMS_CARRIER_OUTAGE", "false"));
    }

    private boolean isActive(String status) {
        String value = normalize(status);
        return "PENDING".equals(value) || "IN_TRANSIT".equals(value) || "DELAYED".equals(value) || "CUSTOMS_HOLD".equals(value);
    }

    private String normalize(String value) {
        return value == null ? "PENDING" : value.trim().toUpperCase().replace(' ', '_');
    }

    private String value(String value) {
        return value == null || value.isBlank() ? "unconfigured" : value.trim();
    }

    private String safeMessage(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? exception.getClass().getSimpleName() : message;
    }
}
