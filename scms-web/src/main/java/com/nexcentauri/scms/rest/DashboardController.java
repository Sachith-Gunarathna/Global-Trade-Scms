package com.nexcentauri.scms.rest;

import com.nexcentauri.scms.entity.SystemUser;
import com.nexcentauri.scms.entity.Vendor;
import com.nexcentauri.scms.security.AccessGuard;
import com.nexcentauri.scms.service.DashboardService;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
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

    @Inject
    private AccessGuard accessGuard;

    @GET
    public Map<String, Object> dashboard() {
        SystemUser user = accessGuard.requireAnyRole("ADMIN", "LOGISTICS_COORDINATOR", "WAREHOUSE_MANAGER", "CUSTOMS_AGENT", "VENDOR_REP");
        if (accessGuard.isVendorRepresentative(user)) {
            Vendor vendor = accessGuard.requireRepresentativeVendor(user);
            return dashboardService.getDashboardDataForVendor(vendor.getId(), user.getEmail());
        }
        return dashboardService.getDashboardData();
    }
}
