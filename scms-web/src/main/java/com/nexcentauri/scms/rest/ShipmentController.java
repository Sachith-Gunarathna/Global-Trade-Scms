package com.nexcentauri.scms.rest;

import com.nexcentauri.scms.entity.Shipment;
import com.nexcentauri.scms.exception.SupplyChainApplicationException;
import com.nexcentauri.scms.rest.dto.ShipmentRequest;
import com.nexcentauri.scms.service.ShipmentService;
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
import java.util.List;

@Path("/shipments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ShipmentController {
    @EJB
    private ShipmentService shipmentService;

    @GET
    public List<java.util.Map<String, Object>> all() {
        return shipmentService.getAll().stream().map(ApiMapper::shipment).toList();
    }

    @GET
    @Path("/{id}")
    public java.util.Map<String, Object> one(@PathParam("id") Long id) throws SupplyChainApplicationException {
        return ApiMapper.shipment(shipmentService.get(id));
    }

    @POST
    public Response create(ShipmentRequest request) throws SupplyChainApplicationException {
        if (request == null) throw new SupplyChainApplicationException("Shipment details are required.");
        Shipment shipment = new Shipment();
        shipment.setTrackingNumber(request.getTrackingNumber());
        shipment.setOrigin(request.getOrigin());
        shipment.setDestination(request.getDestination());
        shipment.setStatus(request.getStatus());
        shipment.setEstimatedDeliveryDate(request.getEstimatedDeliveryDate());
        shipment.setCarrier(request.getCarrier());
        shipment.setVessel(request.getVessel());
        shipment.setProgress(request.getProgress());
        shipment.setDeclaredValue(request.getValue());
        shipment.setWeight(request.getWeight());
        return Response.status(Response.Status.CREATED).entity(ApiMapper.shipment(shipmentService.create(shipment, request.getVendorId()))).build();
    }

    @PUT
    @Path("/{id}/status")
    public Response status(@PathParam("id") Long id, @QueryParam("value") String value) throws SupplyChainApplicationException {
        return Response.ok(ApiMapper.shipment(shipmentService.updateStatus(id, value))).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) throws SupplyChainApplicationException {
        shipmentService.delete(id);
        return Response.noContent().build();
    }
}
