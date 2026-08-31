package com.nexcentauri.scms.exception;

public class CarrierIntegrationException extends SupplyChainApplicationException {
    public CarrierIntegrationException(String message) { super(message); }
    public CarrierIntegrationException(String message, Throwable cause) { super(message, cause); }
}
