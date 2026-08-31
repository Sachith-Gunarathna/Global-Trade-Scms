package com.nexcentauri.scms.rest;

import com.nexcentauri.scms.service.MonitoringService;
import jakarta.ejb.EJB;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("/monitoring")
@Produces(MediaType.APPLICATION_JSON)
public class MonitoringController {
    @EJB
    private MonitoringService monitoringService;

    @GET
    public Map<String, Object> monitoring() {
        return monitoringService.snapshot();
    }

    @GET
    @Path("/routes")
    public Map<Long, Integer> routePriorities() {
        return monitoringService.routePriorities();
    }

    @POST
    @Path("/routes/apply")
    public Map<String, Object> applyRoutes() {
        return Map.of("updated", monitoringService.applyRoutePriorities());
    }

    @GET
    @Path("/timers")
    public Map<String, Object> timers() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timers", monitoringService.timers());
        return result;
    }
}
