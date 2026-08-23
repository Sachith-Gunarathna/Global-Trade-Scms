package com.nexcentauri.scms.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "shipments")
public class Shipment implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long trachingNumber;

    @Column(nullable = false)
    private String destination;

    @Column(nullable = false)
    private String status;

    @Column(name = "estimated_delivery_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date estimatedDeliveryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    public Shipment(){}

    public Long getTrachingNumber() {return trachingNumber;}
    public void setTrachingNumber(Long trachingNumber) {this.trachingNumber = trachingNumber;}
    public String getDestination() {return destination;}
    public void setDestination(String destination) {this.destination = destination;}
    public String getStatus() {return status;}
    public void setStatus(String status) {this.status = status;}
    public Date getEstimatedDeliveryDate() {return estimatedDeliveryDate;}
    public void setEstimatedDeliveryDate(Date estimatedDeliveryDate) {this.estimatedDeliveryDate = estimatedDeliveryDate;}
    public Vendor getVendor() {return vendor;}
    public void setVendor(Vendor vendor) {this.vendor = vendor;}

}
