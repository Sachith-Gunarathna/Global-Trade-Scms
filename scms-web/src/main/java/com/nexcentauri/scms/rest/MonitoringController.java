package com.nexcentauri.scms.rest;

import com.nexcentauri.scms.exception.SupplyChainApplicationException;
import com.nexcentauri.scms.security.AccessGuard;
import com.nexcentauri.scms.service.DisruptionRecoveryService;
import com.nexcentauri.scms.service.MonitoringService;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("/monitoring")
@Produces(MediaType.APPLICATION_JSON)
public class MonitoringController {
    @EJB
    private MonitoringService monitoringService;

    @EJB
    private DisruptionRecoveryService disruptionRecoveryService;

    @Inject
    private AccessGuard accessGuard;

    @GET
    public Map<String, Object> monitoring() {
        accessGuard.requireAnyRole("ADMIN", "LOGISTICS_COORDINATOR", "WAREHOUSE_MANAGER", "CUSTOMS_AGENT");
        return monitoringService.snapshot();
    }

    @GET
    @Path("/routes")
    public Map<Long, Integer> routePriorities() {
        accessGuard.requireAnyRole("ADMIN", "LOGISTICS_COORDINATOR", "WAREHOUSE_MANAGER", "CUSTOMS_AGENT");
        return monitoringService.routePriorities();
    }

    @POST
    @Path("/routes/apply")
    public Map<String, Object> applyRoutes() {
        accessGuard.requireAnyRole("ADMIN", "LOGISTICS_COORDINATOR");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("updated", monitoringService.applyRoutePriorities());
        return result;
    }

    @GET
    @Path("/timers")
    public Object timers() {
        accessGuard.requireAnyRole("ADMIN", "LOGISTICS_COORDINATOR", "WAREHOUSE_MANAGER", "CUSTOMS_AGENT");
        return monitoringService.timers();
    }

    @GET
    @Path("/integrations")
    public Map<String, Object> integrations() {
        accessGuard.requireAnyRole("ADMIN", "LOGISTICS_COORDINATOR", "WAREHOUSE_MANAGER", "CUSTOMS_AGENT");
        return monitoringService.carrierIntegration();
    }

    @POST
    @Path("/integrations/carriers/sync")
    public Map<String, Object> synchronizeCarriers() {
        accessGuard.requireAnyRole("ADMIN", "LOGISTICS_COORDINATOR");
        return monitoringService.synchronizeCarriers();
    }

    @POST
    @Path("/disruptions/weather/{shipmentId}")
    public Map<String, Object> reportWeather(@PathParam("shipmentId") Long shipmentId, @QueryParam("severity") String severity) throws SupplyChainApplicationException {
        accessGuard.requireAnyRole("ADMIN", "LOGISTICS_COORDINATOR");
        return ApiMapper.shipment(disruptionRecoveryService.reportWeatherDisruption(shipmentId, severity));
    }

    @DELETE
    @Path("/disruptions/weather/{shipmentId}")
    public Map<String, Object> resolveWeather(@PathParam("shipmentId") Long shipmentId) throws SupplyChainApplicationException {
        accessGuard.requireAnyRole("ADMIN", "LOGISTICS_COORDINATOR");
        return ApiMapper.shipment(disruptionRecoveryService.resolveWeatherDisruption(shipmentId));
    }
}
