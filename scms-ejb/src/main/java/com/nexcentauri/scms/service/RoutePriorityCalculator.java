package com.nexcentauri.scms.service;

import java.time.Duration;
import java.time.LocalDateTime;

public final class RoutePriorityCalculator {
    private RoutePriorityCalculator() {
    }

    public static int calculate(String status, LocalDateTime estimatedDeliveryDate, LocalDateTime now) {
        int priority = 20;
        if ("DELAYED".equals(status)) priority += 50;
        if ("CUSTOMS_HOLD".equals(status)) priority += 35;
        if (estimatedDeliveryDate != null) {
            long hours = Duration.between(now, estimatedDeliveryDate).toHours();
            if (hours <= 0) priority += 30;
            else if (hours <= 24) priority += 20;
            else if (hours <= 72) priority += 10;
        }
        return Math.min(100, priority);
    }
}
