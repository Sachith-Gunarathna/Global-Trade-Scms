package com.nexcentauri.scms.service;

import com.nexcentauri.scms.entity.Shipment;
import com.nexcentauri.scms.interceptor.LogisticsAuditInterceptor;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.*;
import jakarta.interceptor.Interceptors;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
@Interceptors(LogisticsAuditInterceptor.class)

@DeclareRoles({"LOGISTICS_PERSONNEL", "VENDOR", "CUSTOMS_OFFICIAL"})
public class ShipmentService {

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager entityManager;

    @RolesAllowed({"LOGISTICS_PERSONNEL"})
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Shipment createShipment(Shipment shipment){
        entityManager.persist(shipment);
        return  shipment;
    }

    @RolesAllowed({"LOGISTICS_PERSONNEL","CUSTOMS_OFFICIAL"})
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Shipment updateShipmentStatus(Long shipmentId, String newStatus){
        Shipment shipment = entityManager.find(Shipment.class, shipmentId);
        if(shipment != null){
            shipment.setStatus(newStatus);
            entityManager.merge(shipment);
        }

        return shipment;
    }

    @RolesAllowed({"LOGISTICS_PERSONNEL","VENDOR","CUSTOMS_OFFICIAL"})
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Shipment getShipment(Long id){
        return entityManager.find(Shipment.class, id);
    }

    @RolesAllowed({"LOGISTICS_PERSONNEL","VENDOR","CUSTOMS_OFFICIAL"})
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Shipment> getAllShipments(){
        return entityManager.createQuery("SELECT s FROM Shipment s", Shipment.class).getResultList();
    }
}

