package com.nexcentauri.scms.service;

import com.nexcentauri.scms.entity.AuditLog;
import com.nexcentauri.scms.entity.CustomsDocument;
import com.nexcentauri.scms.entity.Inventory;
import com.nexcentauri.scms.entity.Shipment;
import com.nexcentauri.scms.entity.TradeOrder;
import com.nexcentauri.scms.entity.Vendor;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Singleton
@Startup
public class DataBootstrapService {
    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager entityManager;
    @EJB
    private AuthService authService;

    @PostConstruct
    public void initialize() {
        seedUsers();
        Long vendorCount = entityManager.createQuery("SELECT COUNT(v) FROM Vendor v", Long.class).getSingleResult();
        if (vendorCount == 0L) seedOperationalData();
    }

    private void seedUsers() {
        String password = System.getenv().getOrDefault("SCMS_BOOTSTRAP_PASSWORD", "Scms@1234");
        authService.createBootstrapUser("Alex", "Grant", "admin@globaltrade.lk", "ADMIN", password);
        authService.createBootstrapUser("Nimal", "Perera", "logistics@globaltrade.lk", "LOGISTICS_COORDINATOR", password);
        authService.createBootstrapUser("Sanduni", "Fernando", "warehouse@globaltrade.lk", "WAREHOUSE_MANAGER", password);
        authService.createBootstrapUser("Tharindu", "Silva", "customs@globaltrade.lk", "CUSTOMS_AGENT", password);
        authService.createBootstrapUser("Kamal", "Jayawardena", "vendor@globaltrade.lk", "VENDOR_REP", password);
    }

    private void seedOperationalData() {
        Vendor v1 = vendor("SUP-101", "Lanka Industrial Fabrications PLC", "supply@lankafabrications.lk", "+94 11 289 4020", "Sri Lanka", "South Asia", "Industrial Manufacturing", 4.8, 96.4, 4);
        Vendor v2 = vendor("SUP-102", "Tokyo Precision Robotics Ltd", "export@tokyorobotics.jp", "+81 3 5555 0192", "Japan", "East Asia", "Electronics & Sensors", 4.9, 98.2, 2);
        Vendor v3 = vendor("SUP-103", "Rhein Cargo Components GmbH", "trade@rheincomponents.de", "+49 40 555 1040", "Germany", "Europe", "Industrial Manufacturing", 4.6, 94.5, 3);
        entityManager.persist(v1);
        entityManager.persist(v2);
        entityManager.persist(v3);

        Inventory i1 = inventory("ELX-4201", "GPS Telematics Fleet Tracker Sensors", "Electronics", 240, 60, "Colombo Central Distribution Center", 150, 400, v2);
        Inventory i2 = inventory("PKG-6112", "High-Density Reinforced Container Pallets", "Packaging", 42, 50, "Hambantota Port Free Zone DC", 80, 350, v1);
        Inventory i3 = inventory("MCH-3055", "Hydraulic Dock Loading Assemblies", "Industrial", 18, 20, "Colombo Port Warehouse 03", 1250, 80, v3);
        Inventory i4 = inventory("AGR-2208", "Export Grade Tea Packaging Crates", "Agriculture", 410, 100, "Kandy Regional Depot", 45, 600, v1);
        entityManager.persist(i1);
        entityManager.persist(i2);
        entityManager.persist(i3);
        entityManager.persist(i4);

        Shipment s1 = shipment("SHP-78421", "Colombo, LK", "Rotterdam, NL", "IN_TRANSIT", LocalDateTime.now().plusDays(5), "Maersk", "Maersk Colombo Express", 42, 195000, "16.5 t", v1);
        Shipment s2 = shipment("SHP-78422", "Shanghai, CN", "Colombo, LK", "DELAYED", LocalDateTime.now().minusHours(10), "MSC", "MSC Aurora", 67, 286000, "22.0 t", v2);
        Shipment s3 = shipment("SHP-78423", "Hamburg, DE", "Colombo, LK", "PENDING", LocalDateTime.now().plusDays(8), "Hapag-Lloyd", "Berlin Express", 10, 132000, "11.2 t", v3);
        Shipment s4 = shipment("SHP-78424", "Colombo, LK", "Singapore, SG", "DELIVERED", LocalDateTime.now().minusDays(1), "CMA CGM", "CMA CGM Lanka", 100, 98000, "8.4 t", v1);
        entityManager.persist(s1);
        entityManager.persist(s2);
        entityManager.persist(s3);
        entityManager.persist(s4);

        TradeOrder o1 = order("ORD-9281", "Ceylon Export Holdings PLC", 8, 38400, "PROCESSING", LocalDate.now(), "Western Province", "procurement@ceylonexports.lk", v1);
        TradeOrder o2 = order("ORD-9282", "Lanka Industrial Distribution Ltd", 12, 54000, "PENDING", LocalDate.now().minusDays(1), "Central Province", "orders@lankaindustrial.lk", v3);
        TradeOrder o3 = order("ORD-9283", "Oceanic Retail Network", 5, 22750, "SHIPPED", LocalDate.now().minusDays(2), "Southern Province", "cargo@oceanic.lk", v2);
        entityManager.persist(o1);
        entityManager.persist(o2);
        entityManager.persist(o3);

        CustomsDocument c1 = customs("CUS-EX-5001", "EXPORT_DECLARATION", "PENDING", LocalDateTime.now().plusHours(12), "Awaiting final port inspection", s1);
        CustomsDocument c2 = customs("CUS-IM-5002", "IMPORT_DECLARATION", "PENDING", LocalDateTime.now().plusHours(20), "Commercial invoice verification pending", s2);
        CustomsDocument c3 = customs("CUS-TR-5003", "TRANSIT_PERMIT", "APPROVED", LocalDateTime.now().plusDays(3), "Transit permit approved", s3);
        entityManager.persist(c1);
        entityManager.persist(c2);
        entityManager.persist(c3);

        AuditLog log = new AuditLog();
        log.setAction("BOOTSTRAP");
        log.setComponentName("DataBootstrapService");
        log.setMethodName("initialize");
        log.setTimestamp(LocalDateTime.now());
        log.setPerformedBy("SYSTEM");
        log.setDurationMs(0L);
        log.setSuccess(true);
        log.setDetail("Sample logistics records initialized");
        entityManager.persist(log);
    }

    private Vendor vendor(String code, String name, String email, String phone, String country, String region, String category, double rating, double onTimeRate, int activeOrders) {
        Vendor vendor = new Vendor();
        vendor.setCode(code);
        vendor.setName(name);
        vendor.setEmail(email);
        vendor.setPhone(phone);
        vendor.setCountry(country);
        vendor.setRegion(region);
        vendor.setCategory(category);
        vendor.setRating(rating);
        vendor.setPerformanceScore(rating * 20.0);
        vendor.setOnTimeRate(onTimeRate);
        vendor.setActiveOrders(activeOrders);
        vendor.setStatus("ACTIVE");
        vendor.setCreatedAt(LocalDateTime.now());
        return vendor;
    }

    private Inventory inventory(String sku, String name, String category, int quantity, int reorder, String warehouse, double value, int capacity, Vendor vendor) {
        Inventory item = new Inventory();
        item.setSku(sku);
        item.setItemName(name);
        item.setCategory(category);
        item.setQuantity(quantity);
        item.setReorderLevel(reorder);
        item.setWarehouse(warehouse);
        item.setUnitValue(BigDecimal.valueOf(value));
        item.setCapacity(capacity);
        item.setVendor(vendor);
        item.setUpdatedAt(LocalDateTime.now());
        return item;
    }

    private Shipment shipment(String tracking, String origin, String destination, String status, LocalDateTime eta, String carrier, String vessel, int progress, double value, String weight, Vendor vendor) {
        Shipment shipment = new Shipment();
        shipment.setTrackingNumber(tracking);
        shipment.setOrigin(origin);
        shipment.setDestination(destination);
        shipment.setStatus(status);
        shipment.setEstimatedDeliveryDate(eta);
        shipment.setCarrier(carrier);
        shipment.setVessel(vessel);
        shipment.setProgress(progress);
        shipment.setDeclaredValue(BigDecimal.valueOf(value));
        shipment.setWeight(weight);
        shipment.setRoutePriority("DELAYED".equals(status) ? 85 : 30);
        shipment.setVendor(vendor);
        shipment.setCreatedAt(LocalDateTime.now());
        shipment.setUpdatedAt(LocalDateTime.now());
        return shipment;
    }

    private TradeOrder order(String number, String customer, int items, double total, String status, LocalDate date, String region, String contact, Vendor vendor) {
        TradeOrder order = new TradeOrder();
        order.setOrderNumber(number);
        order.setCustomer(customer);
        order.setItemCount(items);
        order.setTotalAmount(BigDecimal.valueOf(total));
        order.setStatus(status);
        order.setOrderDate(date);
        order.setRegion(region);
        order.setContact(contact);
        order.setVendor(vendor);
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }

    private CustomsDocument customs(String number, String type, String status, LocalDateTime deadline, String notes, Shipment shipment) {
        CustomsDocument document = new CustomsDocument();
        document.setDocumentNumber(number);
        document.setDocumentType(type);
        document.setStatus(status);
        document.setDeadline(deadline);
        document.setNotes(notes);
        document.setShipment(shipment);
        document.setCreatedAt(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());
        return document;
    }
}
