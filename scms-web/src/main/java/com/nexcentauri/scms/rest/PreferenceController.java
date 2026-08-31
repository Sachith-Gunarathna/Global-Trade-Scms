package com.nexcentauri.scms.rest;

import com.nexcentauri.scms.exception.SupplyChainApplicationException;
import com.nexcentauri.scms.service.UserPreferenceService;
import jakarta.ejb.EJB;
import jakarta.security.enterprise.SecurityContext;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.security.Principal;
import java.util.Map;

@Path("/users/{email}/preferences")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PreferenceController {
    @EJB
    private UserPreferenceService preferenceService;
    @jakarta.inject.Inject
    private SecurityContext securityContext;

    @GET
    public Response get(@PathParam("email") String email) throws SupplyChainApplicationException {
        authorize(email);
        return Response.ok(Map.of("success", true, "preferences", preferenceService.getPreferences(email))).build();
    }

    @PUT
    public Response update(@PathParam("email") String email, Map<String, Object> preferences) throws SupplyChainApplicationException {
        authorize(email);
        return Response.ok(Map.of("success", true, "preferences", preferenceService.updatePreferences(email, preferences))).build();
    }

    private void authorize(String email) throws SupplyChainApplicationException {
        Principal principal = securityContext.getCallerPrincipal();
        if (principal == null) throw new SupplyChainApplicationException("Authentication is required.");
        if (!principal.getName().equalsIgnoreCase(email) && !securityContext.isCallerInRole("ADMIN")) {
            throw new SupplyChainApplicationException("You are not authorized to access these preferences.");
        }
    }
}
