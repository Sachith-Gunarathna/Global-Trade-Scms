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
@Table(name = "vendors")
public class Vendor implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 40)
    private String code;
    @Column(nullable = false, length = 160)
    private String name;
    @Column(nullable = false, unique = true, length = 160)
    private String email;
    @Column(length = 40)
    private String phone;
    @Column(length = 100)
    private String country;
    @Column(length = 100)
    private String region;
    @Column(length = 120)
    private String category;
    private Double rating;
    @Column(name = "performance_score")
    private Double performanceScore;
    @Column(name = "on_time_rate")
    private Double onTimeRate;
    @Column(name = "active_orders")
    private Integer activeOrders;
    @Column(nullable = false, length = 30)
    private String status;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Vendor() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public Double getPerformanceScore() { return performanceScore; }
    public void setPerformanceScore(Double performanceScore) { this.performanceScore = performanceScore; }
    public Double getOnTimeRate() { return onTimeRate; }
    public void setOnTimeRate(Double onTimeRate) { this.onTimeRate = onTimeRate; }
    public Integer getActiveOrders() { return activeOrders; }
    public void setActiveOrders(Integer activeOrders) { this.activeOrders = activeOrders; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
