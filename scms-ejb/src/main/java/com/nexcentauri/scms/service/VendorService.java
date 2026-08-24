package com.nexcentauri.scms.service;

import com.nexcentauri.scms.entity.Vendor;
import com.nexcentauri.scms.exception.VendorNotFoundException;
import jakarta.annotation.Resource;
import jakarta.ejb.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.UserTransaction;

import java.util.List;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class VendorService {

    private static final String FIND_ALL_VENDORS_QUERY = "SELECT v FROM Vendor v";

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager entityManager;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Vendor createVendor(Vendor vendor) throws Exception{

            entityManager.persist(vendor);
            return vendor;

    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Vendor updateVendorScore(Long vendorId, Double newPerformanceScore) throws VendorNotFoundException{

            Vendor vendor = findVendorOrThrow(vendorId);
            vendor.setPerformanceScore(newPerformanceScore);
            return vendor;

    }

    public List<Vendor> getAllVendors() throws Exception{
       return entityManager.createQuery(FIND_ALL_VENDORS_QUERY, Vendor.class).getResultList();
    }

    private Vendor findVendorOrThrow(Long vendorId) throws VendorNotFoundException{
        Vendor vendor = entityManager.find(Vendor.class, vendorId);

        if(vendor == null){
            throw new VendorNotFoundException("Vendor with Id" + vendorId + " not found.");
        }

        return vendor;
    }

}
