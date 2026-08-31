package com.nexcentauri.scms.rest.mapper;

import jakarta.ejb.EJBAccessException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;

@Provider
public class AccessDeniedMapper implements ExceptionMapper<EJBAccessException> {
    @Override
    public Response toResponse(EJBAccessException exception) {
        return Response.status(Response.Status.FORBIDDEN).entity(Map.of("success", false, "message", "You are not authorized to perform this operation.")).build();
    }
}
