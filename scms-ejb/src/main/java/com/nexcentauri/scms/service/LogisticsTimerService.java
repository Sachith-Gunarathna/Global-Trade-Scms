package com.nexcentauri.scms.service;

import com.nexcentauri.scms.entity.CustomsDocument;
import com.nexcentauri.scms.entity.Inventory;
import com.nexcentauri.scms.entity.Shipment;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.Timeout;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerConfig;
import jakarta.ejb.TimerService;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Singleton
@Startup
@Lock(LockType.READ)
public class LogisticsTimerService {
    private static final String CUSTOMS_TIMER = "CUSTOMS_DEADLINE_TIMER";
    @Resource
    private TimerService timerService;
    @EJB
    private ShipmentService shipmentService;
    @EJB
    private InventoryService inventoryService;
    @EJB
    private VendorService vendorService;
    @EJB
    private CustomsService customsService;
    @EJB
    private RouteOptimizationService routeOptimizationService;
    @EJB
    private AlertService alertService;
    @EJB
    private PerformanceService performanceService;
    @EJB
    private CarrierGateway carrierGateway;

    @PostConstruct
    @Lock(LockType.WRITE)
    public void initializeProgrammaticTimer() {
        boolean exists = timerService.getTimers().stream().anyMatch(timer -> CUSTOMS_TIMER.equals(String.valueOf(timer.getInfo())));
        if (!exists) {
            Date firstRun = new Date(System.currentTimeMillis() + 60_000L);
            timerService.createIntervalTimer(firstRun, 30L * 60L * 1000L, new TimerConfig(CUSTOMS_TIMER, true));
        }
    }

    @Schedule(hour = "*", minute = "*/5", second = "0", persistent = true)
    public void monitorShipments() {
        long started = System.nanoTime();
        boolean success = false;
        try {
            carrierGateway.synchronizeActiveShipments();
            shipmentService.markOverdueShipmentsDelayed(LocalDateTime.now());
            for (Shipment shipment : shipmentService.getAllForAutomation()) {
                if ("DELAYED".equals(shipment.getStatus())) {
                    alertService.raise("danger", "SHIPMENT", shipment.getTrackingNumber(), "Shipment " + shipment.getTrackingNumber() + " is delayed", shipment.getOrigin() + " to " + shipment.getDestination() + " requires attention.");
                } else {
                    alertService.resolve("SHIPMENT", shipment.getTrackingNumber());
                }
            }
            success = true;
        } finally {
            performanceService.record("TIMER", "monitorShipments", elapsed(started), success);
        }
    }

    @Schedule(hour = "*", minute = "*/10", second = "15", persistent = true)
    public void monitorInventory() {
        long started = System.nanoTime();
        boolean success = false;
        try {
            for (Inventory item : inventoryService.getAllForAutomation()) {
                if (item.getQuantity() <= item.getReorderLevel()) {
                    alertService.raise("warning", "INVENTORY", item.getSku(), "Low stock: " + item.getItemName(), item.getQuantity() + " units remain; reorder level is " + item.getReorderLevel() + ".");
                } else {
                    alertService.resolve("INVENTORY", item.getSku());
                }
            }
            success = true;
        } finally {
            performanceService.record("TIMER", "monitorInventory", elapsed(started), success);
        }
    }

    @Schedule(hour = "1", minute = "0", second = "0", persistent = true)
    public void evaluateVendorPerformance() {
        long started = System.nanoTime();
        boolean success = false;
        try {
            vendorService.recalculatePerformance();
            success = true;
        } finally {
            performanceService.record("TIMER", "evaluateVendorPerformance", elapsed(started), success);
        }
    }

    @Schedule(hour = "*", minute = "*/15", second = "30", persistent = true)
    public void optimizeRoutes() {
        long started = System.nanoTime();
        boolean success = false;
        try {
            routeOptimizationService.applyPriorities();
            success = true;
        } finally {
            performanceService.record("TIMER", "optimizeRoutes", elapsed(started), success);
        }
    }

    @Timeout
    public void handleProgrammaticTimer(Timer timer) {
        if (!CUSTOMS_TIMER.equals(String.valueOf(timer.getInfo()))) return;
        long started = System.nanoTime();
        boolean success = false;
        try {
            LocalDateTime warningTime = LocalDateTime.now().plusHours(24);
            List<CustomsDocument> urgent = customsService.dueBefore(warningTime);
            for (CustomsDocument document : urgent) {
                alertService.raise("warning", "CUSTOMS", document.getDocumentNumber(), "Customs deadline approaching", "Document " + document.getDocumentNumber() + " is due by " + document.getDeadline() + ".");
            }
            success = true;
        } finally {
            performanceService.record("TIMER", "customsDeadlineProgrammaticTimer", elapsed(started), success);
        }
    }

    public List<Map<String, Object>> timerSnapshots() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Timer timer : timerService.getTimers()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("info", String.valueOf(timer.getInfo()));
            try { item.put("nextTimeout", timer.getNextTimeout() == null ? null : timer.getNextTimeout().toInstant().toString()); } catch (Exception exception) { item.put("nextTimeout", null); }
            item.put("persistent", timer.isPersistent());
            result.add(item);
        }
        return result;
    }

    private long elapsed(long started) { return (System.nanoTime() - started) / 1_000_000L; }
}
