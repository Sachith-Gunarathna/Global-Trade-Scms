package com.nexcentauri.scms.service;

import jakarta.ejb.Stateless;
import java.time.LocalDateTime;

@Stateless
public class ContainerProbeBean {
    public int routePriority(String status, LocalDateTime eta, LocalDateTime now) {
        return RoutePriorityCalculator.calculate(status, eta, now);
    }
}
