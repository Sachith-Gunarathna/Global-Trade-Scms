package com.nexcentauri.scms.service;

import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

import java.util.logging.Logger;

@Singleton
@Startup
public class LogisticsTimerService {

    private static final Logger logger = Logger.getLogger(LogisticsTimerService.class.getName());

    @Schedule(hour = "*", minute = "*", second = "0", persistent = false)
    public void monitorShipmentStatus(){
        logger.info("[TIMER TRIGGERED] Checking global shipment statuses and customs deadlines...");
    }

    @Schedule(hour = "23", minute = "59", persistent = true)
    public void evaluateVendorPerformance(){
        logger.info("[TIMER TRIGGERED] Evaluating daily vendor performance scores...");
    }

}
