package com.nexcentauri.scms.rest.mapper;

import com.nexcentauri.scms.exception.InventoryOperationException;
import com.nexcentauri.scms.exception.ShipmentNotFoundException;
import com.nexcentauri.scms.exception.SupplyChainApplicationException;
import com.nexcentauri.scms.exception.VendorNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;

@Provider
public class SupplyChainExceptionMapper implements ExceptionMapper<SupplyChainApplicationException> {
    @Override
    public Response toResponse(SupplyChainApplicationException exception) {
        Response.Status status = exception instanceof ShipmentNotFoundException || exception instanceof VendorNotFoundException || exception instanceof InventoryOperationException && exception.getMessage() != null && exception.getMessage().contains("was not found")
                ? Response.Status.NOT_FOUND
                : Response.Status.BAD_REQUEST;
        return Response.status(status).entity(Map.of("success", false, "message", message(exception))).build();
    }

    private String message(Exception exception) {
        return exception.getMessage() == null || exception.getMessage().isBlank() ? "The requested supply chain operation could not be completed." : exception.getMessage();
    }
}
