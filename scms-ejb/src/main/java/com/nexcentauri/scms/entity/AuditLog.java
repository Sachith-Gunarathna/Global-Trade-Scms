package com.nexcentauri.scms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 30)
    private String action;
    @Column(name = "method_name", nullable = false, length = 160)
    private String methodName;
    @Column(name = "component_name", length = 120)
    private String componentName;
    @Column(nullable = false)
    private LocalDateTime timestamp;
    @Column(name = "performed_by", length = 160)
    private String performedBy;
    @Column(name = "duration_ms")
    private Long durationMs;
    @Column(nullable = false)
    private Boolean success;
    @Column(length = 500)
    private String detail;

    public AuditLog() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getMethodName() { return methodName; }
    public void setMethodName(String methodName) { this.methodName = methodName; }
    public String getComponentName() { return componentName; }
    public void setComponentName(String componentName) { this.componentName = componentName; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
}
