package com.nexcentauri.scms.rest;

import com.nexcentauri.scms.entity.Inventory;
import com.nexcentauri.scms.exception.SupplyChainApplicationException;
import com.nexcentauri.scms.rest.dto.InventoryRequest;
import com.nexcentauri.scms.service.InventoryService;
import jakarta.ejb.EJB;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.List;

@Path("/inventory")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InventoryController {
    @EJB
    private InventoryService inventoryService;

    @GET
    public List<java.util.Map<String, Object>> all() {
        return inventoryService.getAll().stream().map(ApiMapper::inventory).toList();
    }

    @POST
    public Response create(InventoryRequest request) throws SupplyChainApplicationException {
        if (request == null) throw new SupplyChainApplicationException("Inventory details are required.");
        Inventory item = new Inventory();
        item.setSku(request.getSku());
        item.setItemName(request.getName());
        item.setCategory(request.getCategory());
        item.setQuantity(request.getStock());
        item.setReorderLevel(request.getThreshold());
        item.setWarehouse(request.getWarehouse());
        int quantity = request.getStock() == null ? 0 : request.getStock();
        BigDecimal totalValue = request.getValue() == null ? BigDecimal.ZERO : request.getValue();
        item.setUnitValue(quantity > 0 ? totalValue.divide(BigDecimal.valueOf(quantity), 2, java.math.RoundingMode.HALF_UP) : totalValue);
        item.setCapacity(request.getCapacity());
        return Response.status(Response.Status.CREATED).entity(ApiMapper.inventory(inventoryService.create(item, request.getVendorId()))).build();
    }

    @PUT
    @Path("/{id}/quantity")
    public Response quantity(@PathParam("id") Long id, @QueryParam("value") Integer value) throws SupplyChainApplicationException {
        if (value == null) throw new SupplyChainApplicationException("A quantity is required.");
        return Response.ok(ApiMapper.inventory(inventoryService.updateQuantity(id, value))).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) throws SupplyChainApplicationException {
        inventoryService.delete(id);
        return Response.noContent().build();
    }
}
