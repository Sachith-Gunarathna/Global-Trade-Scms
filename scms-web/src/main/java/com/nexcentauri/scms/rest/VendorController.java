package com.nexcentauri.scms.rest;

import com.nexcentauri.scms.entity.Vendor;
import com.nexcentauri.scms.service.VendorService;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/vendors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VendorController {

    @EJB
    private VendorService vendorService;

    @GET
    public Response getAllVendors(){
        try {
            List<Vendor> vendors = vendorService.getAllVendors();
            return Response.ok(vendors).build();

        }catch (Exception e){
            return Response.serverError().entity("Error: "+ e.getMessage()).build();
        }
    }

    @POST
    public Response createVendor(Vendor vendor){
        try {
            Vendor created = vendorService.createVendor(vendor);
            return Response.status(Response.Status.CREATED).entity(created).build();
        }catch (Exception e){
            return Response.serverError().entity("Error: " + e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id}/score")
    public Response updateVendorScore(@PathParam("id") Long id, @QueryParam("score") Double score){
        try {
            Vendor update = vendorService.updateVendorScore(id,score);
            return Response.ok(update).build();
        }catch (Exception e){
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

}
