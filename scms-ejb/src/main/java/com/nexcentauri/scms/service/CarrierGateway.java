package com.nexcentauri.scms.service;

import com.nexcentauri.scms.exception.CarrierIntegrationException;
import jakarta.ejb.Local;
import java.util.Map;

@Local
public interface CarrierGateway {
    Map<String, Object> synchronizeActiveShipments();
    Map<String, Object> health();
    String pollShipment(Long shipmentId) throws CarrierIntegrationException;
}
