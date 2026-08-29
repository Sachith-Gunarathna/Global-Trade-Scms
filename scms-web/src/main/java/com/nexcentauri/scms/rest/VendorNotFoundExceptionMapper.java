package com.nexcentauri.scms.rest;

import com.nexcentauri.scms.exception.VendorNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class VendorNotFoundExceptionMapper implements ExceptionMapper<VendorNotFoundException> {

    @Override
    public Response toResponse(VendorNotFoundException exception){
        String jsonResponse = String.format("{\"success\": false, \"error\": \"%s\"}", exception.getMessage());

        return Response.status(Response.Status.NOT_FOUND)
                .entity(jsonResponse)
                .type("application/json")
                .build();
    }

}
