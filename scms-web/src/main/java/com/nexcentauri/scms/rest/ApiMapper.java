package com.nexcentauri.scms.rest;

import com.nexcentauri.scms.entity.CustomsDocument;
import com.nexcentauri.scms.entity.Inventory;
import com.nexcentauri.scms.entity.Shipment;
import com.nexcentauri.scms.entity.SystemUser;
import com.nexcentauri.scms.entity.TradeOrder;
import com.nexcentauri.scms.entity.Vendor;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ApiMapper {
    private ApiMapper() {
    }

    public static Map<String, Object> user(SystemUser user) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", user.getId());
        result.put("email", value(user.getEmail()));
        result.put("firstName", value(user.getFirstName()));
        result.put("lastName", value(user.getLastName()));
        result.put("phone", value(user.getMobileNumber()));
        result.put("mobileNumber", value(user.getMobileNumber()));
        result.put("organization", value(user.getOrganizationOrCompany()));
        result.put("organizationOrCompany", value(user.getOrganizationOrCompany()));
        result.put("hub", value(user.getPrimaryHub()));
        result.put("primaryHub", value(user.getPrimaryHub()));
        result.put("department", value(user.getDepartment()));
        result.put("role", value(user.getRole()));
        result.put("title", title(user.getRole()));
        return result;
    }

    public static Map<String, Object> vendor(Vendor vendor) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("databaseId", vendor.getId());
        result.put("id", value(vendor.getCode()));
        result.put("code", value(vendor.getCode()));
        result.put("name", value(vendor.getName()));
        result.put("email", value(vendor.getEmail()));
        result.put("phone", value(vendor.getPhone()));
        result.put("country", value(vendor.getCountry()));
        result.put("region", value(vendor.getRegion()));
        result.put("category", value(vendor.getCategory()));
        result.put("rating", number(vendor.getRating()));
        result.put("performanceScore", number(vendor.getPerformanceScore()));
        result.put("onTimeRate", number(vendor.getOnTimeRate()));
        result.put("activeOrders", vendor.getActiveOrders() == null ? 0 : vendor.getActiveOrders());
        result.put("status", title(vendor.getStatus()));
        return result;
    }

    public static Map<String, Object> inventory(Inventory item) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("databaseId", item.getId());
        result.put("sku", value(item.getSku()));
        result.put("name", value(item.getItemName()));
        result.put("category", value(item.getCategory()));
        result.put("stock", item.getQuantity() == null ? 0 : item.getQuantity());
        result.put("threshold", item.getReorderLevel() == null ? 0 : item.getReorderLevel());
        result.put("warehouse", value(item.getWarehouse()));
        BigDecimal unitValue = item.getUnitValue() == null ? BigDecimal.ZERO : item.getUnitValue();
        int quantity = item.getQuantity() == null ? 0 : item.getQuantity();
        result.put("value", unitValue.multiply(BigDecimal.valueOf(quantity)));
        result.put("unitValue", unitValue);
        result.put("capacity", item.getCapacity() == null ? 0 : item.getCapacity());
        if (item.getVendor() != null) {
            result.put("vendorId", item.getVendor().getId());
            result.put("vendorName", value(item.getVendor().getName()));
        }
        result.put("updatedAt", item.getUpdatedAt() == null ? null : item.getUpdatedAt().toString());
        return result;
    }

    public static Map<String, Object> shipment(Shipment shipment) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("databaseId", shipment.getId());
        result.put("id", value(shipment.getTrackingNumber()));
        result.put("trackingNumber", value(shipment.getTrackingNumber()));
        result.put("origin", value(shipment.getOrigin()));
        result.put("destination", value(shipment.getDestination()));
        result.put("status", title(shipment.getStatus()));
        result.put("eta", shipment.getEstimatedDeliveryDate() == null ? "" : shipment.getEstimatedDeliveryDate().toLocalDate().toString());
        result.put("estimatedDeliveryDate", shipment.getEstimatedDeliveryDate() == null ? null : shipment.getEstimatedDeliveryDate().toString());
        result.put("carrier", value(shipment.getCarrier()));
        result.put("vessel", value(shipment.getVessel()));
        result.put("progress", shipment.getProgress() == null ? 0 : shipment.getProgress());
        result.put("value", shipment.getDeclaredValue() == null ? BigDecimal.ZERO : shipment.getDeclaredValue());
        result.put("weight", value(shipment.getWeight()));
        result.put("updated", shipment.getUpdatedAt() == null ? "" : shipment.getUpdatedAt().toString());
        result.put("routePriority", shipment.getRoutePriority() == null ? 0 : shipment.getRoutePriority());
        if (shipment.getVendor() != null) {
            result.put("vendorId", shipment.getVendor().getId());
            result.put("vendorName", value(shipment.getVendor().getName()));
        }
        return result;
    }

    public static Map<String, Object> order(TradeOrder order) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("databaseId", order.getId());
        result.put("id", value(order.getOrderNumber()));
        result.put("orderNumber", value(order.getOrderNumber()));
        result.put("customer", value(order.getCustomer()));
        result.put("items", order.getItemCount() == null ? 0 : order.getItemCount());
        result.put("itemCount", order.getItemCount() == null ? 0 : order.getItemCount());
        result.put("total", order.getTotalAmount() == null ? BigDecimal.ZERO : order.getTotalAmount());
        result.put("totalAmount", order.getTotalAmount() == null ? BigDecimal.ZERO : order.getTotalAmount());
        result.put("status", title(order.getStatus()));
        result.put("date", order.getOrderDate() == null ? "" : order.getOrderDate().toString());
        result.put("orderDate", order.getOrderDate() == null ? null : order.getOrderDate().toString());
        result.put("region", value(order.getRegion()));
        result.put("contact", value(order.getContact()));
        if (order.getVendor() != null) {
            result.put("vendorId", order.getVendor().getId());
            result.put("vendorName", value(order.getVendor().getName()));
        }
        return result;
    }

    public static Map<String, Object> customs(CustomsDocument document) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", document.getId());
        result.put("documentNumber", value(document.getDocumentNumber()));
        result.put("documentType", title(document.getDocumentType()));
        result.put("status", title(document.getStatus()));
        result.put("deadline", document.getDeadline() == null ? null : document.getDeadline().toString());
        result.put("notes", value(document.getNotes()));
        if (document.getShipment() != null) {
            result.put("shipmentId", document.getShipment().getId());
            result.put("trackingNumber", value(document.getShipment().getTrackingNumber()));
            result.put("origin", value(document.getShipment().getOrigin()));
            result.put("destination", value(document.getShipment().getDestination()));
        }
        result.put("updatedAt", document.getUpdatedAt() == null ? null : document.getUpdatedAt().toString());
        return result;
    }

    private static Object number(Double value) {
        return value == null ? 0.0 : value;
    }

    private static String value(String value) {
        return value == null ? "" : value;
    }

    public static String title(String value) {
        if (value == null || value.isBlank()) return "";
        String[] parts = value.toLowerCase().replace('_', ' ').trim().split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (result.length() > 0) result.append(' ');
            result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return result.toString();
    }
}
