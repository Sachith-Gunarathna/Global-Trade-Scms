package com.nexcentauri.scms.service;

import com.nexcentauri.scms.entity.Vendor;
import com.nexcentauri.scms.interceptor.LogisticsAuditInterceptor;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
@Interceptors(LogisticsAuditInterceptor.class)
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
