package com.nexcentauri.scms.service;

import com.nexcentauri.scms.entity.CustomsDocument;
import com.nexcentauri.scms.entity.Inventory;
import com.nexcentauri.scms.entity.Shipment;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

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

    }

    @Schedule(hour = "*", minute = "*", second = "0", persistent = false)
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
                "SELECT c FROM CustomsDocument c WHERE c.status = 'PENDING' AND c.deadline < :warningTime")
                .setParameter("warningTime", warningTime)
                .getResultList();

        for (CustomsDocument doc : urgentDocs){
            LOGGER.warning("URGENT: Customs Document " + doc.getDocumentNumber() + " " +
                    "is approaching its deadline: " + doc.getDeadline());
        }
    }

}
