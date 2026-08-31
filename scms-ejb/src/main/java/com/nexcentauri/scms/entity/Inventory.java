package com.nexcentauri.scms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
public class Inventory implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 60)
    private String sku;
    @Column(name = "item_name", nullable = false, length = 180)
    private String itemName;
    @Column(length = 100)
    private String category;
    @Column(nullable = false)
    private Integer quantity;
    @Column(name = "reorder_level", nullable = false)
    private Integer reorderLevel;
    @Column(nullable = false, length = 160)
    private String warehouse;
    @Column(name = "unit_value", precision = 14, scale = 2)
    private BigDecimal unitValue;
    private Integer capacity;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Inventory() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Integer getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(Integer reorderLevel) { this.reorderLevel = reorderLevel; }
    public String getWarehouse() { return warehouse; }
    public void setWarehouse(String warehouse) { this.warehouse = warehouse; }
    public BigDecimal getUnitValue() { return unitValue; }
    public void setUnitValue(BigDecimal unitValue) { this.unitValue = unitValue; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public Vendor getVendor() { return vendor; }
    public void setVendor(Vendor vendor) { this.vendor = vendor; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
