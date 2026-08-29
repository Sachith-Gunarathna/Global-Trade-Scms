package com.nexcentauri.scms.rest;

import com.nexcentauri.scms.service.DashboardService;
import jakarta.ejb.EJB;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/dashboard")
@Produces(MediaType.APPLICATION_JSON)
public class DashboardController {

    @EJB
    private DashboardService dashboardService;

    @GET
    public Response getDashboard(){

        try {

            Map<String, Object> dashboardData = dashboardService.getDashboardData();

            return Response.ok(dashboardData).build();

        }catch (Exception e){

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("success",false,"error","Unable to load dashboard data.")).build();

        }

    }

}
