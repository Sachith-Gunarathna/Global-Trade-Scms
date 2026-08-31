package com.nexcentauri.scms.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ShipmentRequest {
    private String trackingNumber;
    private String origin;
    private String destination;
    private String status;
    private LocalDateTime estimatedDeliveryDate;
    private String carrier;
    private String vessel;
    private Integer progress;
    private BigDecimal value;
    private String weight;
    private Long vendorId;
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getEstimatedDeliveryDate() { return estimatedDeliveryDate; }
    public void setEstimatedDeliveryDate(LocalDateTime estimatedDeliveryDate) { this.estimatedDeliveryDate = estimatedDeliveryDate; }
    public String getCarrier() { return carrier; }
    public void setCarrier(String carrier) { this.carrier = carrier; }
    public String getVessel() { return vessel; }
    public void setVessel(String vessel) { this.vessel = vessel; }
    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }
    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }
    public String getWeight() { return weight; }
    public void setWeight(String weight) { this.weight = weight; }
    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
}
