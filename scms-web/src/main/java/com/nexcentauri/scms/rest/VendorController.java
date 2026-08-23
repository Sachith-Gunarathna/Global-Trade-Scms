package com.nexcentauri.scms.rest;

import com.nexcentauri.scms.entity.Vendor;
import com.nexcentauri.scms.exception.VendorNotFoundException;
import com.nexcentauri.scms.service.VendorService;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.awt.*;

@Path("/vendors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VendorController {

    @EJB
    private VendorService vendorService;

    @POST
    public Response createVendor(Vendor vendor){
        vendorService.addVendor(vendor);
        return Response.status(Response.Status.CREATED).entity(vendor).build();
    }

    @GET
    @Path("/{id}")
    public Response getVendorById(@PathParam("id") Long id){
        try {
            Vendor vendor = vendorService.getVendor(id);
            return Response.ok(vendor).build();
        }catch (VendorNotFoundException e){
            String errorMessage = "{\"error\": \""+e.getMessage()+"\"}";
            return Response.status(Response.Status.NOT_FOUND).entity(errorMessage).build();
        }
    }

}
