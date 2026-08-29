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

    @Resource
    private SessionContext sessionContext;



    public Vendor createVendor(Vendor vendor) throws Exception{

        UserTransaction userTransaction = sessionContext.getUserTransaction();

        try {
            userTransaction.begin();
            entityManager.persist(vendor);
            userTransaction.commit();
            return vendor;

        }catch (Exception e){
            userTransaction.rollback();
            throw new Exception("Error creating vendor: " + e.getMessage(), e);
        }


    }

    public Vendor updateVendorScore(Long vendorId, Double newPerformanceScore) throws Exception{
        UserTransaction userTransaction = sessionContext.getUserTransaction();

        try {
            userTransaction.begin();

            Vendor vendor = findVendorOrThrow(vendorId);
            vendor.setPerformanceScore(newPerformanceScore);
            entityManager.merge(vendor);

            userTransaction.commit();
            return vendor;
        }catch (VendorNotFoundException e){
            userTransaction.rollback();
            throw e;
        }catch (Exception e){
            userTransaction.rollback();
            throw new VendorNotFoundException("Vendor with Id " + vendorId + " not found.");
        }


    }

    public List<Vendor> getAllVendors() throws Exception{
        UserTransaction userTransaction = sessionContext.getUserTransaction();

        try {
            userTransaction.begin();
            List<Vendor> vendors = entityManager.createQuery(FIND_ALL_VENDORS_QUERY,Vendor.class).getResultList();
            userTransaction.commit();
            return vendors;
        }catch (Exception e){
            userTransaction.rollback();
            throw e;
        }
    }

    private Vendor findVendorOrThrow(Long vendorId) throws VendorNotFoundException{
        Vendor vendor = entityManager.find(Vendor.class, vendorId);

        if(vendor == null){
            throw new VendorNotFoundException("Vendor with Id" + vendorId + " not found.");
        }

        return vendor;
    }

}
