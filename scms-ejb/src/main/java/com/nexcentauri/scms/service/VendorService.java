package com.nexcentauri.scms.service;

import com.nexcentauri.scms.entity.Vendor;
import com.nexcentauri.scms.exception.VendorNotFoundException;
import com.nexcentauri.scms.interceptor.LogisticsAuditInterceptor;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
@Interceptors(LogisticsAuditInterceptor.class)
public class VendorService {

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager entityManager;

    @Resource
    private SessionContext sessionContext;

//    @RolesAllowed({"LOGISTICS_MANAGER","ADMIN"})
    @PermitAll
    public void addVendor(Vendor vendor){
        if(sessionContext.isCallerInRole("ADMIN")){
            vendor.setPerformanceScore(100.0);
        }
        entityManager.persist(vendor);
    }

    @PermitAll
    public Vendor getVendor(Long id) throws VendorNotFoundException {

        Vendor vendor = entityManager.find(Vendor.class, id);

        if(vendor == null){
            throw new VendorNotFoundException("Vendor with ID "+ id +" does not exist in the logistics database.");
        }
        return vendor;
    }

}
