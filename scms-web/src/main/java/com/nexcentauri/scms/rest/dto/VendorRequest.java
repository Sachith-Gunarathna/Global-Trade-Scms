package com.nexcentauri.scms.rest.dto;

public class VendorRequest {
    private String code;
    private String name;
    private String email;
    private String phone;
    private String country;
    private String region;
    private String category;
    private Double rating;
    private Double performanceScore;
    private Double onTimeRate;
    private Integer activeOrders;
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
}
