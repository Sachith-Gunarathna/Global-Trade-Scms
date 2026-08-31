package com.nexcentauri.scms.service;

import com.nexcentauri.scms.entity.Inventory;
import com.nexcentauri.scms.entity.Vendor;
import com.nexcentauri.scms.exception.InventoryOperationException;
import com.nexcentauri.scms.exception.SupplyChainApplicationException;
import com.nexcentauri.scms.interceptor.binding.AuditTrail;
import com.nexcentauri.scms.interceptor.binding.Monitored;
import jakarta.annotation.security.DeclareRoles;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
@DeclareRoles({"ADMIN", "LOGISTICS_COORDINATOR", "WAREHOUSE_MANAGER", "CUSTOMS_AGENT", "VENDOR_REP"})
@AuditTrail
@Monitored
public class InventoryService {
    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager entityManager;
    @EJB
    private VendorService vendorService;

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Inventory> getAll() {
        return entityManager.createQuery("SELECT i FROM Inventory i LEFT JOIN FETCH i.vendor ORDER BY i.itemName", Inventory.class).getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Inventory> getAllForAutomation() {
        return entityManager.createQuery("SELECT i FROM Inventory i LEFT JOIN FETCH i.vendor ORDER BY i.itemName", Inventory.class).getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Inventory> getAllForVendor(Long vendorId) {
        return entityManager.createQuery("SELECT i FROM Inventory i JOIN FETCH i.vendor WHERE i.vendor.id = :vendorId ORDER BY i.itemName", Inventory.class)
                .setParameter("vendorId", vendorId)
                .getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Inventory create(Inventory item, Long vendorId) throws SupplyChainApplicationException {
        validate(item);
        if (findBySku(item.getSku()) != null) throw new InventoryOperationException("SKU " + item.getSku() + " already exists.");
        if (vendorId != null) {
            Vendor vendor = vendorService.require(vendorId);
            item.setVendor(vendor);
        }
        item.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(item);
        return item;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Inventory updateQuantity(Long id, int quantity) throws InventoryOperationException {
        if (quantity < 0) throw new InventoryOperationException("Inventory quantity cannot be negative.");
        Inventory item = require(id);
        item.setQuantity(quantity);
        item.setUpdatedAt(LocalDateTime.now());
        return item;
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void reserveStock(Long id, int quantity) throws InventoryOperationException {
        if (quantity <= 0) throw new InventoryOperationException("Reservation quantity must be greater than zero.");
        Inventory item = require(id);
        if (item.getQuantity() < quantity) throw new InventoryOperationException("Insufficient stock for SKU " + item.getSku() + ".");
        item.setQuantity(item.getQuantity() - quantity);
        item.setUpdatedAt(LocalDateTime.now());
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void delete(Long id) throws InventoryOperationException {
        entityManager.remove(require(id));
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Inventory require(Long id) throws InventoryOperationException {
        Inventory item = entityManager.find(Inventory.class, id);
        if (item == null) throw new InventoryOperationException("Inventory record with ID " + id + " was not found.");
        return item;
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Inventory> lowStock() {
        return entityManager.createQuery("SELECT i FROM Inventory i WHERE i.quantity <= i.reorderLevel ORDER BY i.quantity", Inventory.class).getResultList();
    }

    private Inventory findBySku(String sku) {
        try {
            return entityManager.createQuery("SELECT i FROM Inventory i WHERE i.sku = :sku", Inventory.class).setParameter("sku", sku).getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    private void validate(Inventory item) throws InventoryOperationException {
        if (item == null) throw new InventoryOperationException("Inventory item is required.");
        if (item.getSku() == null || item.getSku().isBlank()) throw new InventoryOperationException("SKU is required.");
        if (item.getItemName() == null || item.getItemName().isBlank()) throw new InventoryOperationException("Item name is required.");
        if (item.getQuantity() == null || item.getQuantity() < 0) throw new InventoryOperationException("Quantity must be zero or greater.");
        if (item.getReorderLevel() == null || item.getReorderLevel() < 0) throw new InventoryOperationException("Reorder level must be zero or greater.");
        if (item.getWarehouse() == null || item.getWarehouse().isBlank()) throw new InventoryOperationException("Warehouse is required.");
    }
}
