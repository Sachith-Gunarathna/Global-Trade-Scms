package com.nexcentauri.scms.service;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoutePriorityCalculatorTest {
    @Test
    void delayedShipmentReceivesHighPriority() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 1, 10, 0);
        int priority = RoutePriorityCalculator.calculate("DELAYED", now.plusHours(12), now);
        assertEquals(90, priority);
    }

    @Test
    void overdueShipmentIsCappedAtOneHundred() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 1, 10, 0);
        int priority = RoutePriorityCalculator.calculate("DELAYED", now.minusHours(2), now);
        assertEquals(100, priority);
    }

    @Test
    void normalFutureShipmentKeepsLowerPriority() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 1, 10, 0);
        int priority = RoutePriorityCalculator.calculate("IN_TRANSIT", now.plusDays(5), now);
        assertTrue(priority < 50);
    }
}
