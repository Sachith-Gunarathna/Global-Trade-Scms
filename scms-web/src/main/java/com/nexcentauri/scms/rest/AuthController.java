package com.nexcentauri.scms.rest;

import com.nexcentauri.scms.entity.SystemUser;
import com.nexcentauri.scms.exception.SupplyChainApplicationException;
import com.nexcentauri.scms.rest.dto.LoginRequest;
import com.nexcentauri.scms.rest.dto.PasswordChangeRequest;
import com.nexcentauri.scms.rest.dto.ProfileUpdateRequest;
import com.nexcentauri.scms.rest.dto.RegisterRequest;
import com.nexcentauri.scms.service.AuthService;
import jakarta.ejb.EJB;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import jakarta.security.enterprise.credential.Password;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthController {
    @EJB
    private AuthService authService;
    @jakarta.inject.Inject
    private SecurityContext securityContext;

    @POST
    @Path("/login")
    public Response login(LoginRequest request, @Context HttpServletRequest servletRequest, @Context HttpServletResponse servletResponse) throws SupplyChainApplicationException {
        if (request == null || request.getEmail() == null || request.getEmail().isBlank() || request.getPassword() == null || request.getPassword().isBlank()) {
            throw new SupplyChainApplicationException("Email and password are required.");
        }
        AuthenticationParameters parameters = AuthenticationParameters.withParams()
                .newAuthentication(true)
                .credential(new UsernamePasswordCredential(request.getEmail(), new Password(request.getPassword())));
        AuthenticationStatus status = securityContext.authenticate(servletRequest, servletResponse, parameters);
        if (status != AuthenticationStatus.SUCCESS) throw new SupplyChainApplicationException("Invalid email or password.");
        SystemUser user = authService.findByEmail(request.getEmail());
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("user", ApiMapper.user(user));
        return Response.ok(response).build();
    }

    @POST
    @Path("/register")
    public Response register(RegisterRequest request) throws SupplyChainApplicationException {
        if (request == null) throw new SupplyChainApplicationException("Registration details are required.");
        SystemUser user = authService.registerUser(request.getFirstName(), request.getLastName(), request.getEmail(), request.getMobileNumber(), request.getOrganizationOrCompany(), request.getPrimaryHub(), request.getDepartment(), request.getRole(), request.getPassword());
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("user", ApiMapper.user(user));
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path("/verify")
    public Response verify() {
        Principal principal = securityContext.getCallerPrincipal();
        if (principal == null) return Response.status(Response.Status.UNAUTHORIZED).entity(Map.of("success", false, "message", "Authentication is required.")).build();
        SystemUser user = authService.findByEmail(principal.getName());
        if (user == null) return Response.status(Response.Status.UNAUTHORIZED).entity(Map.of("success", false, "message", "User session is invalid.")).build();
        return Response.ok(Map.of("success", true, "user", ApiMapper.user(user))).build();
    }

    @POST
    @Path("/logout")
    public Response logout(@Context HttpServletRequest request) throws ServletException {
        request.logout();
        if (request.getSession(false) != null) request.getSession(false).invalidate();
        return Response.ok(Map.of("success", true)).build();
    }

    @PUT
    @Path("/profile")
    public Response updateProfile(ProfileUpdateRequest request) throws SupplyChainApplicationException {
        if (request == null) throw new SupplyChainApplicationException("Profile details are required.");
        String email = callerEmail();
        SystemUser user = authService.updateProfile(email, request.getFirstName(), request.getLastName(), request.getPhone(), request.getDepartment(), request.getHub(), request.getOrganization());
        return Response.ok(Map.of("success", true, "user", ApiMapper.user(user))).build();
    }

    @PUT
    @Path("/password")
    public Response changePassword(PasswordChangeRequest request) throws SupplyChainApplicationException {
        if (request == null) throw new SupplyChainApplicationException("Password details are required.");
        authService.changePassword(callerEmail(), request.getCurrentPassword(), request.getNewPassword());
        return Response.ok(Map.of("success", true, "message", "Password updated successfully.")).build();
    }

    private String callerEmail() throws SupplyChainApplicationException {
        Principal principal = securityContext.getCallerPrincipal();
        if (principal == null) throw new SupplyChainApplicationException("Authentication is required.");
        return principal.getName();
    }
}
