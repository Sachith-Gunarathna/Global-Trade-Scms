package com.nexcentauri.scms.rest;

import com.nexcentauri.scms.entity.CustomsDocument;
import com.nexcentauri.scms.exception.SupplyChainApplicationException;
import com.nexcentauri.scms.rest.dto.CustomsRequest;
import com.nexcentauri.scms.service.CustomsService;
import com.nexcentauri.scms.service.CustomsTransactionService;
import jakarta.ejb.EJB;
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

@Path("/customs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomsController {
    @EJB
    private CustomsService customsService;
    @EJB
    private CustomsTransactionService transactionService;

    @GET
    public List<java.util.Map<String, Object>> all() {
        return customsService.getAll().stream().map(ApiMapper::customs).toList();
    }

    @POST
    public Response create(CustomsRequest request) throws SupplyChainApplicationException {
        if (request == null) throw new SupplyChainApplicationException("Customs document details are required.");
        CustomsDocument document = new CustomsDocument();
        document.setDocumentNumber(request.getDocumentNumber());
        document.setDocumentType(request.getDocumentType());
        document.setStatus(request.getStatus());
        document.setDeadline(request.getDeadline());
        document.setNotes(request.getNotes());
        return Response.status(Response.Status.CREATED).entity(ApiMapper.customs(customsService.create(document, request.getShipmentId()))).build();
    }

    @PUT
    @Path("/{id}/approve")
    public Response approve(@PathParam("id") Long id, @QueryParam("notes") String notes) throws SupplyChainApplicationException {
        return Response.ok(ApiMapper.customs(customsService.approve(id, notes))).build();
    }

    @PUT
    @Path("/{id}/reject")
    public Response reject(@PathParam("id") Long id, @QueryParam("notes") String notes) throws SupplyChainApplicationException {
        return Response.ok(ApiMapper.customs(customsService.reject(id, notes))).build();
    }

    @POST
    @Path("/{id}/release")
    public Response release(@PathParam("id") Long id) throws SupplyChainApplicationException {
        return Response.ok(ApiMapper.customs(transactionService.releaseShipment(id))).build();
    }
}
