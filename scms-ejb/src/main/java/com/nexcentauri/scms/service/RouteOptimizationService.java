package com.nexcentauri.scms.service;

import com.nexcentauri.scms.entity.Shipment;
import com.nexcentauri.scms.exception.ShipmentNotFoundException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Stateless
public class RouteOptimizationService {
    @EJB
    private ShipmentService shipmentService;

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Map<Long, Integer> calculatePriorities() {
        List<Shipment> shipments = shipmentService.activeForRouteOptimization();
        Map<Long, Integer> priorities = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();
        for (Shipment shipment : shipments) {
            int priority = RoutePriorityCalculator.calculate(shipment.getStatus(), shipment.getEstimatedDeliveryDate(), now);
            priorities.put(shipment.getId(), priority);
        }
        return priorities;
    }

    public int applyPriorities() {
        Map<Long, Integer> values = calculatePriorities();
        int updated = 0;
        for (Map.Entry<Long, Integer> entry : values.entrySet()) {
            try {
                shipmentService.updateRoutePriority(entry.getKey(), entry.getValue());
                updated++;
            } catch (ShipmentNotFoundException ignored) { }
        }
        return updated;
    }
}
