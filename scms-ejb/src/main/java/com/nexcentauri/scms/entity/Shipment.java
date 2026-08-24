package com.nexcentauri.scms.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipments")
public class Shipment implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "traching_number", unique = true, nullable = false)
    private Long trachingNumber;

    @Column(nullable = false)
    private String destination;

    @Column(nullable = false)
    private String origin;

    @Column(nullable = false)
    private String status;

    @Column(name = "estimated_delivery_date")
    private LocalDateTime estimatedDeliveryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    public Shipment(){}

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public Long getTrachingNumber() {return trachingNumber;}
    public void setTrachingNumber(Long trachingNumber) {this.trachingNumber = trachingNumber;}
    public String getDestination() {return destination;}
    public void setDestination(String destination) {this.destination = destination;}
    public String getOrigin() {return origin;}
    public void setOrigin(String origin) {this.origin = origin;}
    public String getStatus() {return status;}
    public void setStatus(String status) {this.status = status;}
    public LocalDateTime getEstimatedDeliveryDate() {return estimatedDeliveryDate;}
    public void setEstimatedDeliveryDate(LocalDateTime estimatedDeliveryDate) {this.estimatedDeliveryDate = estimatedDeliveryDate;}
    public Vendor getVendor() {return vendor;}
    public void setVendor(Vendor vendor) {this.vendor = vendor;}

}
