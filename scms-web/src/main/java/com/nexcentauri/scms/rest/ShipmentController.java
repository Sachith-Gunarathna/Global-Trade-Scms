package com.nexcentauri.scms.rest;

import com.nexcentauri.scms.entity.Shipment;
import com.nexcentauri.scms.service.ShipmentService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/shipments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ShipmentController {

    @Inject
    private ShipmentService shipmentService;

    @GET
    public Response getAllShipments(){
        List<Shipment> shipments = shipmentService.getAllShipments();
        return Response.ok(shipments).build();
    }

    @GET
    @Path("/{id}")
    public Response getShipment(@PathParam("id") Long id){
        Shipment shipment = shipmentService.getShipment(id);
        if(shipment != null){
            return Response.ok(shipment).build();
        }

        return Response.status(Response.Status.NOT_FOUND).entity("Shipment not found").build();
    }

    @POST
    public Response createShipment(Shipment shipment){
        Shipment created = shipmentService.createShipment(shipment);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}/status")
    public Response updateShipmentStatus(@PathParam("id") Long id, @QueryParam("status") String status){
        Shipment update = shipmentService.updateShipmentStatus(id,status);
        if(update != null){
            return Response.ok(update).build();
        }
        return Response.status(Response.Status.NOT_FOUND).entity("Shipment not found").build();
    }

}
