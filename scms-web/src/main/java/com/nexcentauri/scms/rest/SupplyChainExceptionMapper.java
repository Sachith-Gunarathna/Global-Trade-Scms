package com.nexcentauri.scms.rest;

import com.nexcentauri.scms.exception.SupplyChainApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class SupplyChainExceptionMapper implements ExceptionMapper<SupplyChainApplicationException> {

    @Override
    public Response toResponse(SupplyChainApplicationException exception){

        String jsonError = String.format("{\"status\": \"error\", \"message\": \"%s\"}", exception.getMessage());

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(jsonError)
                .type("application/json")
                .build();

    }

}
