package com.nexcentauri.scms.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "customs_documents")
public class CustomsDocument implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_number", unique = true, nullable = false)
    private String documentNumber;

    @Column(nullable = false)
    private String status;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id")
    private Shipment shipment;

    public CustomsDocument(){}

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public String getDocumentNumber() {return documentNumber;}
    public void setDocumentNumber(String documentNumber) {this.documentNumber = documentNumber;}
    public String getStatus() {return status;}
    public void setStatus(String status) {this.status = status;}
    public LocalDateTime getDeadline() {return deadline;}
    public void setDeadline(LocalDateTime deadline) {this.deadline = deadline;}
    public Shipment getShipment() {return shipment;}
    public void setShipment(Shipment shipment) {this.shipment = shipment;}

}
