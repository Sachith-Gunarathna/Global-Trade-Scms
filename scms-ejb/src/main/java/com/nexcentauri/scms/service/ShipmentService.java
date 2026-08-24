package com.nexcentauri.scms.service;

import com.nexcentauri.scms.entity.Shipment;
import jakarta.ejb.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
public class ShipmentService {

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager entityManager;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Shipment createShipment(Shipment shipment){
        entityManager.persist(shipment);
        return  shipment;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Shipment updateShipmentStatus(Long shipmentId, String newStatus){
        Shipment shipment = entityManager.find(Shipment.class, shipmentId);
        if(shipment != null){
            shipment.setStatus(newStatus);
            entityManager.merge(shipment);
        }

        return shipment;
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Shipment getShipment(Long id){
        return entityManager.find(Shipment.class, id);
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Shipment> getAllShipments(){
        return entityManager.createQuery("SELECT s FROM Shipment s", Shipment.class).getResultList();
    }
}

