package com.nexcentauri.scms.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "vendors")
public class Vendor implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false , unique = true)
    private String email;

    @Column(name = "performance_score")
    private Double performanceScore;

    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Shipment> shipments;

    public Vendor(){}

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}
    public Double getPerformanceScore() {return performanceScore;}
    public void setPerformanceScore(Double performanceScore) {this.performanceScore = performanceScore;}
    public List<Shipment> getShipments() {return shipments;}
    public void setShipments(List<Shipment> shipments) {this.shipments = shipments;}

}
