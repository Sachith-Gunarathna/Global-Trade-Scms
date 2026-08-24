package com.nexcentauri.scms.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action;

    @Column(name = "method_name")
    private String methodName;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "performed_by")
    private String performedBy;

    public AuditLog(){}

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public String getAction() {return action;}
    public void setAction(String action) {this.action = action;}
    public String getMethodName() {return methodName;}
    public void setMethodName(String methodName) {this.methodName = methodName;}
    public LocalDateTime getTimestamp() {return timestamp;}
    public void setTimestamp(LocalDateTime timestamp) {this.timestamp = timestamp;}
    public String getPerformedBy() {return performedBy;}
    public void setPerformedBy(String performedBy) {this.performedBy = performedBy;}

}
