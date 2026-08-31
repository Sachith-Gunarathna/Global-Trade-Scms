package com.nexcentauri.scms.rest.mapper;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {
    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof WebApplicationException webException) {
            int status = webException.getResponse().getStatus();
            String message = exception.getMessage();
            if (message == null || message.isBlank()) {
                message = status == 401 ? "Authentication is required." : status == 403 ? "You are not authorized to perform this operation." : "The request could not be completed.";
            }
            return Response.status(status).entity(Map.of("success", false, "message", message)).build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Map.of("success", false, "message", "An unexpected server error occurred.")).build();
    }
}
