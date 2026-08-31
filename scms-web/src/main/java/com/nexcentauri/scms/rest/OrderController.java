package com.nexcentauri.scms.rest;

import com.nexcentauri.scms.entity.SystemUser;
import com.nexcentauri.scms.entity.TradeOrder;
import com.nexcentauri.scms.entity.Vendor;
import com.nexcentauri.scms.exception.SupplyChainApplicationException;
import com.nexcentauri.scms.rest.dto.OrderRequest;
import com.nexcentauri.scms.security.AccessGuard;
import com.nexcentauri.scms.service.OrderService;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderController {
    @EJB
    private OrderService orderService;

    @Inject
    private AccessGuard accessGuard;

    @GET
    public List<Map<String, Object>> all() {
        SystemUser user = accessGuard.requireAnyRole("ADMIN", "LOGISTICS_COORDINATOR", "WAREHOUSE_MANAGER", "VENDOR_REP");
        if (accessGuard.isVendorRepresentative(user)) {
            Vendor vendor = accessGuard.requireRepresentativeVendor(user);
            return orderService.getAllForVendor(vendor.getId()).stream().map(ApiMapper::order).toList();
        }
        return orderService.getAll().stream().map(ApiMapper::order).toList();
    }

    @POST
    public Response create(OrderRequest request) throws SupplyChainApplicationException {
        accessGuard.requireAnyRole("ADMIN", "LOGISTICS_COORDINATOR");
        if (request == null) throw new SupplyChainApplicationException("Order details are required.");
        TradeOrder order = new TradeOrder();
        order.setOrderNumber(request.getOrderNumber());
        order.setCustomer(request.getCustomer());
        order.setItemCount(request.getItemCount());
        order.setTotalAmount(request.getTotalAmount());
        order.setStatus(request.getStatus());
        order.setOrderDate(request.getOrderDate());
        order.setRegion(request.getRegion());
        order.setContact(request.getContact());
        return Response.status(Response.Status.CREATED).entity(ApiMapper.order(orderService.create(order, request.getVendorId()))).build();
    }

    @PUT
    @Path("/{id}/status")
    public Response status(@PathParam("id") Long id, @QueryParam("value") String value) throws SupplyChainApplicationException {
        accessGuard.requireAnyRole("ADMIN", "LOGISTICS_COORDINATOR");
        return Response.ok(ApiMapper.order(orderService.updateStatus(id, value))).build();
    }
}
