package com.nexcentauri.scms.rest;

import com.nexcentauri.scms.service.DashboardService;
import jakarta.ejb.EJB;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;

@Path("/dashboard")
@Produces(MediaType.APPLICATION_JSON)
public class DashboardController {
    @EJB
    private DashboardService dashboardService;

    @GET
    public Map<String, Object> dashboard() {
        return dashboardService.getDashboardData();
    }
}
