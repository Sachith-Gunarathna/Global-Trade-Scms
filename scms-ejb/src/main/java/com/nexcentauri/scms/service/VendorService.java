package com.nexcentauri.scms.service;

import com.nexcentauri.scms.entity.Vendor;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class VendorService {

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager entityManager;

    public void addVendor(Vendor vendor){
        entityManager.persist(vendor);
    }

    public Vendor getVendor(Long id){
        return entityManager.find(Vendor.class,id);
    }

}
