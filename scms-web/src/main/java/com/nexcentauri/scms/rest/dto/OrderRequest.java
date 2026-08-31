package com.nexcentauri.scms.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class OrderRequest {
    private String orderNumber;
    private String customer;
    private Integer itemCount;
    private BigDecimal totalAmount;
    private String status;
    private LocalDate orderDate;
    private String region;
    private String contact;
    private Long vendorId;
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public String getCustomer() { return customer; }
    public void setCustomer(String customer) { this.customer = customer; }
    public Integer getItemCount() { return itemCount; }
    public void setItemCount(Integer itemCount) { this.itemCount = itemCount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
}
