package com.nexcentauri.scms.service;

import com.nexcentauri.scms.entity.TradeOrder;
import com.nexcentauri.scms.entity.Vendor;
import com.nexcentauri.scms.exception.OrderProcessingException;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Stateless
@DeclareRoles({"ADMIN", "LOGISTICS_COORDINATOR", "WAREHOUSE_MANAGER", "VENDOR_REP"})
@AuditTrail
@Monitored
public class OrderService {
    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager entityManager;
    @EJB
    private VendorService vendorService;

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<TradeOrder> getAll() {
        return entityManager.createQuery("SELECT o FROM TradeOrder o LEFT JOIN FETCH o.vendor ORDER BY o.createdAt DESC", TradeOrder.class).getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<TradeOrder> getAllForVendor(Long vendorId) {
        return entityManager.createQuery("SELECT o FROM TradeOrder o JOIN FETCH o.vendor WHERE o.vendor.id = :vendorId ORDER BY o.createdAt DESC", TradeOrder.class)
                .setParameter("vendorId", vendorId)
                .getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public TradeOrder create(TradeOrder order, Long vendorId) throws SupplyChainApplicationException {
        validate(order);
        if (findByNumber(order.getOrderNumber()) != null) throw new OrderProcessingException("Order number already exists.");
        if (vendorId != null) {
            Vendor vendor = vendorService.require(vendorId);
            order.setVendor(vendor);
            vendor.setActiveOrders((vendor.getActiveOrders() == null ? 0 : vendor.getActiveOrders()) + 1);
        }
        order.setStatus(normalizeStatus(order.getStatus()));
        if (order.getOrderDate() == null) order.setOrderDate(LocalDate.now());
        order.setCreatedAt(LocalDateTime.now());
        entityManager.persist(order);
        return order;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public TradeOrder updateStatus(Long id, String status) throws OrderProcessingException {
        TradeOrder order = entityManager.find(TradeOrder.class, id);
        if (order == null) throw new OrderProcessingException("Order was not found.");
        order.setStatus(normalizeStatus(status));
        return order;
    }

    private TradeOrder findByNumber(String number) {
        try {
            return entityManager.createQuery("SELECT o FROM TradeOrder o WHERE o.orderNumber = :number", TradeOrder.class).setParameter("number", number).getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    private void validate(TradeOrder order) throws OrderProcessingException {
        if (order == null) throw new OrderProcessingException("Order details are required.");
        if (order.getOrderNumber() == null || order.getOrderNumber().isBlank()) throw new OrderProcessingException("Order number is required.");
        if (order.getCustomer() == null || order.getCustomer().isBlank()) throw new OrderProcessingException("Customer name is required.");
        if (order.getItemCount() == null || order.getItemCount() <= 0) throw new OrderProcessingException("Item count must be greater than zero.");
        if (order.getTotalAmount() == null || order.getTotalAmount().signum() < 0) throw new OrderProcessingException("Order total must be zero or greater.");
    }

    private String normalizeStatus(String status) {
        return status == null || status.isBlank() ? "PENDING" : status.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
    }
}
