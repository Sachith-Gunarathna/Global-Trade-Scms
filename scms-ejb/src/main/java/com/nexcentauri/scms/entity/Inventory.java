package com.nexcentauri.scms.entity;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "inventory")
public class Inventory implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_name",nullable = false)
    private String itemName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "reorder_level", nullable = false)
    private Integer reorderLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    public Inventory(){}

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public String getItemName() {return itemName;}
    public void setItemName(String itemName) {this.itemName = itemName;}
    public Integer getQuantity() {return quantity;}
    public void setQuantity(Integer quantity) {this.quantity = quantity;}
    public Integer getReorderLevel() {return reorderLevel;}
    public void setReorderLevel(Integer reorderLevel) {this.reorderLevel = reorderLevel;}
    public Vendor getVendor() {return vendor;}
    public void setVendor(Vendor vendor) {this.vendor = vendor;}

}
