package com.nexcentauri.scms.rest;

import com.nexcentauri.scms.entity.Vendor;
import com.nexcentauri.scms.exception.SupplyChainApplicationException;
import com.nexcentauri.scms.rest.dto.VendorRequest;
import com.nexcentauri.scms.security.AccessGuard;
import com.nexcentauri.scms.service.VendorService;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
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
import java.util.List;

@Path("/vendors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VendorController {
    @EJB
    private VendorService vendorService;

    @Inject
    private AccessGuard accessGuard;

    @GET
    public List<java.util.Map<String, Object>> all() {
        accessGuard.requireAnyRole("ADMIN", "LOGISTICS_COORDINATOR", "WAREHOUSE_MANAGER", "CUSTOMS_AGENT", "VENDOR_REP");
        return vendorService.getAll().stream().map(ApiMapper::vendor).toList();
    }

    @POST
    public Response create(VendorRequest request) throws SupplyChainApplicationException {
        accessGuard.requireAnyRole("ADMIN", "LOGISTICS_COORDINATOR", "WAREHOUSE_MANAGER");
        if (request == null) throw new SupplyChainApplicationException("Vendor details are required.");
        Vendor vendor = new Vendor();
        vendor.setCode(request.getCode());
        vendor.setName(request.getName());
        vendor.setEmail(request.getEmail());
        vendor.setPhone(request.getPhone());
        vendor.setCountry(request.getCountry());
        vendor.setRegion(request.getRegion());
        vendor.setCategory(request.getCategory());
        vendor.setRating(request.getRating());
        vendor.setPerformanceScore(request.getPerformanceScore());
        vendor.setOnTimeRate(request.getOnTimeRate());
        vendor.setActiveOrders(request.getActiveOrders());
        return Response.status(Response.Status.CREATED).entity(ApiMapper.vendor(vendorService.create(vendor))).build();
    }

    @PUT
    @Path("/{id}/score")
    public Response score(@PathParam("id") Long id, @QueryParam("value") Double value) throws SupplyChainApplicationException {
        accessGuard.requireAnyRole("ADMIN", "LOGISTICS_COORDINATOR");
        if (value == null) throw new SupplyChainApplicationException("A performance score is required.");
        return Response.ok(ApiMapper.vendor(vendorService.updateScore(id, value))).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) throws SupplyChainApplicationException {
        accessGuard.requireAnyRole("ADMIN", "LOGISTICS_COORDINATOR");
        vendorService.delete(id);
        return Response.noContent().build();
    }
}
