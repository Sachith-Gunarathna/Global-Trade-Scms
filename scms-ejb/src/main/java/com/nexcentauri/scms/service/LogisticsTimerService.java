package com.nexcentauri.scms.service;

import com.nexcentauri.scms.entity.CustomsDocument;
import com.nexcentauri.scms.entity.Inventory;
import com.nexcentauri.scms.entity.Shipment;
import com.nexcentauri.scms.entity.Vendor;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@Singleton
@Startup
public class LogisticsTimerService {

    private static final Logger LOGGER = Logger.getLogger(LogisticsTimerService.class.getName());

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager entityManager;

    @Schedule(hour = "*", minute = "*", second = "0", persistent = false)
    public void performanceDailySupplyChainChecks(){
        LOGGER.info("--- Executing daily supply chain checks...  ---");

        checkDelayedShipments();
        checkInventoryLevels();
        checkCustomsDeadlines();
    }

    @Schedule(hour = "*", minute = "*", second = "30", persistent = false)
    public void monitorRealTimeShipmentStatus(){
        LOGGER.info("--- Executing daily supply chain checks...  ---");
        LOGGER.info("Real time tracking pinged successfully at: " + System.currentTimeMillis() + ".");
    }

    private void checkDelayedShipments(){
        LOGGER.info("Checking for delayed shipments past their expected delivery date...");

        LocalDateTime now = LocalDateTime.now();

        List<Shipment> delayedShipments = entityManager.createQuery(
                "SELECT s FROM Shipment s" +
                        " WHERE s.status NOT IN ('DELIVERD', 'DELAYED') " +
                        "AND s.estimatedDeliveryDate IS NOT NULL " +
                        "AND s.estimatedDeliveryDate < :now", Shipment.class)
                .setParameter("now",now)
                .getResultList();

        for(Shipment shipment : delayedShipments){
            LOGGER.warning("Shipment " + shipment.getTrachingNumber() + " is DELAYED. Updating status.");
            shipment.setStatus("DELAYED");
            entityManager.merge(shipment);
        }
    }

    private void checkInventoryLevels(){
        LOGGER.info("Monitoring inventory replenishment needs for items below reorder level...");

        List<Inventory> lowInventory = entityManager.createQuery(
                "SELECT i FROM Inventory i WHERE i.quantity <= i.reorderLevel", Inventory.class)
                .getResultList();

        for(Inventory item : lowInventory){
            LOGGER.warning("ALERT: Item '"+ item.getItemName() + "' " +
                    "is low on stock (Quantity: "+item.getQuantity()+"). Recorder required.");
        }
    }

    private void checkCustomsDeadlines(){
        LOGGER.info("Tracking customs documentation deadlines...");
        LocalDateTime warningTime  = LocalDateTime.now().plusDays(1);

        List<CustomsDocument> urgentDocs = entityManager.createQuery(
                "SELECT c FROM CustomsDocument c " +
                        "WHERE c.status = 'PENDING' " +
                        "AND c.deadline IS NOT NULL " +
                        "AND c.deadline <= :warningTime", CustomsDocument.class)
                .setParameter("warningTime", warningTime)
                .getResultList();

        for (CustomsDocument doc : urgentDocs){
            LOGGER.warning("URGENT: Customs Document " + doc.getDocumentNumber() + " " +
                    "is approaching its deadline: " + doc.getDeadline());
        }
    }

    private void evaluatedVendorPerformance(){

        LOGGER.info("Evaluating vendor performance...");

        List<Vendor> vendors = entityManager.createQuery("SELECT DISTINCT v FROM Vendor v " +
                "LEFT JOIN FETCH v.shipments", Vendor.class)
                .getResultList();

        for(Vendor vendor : vendors){

            List<Shipment> shipments = vendor.getShipments();

            if(shipments == null || shipments.isEmpty()){

                LOGGER.info("Vendor " + vendor.getName() + " has no shipments.");
                continue;
            }

            double totalScore = 0.0;

            for(Shipment shipment : shipments){

                String status = normalizeStatus(shipment.getStatus());

                switch (status){
                    case "DELIVERED":
                        totalScore += 100.0;
                        break;

                    case "IN_TRANSIT":
                        totalScore += 80.0;
                        break;
                    case "PENDING":
                        totalScore += 60.0;
                        break;
                    case "DELAYED":
                        totalScore += 40.0;
                        break;
                    default:
                        totalScore += 50.0;
                        break;
                }

            }

            double performanceScore = totalScore / shipments.size();

            performanceScore = Math.round(performanceScore * 100.0) / 100.0;

            vendor.setPerformanceScore(performanceScore);

            LOGGER.info("Vendor " + vendor.getName() + " has a performance score of: " + performanceScore +"%.");

        }

    }

    private void calculateRouteOptimizationPriority(){

        LOGGER.info("Calculating route optimization priority...");

        LocalDateTime now = LocalDateTime.now();

        List<Shipment> activeShipments = entityManager.createQuery(
                "SELECT s FROM Shipment  s " +
                        "WHERE UPPER(s.status) IN " +
                        "('IN TRANSIT', 'IN_TRANSIT','DELAYED')", Shipment.class
        ).getResultList();

        for(Shipment shipment : activeShipments){

            int priorityScore = 20;

            String status = normalizeStatus(shipment.getStatus());

            if("DELAYED".equals(status)){
                priorityScore += 50;
            }

            if(shipment.getEstimatedDeliveryDate() != null){

                long hoursRemaining = Duration.between(
                        now,
                        shipment.getEstimatedDeliveryDate()
                ).toHours();

                if(hoursRemaining <= 0){
                    priorityScore += 30;
                }else if(hoursRemaining <= 24){
                    priorityScore += 20;
                }else if(hoursRemaining <= 72){
                    priorityScore += 10;
                }

            }

            priorityScore = Math.min(priorityScore, 100);

            LOGGER.info("Route priority for shipment "
                    + shipment.getTrachingNumber()
                    + " = "
                    + priorityScore
                    + "/100 ("
                    + shipment.getOrigin()
                    + " -> "
                    + shipment.getDestination()
                    + ")");

        }

    }


    private String normalizeStatus(String status){

            if(status == null){
                return "";
            }

            return status
                    .trim()
                    .toUpperCase()
                    .replace(' ', '_');
    }
}

